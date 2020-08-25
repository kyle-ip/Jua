package com.ywh.jua.state;


import com.ywh.jua.api.*;
import com.ywh.jua.chunk.BinaryChunk;
import com.ywh.jua.chunk.Prototype;
import com.ywh.jua.chunk.Upvalue;
import com.ywh.jua.vm.Instruction;
import com.ywh.jua.vm.OpCode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.ywh.jua.api.ArithOp.LUA_OPBNOT;
import static com.ywh.jua.api.ArithOp.LUA_OPUNM;
import static com.ywh.jua.api.LuaType.*;
import static com.ywh.jua.api.ThreadStatus.LUA_ERRRUN;
import static com.ywh.jua.api.ThreadStatus.LUA_OK;
import static com.ywh.jua.chunk.BinaryChunk.isBinaryChunk;
import static com.ywh.jua.chunk.BinaryChunk.undump;
import static com.ywh.jua.compiler.Compiler.compile;

import com.ywh.jua.compiler.Compiler;

/**
 * Lua State 实现
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public class LuaStateImpl implements LuaState, LuaVM {

    /**
     * Lua 注册表（实现全局变量等）
     * 注册表是全局状态，每个 Lua 解释器实例都有自己的注册表。
     * Lua API 没有提供专门的方法操作注册表，通过伪索引访问。
     */
    LuaTable registry = new LuaTable(0, 0);

    /**
     * 使用单向链表实现函数调用栈，头部是栈顶，尾部是栈底。
     * 入栈即在链表头部插入一个节点，让这个节点成为新的头部。
     */
    private LuaStack stack = new LuaStack(LUA_MINSTACK);

    public LuaStateImpl() {
        // 创建注册表，放入一个全局环境（用于存放全局变量）。
        registry.put(LUA_RIDX_GLOBALS, new LuaTable(0, 0));

        // 推入一个空 Lua 栈（调用帧）。
        LuaStack stack = new LuaStack(LUA_MINSTACK);
        stack.state = this;
        pushLuaStack(stack);
    }

    // ========== Call Stack ==========

    /**
     * 调用帧入栈
     *
     * @param newTop
     */
    private void pushLuaStack(LuaStack newTop) {
        // 推入一个栈帧，即将其 prev 指针指向当前的栈，并把当前的栈指针重置为新的栈帧。
        newTop.prev = this.stack;
        this.stack = newTop;
    }

    /**
     * 调用帧出栈
     */
    private void popLuaStack() {
        LuaStack top = this.stack;
        this.stack = top.prev;
        top.prev = null;
    }

    /**
     * 返回栈顶
     *
     * @return
     */
    @Override
    public int getTop() {
        return stack.top();
    }

    /**
     * 转换为绝对索引
     *
     * @param idx
     * @return
     */
    @Override
    public int absIndex(int idx) {
        return stack.absIndex(idx);
    }

    /**
     * 检查栈是否有剩余空间
     *
     * @param n
     * @return
     */
    @Override
    public boolean checkStack(int n) {
        return true; // TODO
    }

    /**
     * 弹出 n 个值
     *
     * @param n
     */
    @Override
    public void pop(int n) {
        for (int i = 0; i < n; i++) {
            stack.pop();
        }
    }

    /**
     * 把 fromIdx 的值复制到 toIdx
     *
     * @param fromIdx
     * @param toIdx
     */
    @Override
    public void copy(int fromIdx, int toIdx) {
        stack.set(toIdx, stack.get(fromIdx));
    }

    /**
     * 把指定索引处的值推入栈顶
     *
     * @param idx
     */
    @Override
    public void pushValue(int idx) {
        stack.push(stack.get(idx));
    }

    /**
     * 将栈顶值弹出，写入指定位置
     *
     * @param idx
     */
    @Override
    public void replace(int idx) {
        stack.set(idx, stack.pop());
    }

    /**
     * 将栈顶值弹出，插入指定位置
     *
     * @param idx
     */
    @Override
    public void insert(int idx) {
        rotate(idx, 1);
    }

    /**
     * 移除 idx 处的元素
     * 把 idx 旋转到栈顶，再 pop 出去
     *
     * @param idx
     */
    @Override
    public void remove(int idx) {
        rotate(idx, -1);
        pop(1);
    }

    /**
     * 旋转：
     * 5        5        4        3
     * 4        4        5        2
     * 3   ->   1   ->   1   ->   1
     * 2        2        2        5
     * 1        3        3        4
     *
     * @param idx
     * @param n
     */
    @Override
    public void rotate(int idx, int n) {
        int t = stack.top() - 1;            /* end of stack segment being rotated */
        int p = stack.absIndex(idx) - 1;    /* start of segment */
        int m = n >= 0 ? t - n : p - n - 1; /* end of prefix */

        stack.reverse(p, m);     /* reverse the prefix with length 'n' */
        stack.reverse(m + 1, t); /* reverse the suffix */
        stack.reverse(p, t);     /* reverse the entire segment */
    }

    /**
     * 将栈顶索引设置为指定值，小于当前栈顶索引的值全部弹出
     *
     * @param idx
     */
    @Override
    public void setTop(int idx) {
        int newTop = stack.absIndex(idx);
        if (newTop < 0) {
            throw new RuntimeException("stack underflow!");
        }

        int n = stack.top() - newTop;
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                stack.pop();
            }
        } else if (n < 0) {
            for (int i = 0; i > n; i--) {
                stack.push(null);
            }
        }
    }

    /**
     * 获取类型名称
     *
     * @param tp
     * @return
     */
    @Override
    public String typeName(LuaType tp) {
        switch (tp) {
            case LUA_TNONE:
                return "no value";
            case LUA_TNIL:
                return "nil";
            case LUA_TBOOLEAN:
                return "boolean";
            case LUA_TNUMBER:
                return "number";
            case LUA_TSTRING:
                return "string";
            case LUA_TTABLE:
                return "table";
            case LUA_TFUNCTION:
                return "function";
            case LUA_TTHREAD:
                return "thread";
            default:
                return "userdata";
        }
    }

    /**
     * 获取指定索引位置的值的类型
     *
     * @param idx
     * @return
     */
    @Override
    public LuaType type(int idx) {
        return stack.isValid(idx)
            ? LuaValue.typeOf(stack.get(idx))
            : LUA_TNONE;
    }

    // ========== 判断指定索引位置的值的类型 ==========

    @Override
    public boolean isNone(int idx) {
        return type(idx) == LUA_TNONE;
    }

    @Override
    public boolean isNil(int idx) {
        return type(idx) == LUA_TNIL;
    }

    @Override
    public boolean isNoneOrNil(int idx) {
        LuaType t = type(idx);
        return t == LUA_TNONE || t == LUA_TNIL;
    }

    @Override
    public boolean isBoolean(int idx) {
        return type(idx) == LUA_TBOOLEAN;
    }

    @Override
    public boolean isInteger(int idx) {
        return stack.get(idx) instanceof Long;
    }

    @Override
    public boolean isNumber(int idx) {
        return toNumberX(idx) != null;
    }

    @Override
    public boolean isString(int idx) {
        LuaType t = type(idx);
        return t == LUA_TSTRING || t == LUA_TNUMBER;
    }

    @Override
    public boolean isTable(int idx) {
        return type(idx) == LUA_TTABLE;
    }

    @Override
    public boolean isThread(int idx) {
        return type(idx) == LUA_TTHREAD;
    }

    @Override
    public boolean isFunction(int idx) {
        return type(idx) == LUA_TFUNCTION;
    }

    // ========== 转换索引位置的值的类型并返回 ==========

    @Override
    public boolean toBoolean(int idx) {
        return LuaValue.toBoolean(stack.get(idx));
    }

    @Override
    public long toInteger(int idx) {
        Long i = toIntegerX(idx);
        return i == null ? 0 : i;
    }

    @Override
    public Long toIntegerX(int idx) {
        Object val = stack.get(idx);
        return val instanceof Long ? (Long) val : null;
    }

    @Override
    public double toNumber(int idx) {
        Double n = toNumberX(idx);
        return n == null ? 0 : n;
    }

    @Override
    public Double toNumberX(int idx) {
        Object val = stack.get(idx);
        if (val instanceof Double) {
            return (Double) val;
        } else if (val instanceof Long) {
            return ((Long) val).doubleValue();
        } else {
            return null;
        }
    }

    @Override
    public String toString(int idx) {
        Object val = stack.get(idx);
        if (val instanceof String) {
            return (String) val;
        } else if (val instanceof Long || val instanceof Double) {
            return val.toString();
        } else {
            return null;
        }
    }

    // ========== 指定类型的值入栈 ==========

    @Override
    public void pushNil() {
        stack.push(null);
    }

    @Override
    public void pushBoolean(boolean b) {
        stack.push(b);
    }

    @Override
    public void pushInteger(long n) {
        stack.push(n);
    }

    @Override
    public void pushNumber(double n) {
        stack.push(n);
    }

    @Override
    public void pushString(String s) {
        stack.push(s);
    }

    /**
     * 算数、按位运算：
     * 依次弹出右操作数、左操作数，调用 {@link com.ywh.jua.state.Arithmetic #arith 执行计算 }；
     * 如果操作符为取反、符号，则左右操作数为同一个。
     *
     * @param op
     */
    @Override
    public void arith(ArithOp op) {
        Object b = stack.pop();
        Object a = op != LUA_OPUNM && op != LUA_OPBNOT ? stack.pop() : b;
        Object result = Arithmetic.arith(a, b, op, this);
        if (result != null) {
            stack.push(result);
        } else {
            throw new RuntimeException("arithmetic error!");
        }
    }

    /**
     * 比较运算：
     * 比较栈中指定下标的两个元素
     *
     * @param idx1
     * @param idx2
     * @param op
     * @return
     */
    @Override
    public boolean compare(int idx1, int idx2, CmpOp op) {
        if (!stack.isValid(idx1) || !stack.isValid(idx2)) {
            return false;
        }
        Object a = stack.get(idx1), b = stack.get(idx2);
        switch (op) {
            case LUA_OPEQ:
                return Comparison.eq(a, b, this);
            case LUA_OPLT:
                return Comparison.lt(a, b, this);
            case LUA_OPLE:
                return Comparison.le(a, b, this);
            case LUA_OPGT:
                return !Comparison.le(a, b, this);
            case LUA_OPGE:
                return !Comparison.lt(a, b, this);
            default:
                throw new RuntimeException("invalid compare op!");
        }
    }


    /**
     * 创建空表，将其推入栈顶。
     */
    @Override
    public void newTable() {
        createTable(0, 0);
    }

    /**
     * 创建空表（指定数组和哈希表的初始容量），将其推入栈顶。
     *
     * @param nArr
     * @param nRec
     */
    @Override
    public void createTable(int nArr, int nRec) {
        stack.push(new LuaTable(nArr, nRec));
    }

    /**
     * 根据索引从栈中取表，再根据从栈顶弹出的键取值（并将其推入栈顶）。
     *
     * @param idx
     * @return
     */
    @Override
    public LuaType getTable(int idx) {
        Object t = stack.get(idx);
        Object k = stack.pop();
        return getTable(t, k, false);
    }

    /**
     * {@link #getTable}，键为字符串。
     *
     * @param idx
     * @param k
     * @return
     */
    @Override
    public LuaType getField(int idx, String k) {
        Object t = stack.get(idx);
        return getTable(t, k, false);
    }

    /**
     * {@link #getTable}，键为整数。
     *
     * @param idx
     * @param i
     * @return
     */
    @Override
    public LuaType getI(int idx, long i) {
        Object t = stack.get(idx);
        return getTable(t, i, false);
    }

    /**
     * 从指定表中根据指定键取出值（并将其推入栈顶）。
     *
     * @param t
     * @param k
     * @param raw
     * @return
     */
    private LuaType getTable(Object t, Object k, boolean raw) {
        if (t instanceof LuaTable) {
            LuaTable tbl = (LuaTable) t;
            Object v = tbl.get(k);
            // __index 元方法对象既可以是函数（t[k] 表示以 t 和 k 为参数调用该函数）也可以是表（以 k 为键访问 t）。
            if (raw || v != null || !tbl.hasMetafield("__index")) {
                stack.push(v);
                return LuaValue.typeOf(v);
            }
        }
        // raw 字段为 true，则忽略元方法。
        // 如果 t[k] 的 t 是表，且键已经在表中，或者需要忽略元方法，或者表没有索引元方法，则维持原来逻辑，否则尝试调用元方法。
        if (!raw) {
            Object mf = getMetafield(t, "__index");
            if (mf != null) {
                if (mf instanceof LuaTable) {
                    return getTable(mf, k, false);
                } else if (mf instanceof Closure) {
                    Object v = callMetamethod(t, k, mf);
                    stack.push(v);
                    return LuaValue.typeOf(v);
                }
            }
        }
        // TODO
        throw new RuntimeException("not a table!");
    }

    /* set functions (stack -> Lua) */

    /**
     * 根据索引取出指定的表，从栈中先后弹出值、键，把键值对设置到表中。
     *
     * @param idx
     */
    @Override
    public void setTable(int idx) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        Object k = stack.pop();
        setTable(t, k, v, false);
    }

    /**
     * 根据索引取出指定的表，从栈中弹出值，把键值对设置到表中（字符串）。
     *
     * @param idx
     * @param k
     */
    @Override
    public void setField(int idx, String k) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        setTable(t, k, v, false);
    }

    /**
     * 根据索引取出指定的表，从栈中弹出值，把键值对设置到表中（整数）。
     *
     * @param idx
     * @param i
     */
    @Override
    public void setI(int idx, long i) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        setTable(t, i, v, false);
    }

    /**
     * @param t
     * @param k
     * @param v
     * @param raw
     */
    private void setTable(Object t, Object k, Object v, boolean raw) {
        // 区别于“__index”，“__newindex” 用于当执行 t[k] = v 时，如果 t 不是表，或者 k 在表中不存在。

        if (t instanceof LuaTable) {
            LuaTable tbl = (LuaTable) t;
            if (raw || tbl.get(k) != null || !tbl.hasMetafield("__newindex")) {
                tbl.put(k, v);
                return;
            }
        }
        if (!raw) {
            Object mf = getMetafield(t, "__newindex");
            if (mf != null) {
                if (mf instanceof LuaTable) {
                    setTable(mf, k, v, false);
                    return;
                }
                if (mf instanceof Closure) {
                    stack.push(mf);
                    stack.push(t);
                    stack.push(k);
                    stack.push(v);
                    call(3, 0);
                    return;
                }
            }
        }
        throw new RuntimeException("not a table!");
    }

    /**
     * 加载二进制 chunk 或 Lua 脚本，把主函数原型实例化为闭包并推入栈顶。
     * <p>
     * 通过参数 mode（可选 “b”、“t”、“bt”）选定加载模式：
     * b：如果加载二进制 chunk，则只需读文件、解析函数原型、实例化为闭包、推入栈顶；
     * t：如果加载文本 Lua 脚本，则先进行编译。
     * bt：都可以，根据实际情况处理。
     * <p>
     * 如果 load 方法无法加载 chunk，则要在栈顶留下一条错误消息。
     * 返回一个状态码，0 表示成功，其他表示失败。
     *
     * @param chunk
     * @param chunkName
     * @param mode
     * @return
     */
    @Override
    public ThreadStatus load(byte[] chunk, String chunkName, String mode) {

        // 解析字节数组为函数原型，把实例化为闭包的函数原型推入栈顶。
        Prototype proto = isBinaryChunk(chunk) ? undump(chunk) : compile(new String(chunk), chunkName);
        Closure closure = new Closure(proto);
        stack.push(closure);

        // 闭包需要 Upvalue
        if (proto.getUpvalues().length > 0) {
            // 第一个 Upvalue（对于主函数来说是 _ENV）会被初始化为全局环境，其他的 Upvalue 会被初始化成 nil。
            // 由于 Upvalue 的初始值为 nil，所以只要把第一个 Upvalue 值设置成全局环境即可。
            Object env = registry.get(LUA_RIDX_GLOBALS);
            closure.upvals[0] = new UpvalueHolder(env);
        }
        return LUA_OK;
    }

    /**
     * 调用 Lua 函数
     * 在执行之前，必须先把被调用函数入栈，然后把参数值依次入栈；
     * call 方法调用结束后，参数值和函数会被弹出，取而代之的是指定数量的返回值。
     * <p>
     * 接收两个参数，其一是准备传递给被调用函数的参数数量（同时隐含给出被调用函数在栈中的位置）；
     * 其二是需要的返回值数量（多退少补），-1 表示返回值全部留在栈顶。
     *
     * @param nArgs
     * @param nResults
     */
    @Override
    public void call(int nArgs, int nResults) {

        // 取出被调用函数
        Object val = stack.get(-(nArgs + 1));
        Object f = val instanceof Closure ? val : null;

        // 试图“调用”一个非函数类型（闭包）的值，则会判断它是否存在元方法；
        // 如果存在，则以该值为第一个参数，后跟原方法调用的其他参数来调用元方法。
        if (f == null) {
            Object mf = getMetafield(val, "__call");
            if (mf != null && mf instanceof Closure) {
                stack.push(f);
                insert(-(nArgs + 2));
                nArgs += 1;
                f = mf;
            }
        }

        if (f != null) {
            Closure c = (Closure) f;
            // 执行调用
            if (c.proto != null) {
                callLuaClosure(nArgs, nResults, c);
            } else {
                callJavaClosure(nArgs, nResults, c);
            }
        } else {
            throw new RuntimeException("not function!");
        }
    }

    /**
     * 执行被调用函数
     *
     * @param nArgs
     * @param nResults
     * @param c
     */
    private void callLuaClosure(int nArgs, int nResults, Closure c) {

        // 从函数原型取出执行函数需要的寄存器数量、声明的固定参数数量以及是否 vararg 函数。
        int nRegs = c.proto.getMaxStackSize();
        int nParams = c.proto.getNumParams();
        boolean isVararg = c.proto.getIsVararg() == 1;

        // 创建被调用帧（适当扩大，为指令实现函数预留少量栈空间），指定闭包。
        LuaStack newStack = new LuaStack(nRegs + LUA_MINSTACK);
        newStack.closure = c;

        // 把函数和参数值从主调用帧弹出。
        List<Object> funcAndArgs = stack.popN(nArgs + 1);

        // 按照固定参数数量，把参数传入被调用帧。
        // 如果被调用函数是 vararg 参数，且传入参数的数量多于固定参数数量，需要把 vararg 参数记录在被调用帧。
        newStack.pushN(funcAndArgs.subList(1, funcAndArgs.size()), nParams);
        if (nArgs > nParams && isVararg) {
            newStack.varargs = funcAndArgs.subList(nParams + 1, funcAndArgs.size());
        }

        // 被调用帧入栈（成为“当前帧”），且超出 nRegs 部分为溢出。
        pushLuaStack(newStack);
        setTop(nRegs);

        // 执行被调用函数的指令，调用完成后弹出被调用帧（恢复主调用帧为“当前帧”）。
        runLuaClosure();
        popLuaStack();

        // 如果有返回值，则从被调用帧中获取，并放入当前帧。
        if (nResults != 0) {
            List<Object> results = newStack.popN(newStack.top() - nRegs);
            //stack.check(results.size())
            stack.pushN(results, nResults);
        }
    }

    /**
     * 执行 Java 函数
     *
     * @param nArgs
     * @param nResults
     * @param c
     */
    private void callJavaClosure(int nArgs, int nResults, Closure c) {
        // 创建新调用帧。
        LuaStack newStack = new LuaStack(nArgs + LUA_MINSTACK);
        newStack.state = this;
        newStack.closure = c;

        // 把参数值从主调用帧中弹出，推入被调用帧后，Java 闭包从主调用帧中弹出。
        if (nArgs > 0) {
            newStack.pushN(stack.popN(nArgs), nArgs);
        }
        stack.pop();

        // 把被调用帧推入调用栈，成为当前帧；执行 Java 函数，完成后把被调用帧同调用栈弹出（主调用帧又成为当前帧）。
        pushLuaStack(newStack);
        int r = c.javaFunc.invoke(this);
        popLuaStack();

        // 如果有返回值，则把返回值从被调用帧弹出、推入主调用帧（多退少补）。
        if (nResults != 0) {
            List<Object> results = newStack.popN(r);
            //stack.check(results.size())
            stack.pushN(results, nResults);
        }
    }

    /**
     * 逐条执行被调用函数的指令，直到遇到 RETURN 指令。
     */
    private void runLuaClosure() {
        for (; ; ) {
            int i = fetch();
            OpCode opCode = Instruction.getOpCode(i);
            opCode.getAction().execute(i, this);
            if (opCode == OpCode.RETURN) {
                break;
            }
        }
    }

    /* miscellaneous functions */

    /**
     * 求长度
     * 取指定索引的值，求出其长度后推入栈顶。
     *
     * @param idx
     */
    @Override
    public void len(int idx) {

        Object val = stack.get(idx);
        // 该值为字符串，则求长度后推入栈顶。
        if (val instanceof String) {
            pushInteger(((String) val).length());
            return;
        }

        // 该值的类型存在对应的长度元方法。
        Object mm = getMetamethod(val, val, "__len");
        if (mm != null) {
            stack.push(callMetamethod(val, val, mm));
            return;
        }

        // 该值为表。
        if (val instanceof LuaTable) {
            pushInteger(((LuaTable) val).length());
            return;
        }
        throw new RuntimeException("length error!");
    }

    /**
     * 拼接
     * 从栈顶弹出 n 个值，对这些值进行拼接，再把结果推入栈顶。
     * 当 n 为 0，则推入空串；
     * 要求这 n 个值都是字符串。
     *
     * @param n
     */
    @Override
    public void concat(int n) {
        if (n == 0) {
            stack.push("");
        } else if (n >= 2) {
            for (int i = 1; i < n; i++) {
                // 栈顶两个值都为字符串。
                if (isString(-1) && isString(-2)) {
                    String s2 = toString(-1);
                    String s1 = toString(-2);
                    pop(2);
                    pushString(s1 + s2);
                    continue;
                }

                // 栈顶两个值至少一个不为字符串，查找类型对应的拼接元方法。
                Object b = stack.pop();
                Object a = stack.pop();
                Object mm = getMetamethod(a, b, "__concat");
                if (mm != null) {
                    stack.push(callMetamethod(a, b, mm));
                    continue;
                }

                throw new RuntimeException("concatenation error!");
            }
        }
    }


    /**
     * 修改 PC（用于实现跳转指令）
     *
     * @param n
     */
    @Override
    public void addPC(int n) {
        stack.pc += n;
    }

    /**
     * （从指令表）取出当前指令（将 PC 指向下一条指令）
     *
     * @return
     */
    @Override
    public int fetch() {
        return stack.closure.proto.getCode()[stack.pc++];
    }

    /**
     * （从常量表取出一个常量值）将指定常量推入栈顶
     *
     * @param idx
     */
    @Override
    public void getConst(int idx) {
        stack.push(stack.closure.proto.getConstants()[idx]);
    }

    /**
     * 将指定常量或栈值推入栈顶
     * 其中 rk 为 iABC 模式指令里的 OpArgK 类型参数。
     *
     * @param rk
     */
    @Override
    public void getRK(int rk) {

        if (rk > 0xFF) {
            // constant
            getConst(rk & 0xFF);
        } else {
            // register
            pushValue(rk + 1);
        }
    }

    /**
     * 返回当前寄存器数量（栈深度）
     *
     * @return
     */
    @Override
    public int registerCount() {
        return stack.closure.proto.getMaxStackSize();
    }

    /**
     * 把 n 个变长参数推入栈顶（多退少补）。
     *
     * @param n
     */
    @Override
    public void loadVararg(int n) {
        List<Object> varargs = stack.varargs != null ? stack.varargs : Collections.emptyList();
        if (n < 0) {
            n = varargs.size();
        }

        //stack.check(n)
        stack.pushN(varargs, n);
    }

    /**
     * 读取指定的子函数原型，封装为闭包后推入栈顶。
     *
     * @param idx
     */
    @Override
    public void loadProto(int idx) {
        Prototype proto = stack.closure.proto.getProtos()[idx];
        Closure closure = new Closure(proto);
        stack.push(closure);

        // 根据函数原型中的 Upvalue 表来初始化闭包的 Upvalue 值
        for (int i = 0; i < proto.getUpvalues().length; i++) {
            Upvalue uvInfo = proto.getUpvalues()[i];
            int uvIdx = uvInfo.getIdx();

            // 该 Upvalue 捕获的是当前函数的局部变量。
            // 开放状态：Upvalue 捕获的外围函数局部变量还在栈上，直接引用（寄存器里的 Lua 值）；
            // 闭合状态：Upvalue 捕获的外围函数局部变量不在栈上，需要保存在其他地方。
            // 开放 => 闭合：把寄存器里的 Lua 值复制出来，再更新 Upvalue。
            if (uvInfo.getInstack() == 1) {
                if (stack.openuvs == null) {
                    stack.openuvs = new HashMap<>();
                }
                if (stack.openuvs.containsKey(uvIdx)) {
                    closure.upvals[i] = stack.openuvs.get(uvIdx);
                } else {
                    closure.upvals[i] = new UpvalueHolder(stack, uvIdx);
                    stack.openuvs.put(uvIdx, closure.upvals[i]);
                }
            }
            // 该 Upvalue 捕获的是更外围函数中的局部变量（0）。
            else {
                closure.upvals[i] = stack.closure.upvals[uvIdx];
            }
        }
    }

    /**
     * 闭合处于开启状态的 Upvalue：
     * 开放 => 闭合：把寄存器里的 Lua 值复制出来，再更新 Upvalue。
     *
     * @param a
     */
    @Override
    public void closeUpvalues(int a) {
        if (stack.openuvs == null) {
            return;
        }
        for (Iterator<UpvalueHolder> it = stack.openuvs.values().iterator(); it.hasNext(); ) {
            UpvalueHolder uv = it.next();
            if (uv.index >= a - 1) {
                uv.migrate();
                it.remove();
            }
        }
    }

    /**
     * 判断栈中指定索引处的值是否可转换为 Java 函数。
     *
     * @param idx
     * @return
     */
    @Override
    public boolean isJavaFunction(int idx) {
        Object val = stack.get(idx);
        return val instanceof Closure && ((Closure) val).javaFunc != null;
    }

    /**
     * 从栈中取指定索引的闭包、转换为 Java 函数并返回。
     *
     * @param idx
     * @return
     */
    @Override
    public JavaFunction toJavaFunction(int idx) {
        Object val = stack.get(idx);
        return val instanceof Closure ? ((Closure) val).javaFunc : null;
    }

    /**
     * 接收一个 Java 函数参数，把它抓换为闭包后入栈。
     *
     * @param f
     */
    @Override
    public void pushJavaFunction(JavaFunction f) {
        stack.push(new Closure(f, 0));
    }

    /**
     * 把Java 函数转换成 Java 闭包推入栈顶（捕获 Upvalue）。
     *
     * @param f
     * @param n
     */
    @Override
    public void pushJavaClosure(JavaFunction f, int n) {
        Closure closure = new Closure(f, n);

        // 从栈顶弹出 n 个 Lua 值，成为 Java 闭包的 Upvlue。
        for (int i = n; i > 0; i--) {
            Object val = stack.pop();
            closure.upvals[i - 1] = new UpvalueHolder(val); // TODO
        }

        // 把 Java 闭包推入栈顶。
        stack.push(closure);
    }

    /**
     * 把全局环境推入栈顶
     */
    @Override
    public void pushGlobalTable() {
        stack.push(registry.get(LUA_RIDX_GLOBALS));
    }

    /**
     * 获取全局变量
     *
     * @param name
     * @return
     */
    @Override
    public LuaType getGlobal(String name) {
        // 从注册表中取出全局环境。
        Object t = registry.get(LUA_RIDX_GLOBALS);
        return getTable(t, name, false);
    }

    /**
     * 设置全局变量（值为栈顶的）
     *
     * @param name
     */
    @Override
    public void setGlobal(String name) {
        Object t = registry.get(LUA_RIDX_GLOBALS);
        Object v = stack.pop();
        setTable(t, name, v, false);
    }

    /**
     * 给全局环境设置 Java 函数（值）
     *
     * @param name
     * @param f
     */
    @Override
    public void register(String name, JavaFunction f) {
        pushJavaFunction(f);
        setGlobal(name);
    }

    /**
     * 取给定索引的值关联的元表置于栈顶。
     *
     * @param idx
     * @return
     */
    @Override
    public boolean getMetatable(int idx) {
        Object val = stack.get(idx);
        Object mt = getMetatable(val);
        if (mt != null) {
            stack.push(mt);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置指定索引的值为栈顶的元表
     *
     * @param idx
     */
    @Override
    public void setMetatable(int idx) {
        Object val = stack.get(idx);
        Object mtVal = stack.pop();
        if (mtVal == null) {
            setMetatable(val, null);
        } else if (mtVal instanceof LuaTable) {
            setMetatable(val, (LuaTable) mtVal);
        } else {
            // TODO
            throw new RuntimeException("table expected!");
        }
    }

    /**
     * @param val
     * @return
     */
    private LuaTable getMetatable(Object val) {

        if (val instanceof LuaTable) {
            return ((LuaTable) val).metatable;
        }
        String key = "_MT" + LuaValue.typeOf(val);
        Object mt = registry.get(key);
        return mt != null ? (LuaTable) mt : null;
    }

    /**
     * @param val
     * @param mt
     */
    private void setMetatable(Object val, LuaTable mt) {
        // 判断值是否为表，是则直接修改其元表字段，否则根据变量类型把元表存储在注册表中。
        if (val instanceof LuaTable) {
            ((LuaTable) val).metatable = mt;
            return;
        }
        String key = "_MT" + LuaValue.typeOf(val);
        registry.put(key, mt);
    }

    /**
     * 获取元字段
     *
     * @param val
     * @param fieldName
     * @return
     */
    private Object getMetafield(Object val, String fieldName) {
        LuaTable mt = getMetatable(val);
        return mt != null ? mt.get(fieldName) : null;
    }

    /**
     * 获取元方法
     *
     * @param a
     * @param b
     * @param mmName
     * @return
     */
    Object getMetamethod(Object a, Object b, String mmName) {
        Object mm = getMetafield(a, mmName);
        if (mm == null) {
            mm = getMetafield(b, mmName);
        }
        return mm;
    }

    /**
     * 调用元方法
     *
     * @param a
     * @param b
     * @param mm
     * @return
     */
    Object callMetamethod(Object a, Object b, Object mm) {
        //stack.check(4)
        stack.push(mm);
        stack.push(a);
        stack.push(b);
        // 元方法两个参数，一个返回值/
        call(2, 1);
        return stack.pop();
    }


    /**
     * 取长度（忽略元方法）
     *
     * @param idx
     * @return
     */
    @Override
    public int rawLen(int idx) {
        Object val = stack.get(idx);
        if (val instanceof String) {
            return ((String) val).length();
        } else if (val instanceof LuaTable) {
            return ((LuaTable) val).length();
        } else {
            return 0;
        }
    }

    /**
     * 判断相等（忽略元方法）
     *
     * @param idx1
     * @param idx2
     * @return
     */
    @Override
    public boolean rawEqual(int idx1, int idx2) {
        if (!stack.isValid(idx1) || !stack.isValid(idx2)) {
            return false;
        }

        Object a = stack.get(idx1);
        Object b = stack.get(idx2);
        return Comparison.eq(a, b, null);
    }

    /**
     * 从表中取值（忽略元方法）
     *
     * @param idx
     * @return
     */
    @Override
    public LuaType rawGet(int idx) {
        Object t = stack.get(idx);
        Object k = stack.pop();
        return getTable(t, k, true);
    }

    /**
     * 从表中取整型值（忽略元方法）
     *
     * @param idx
     * @param i
     * @return
     */
    @Override
    public LuaType rawGetI(int idx, long i) {
        Object t = stack.get(idx);
        return getTable(t, i, true);
    }

    /**
     * 从表中设置值（忽略元方法）
     *
     * @param idx
     */
    @Override
    public void rawSet(int idx) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        Object k = stack.pop();
        setTable(t, k, v, true);
    }

    /**
     * 从表中设置整型值（忽略元方法）
     *
     * @param idx
     * @param i
     */
    @Override
    public void rawSetI(int idx, long i) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        setTable(t, i, v, true);
    }

    /**
     * 根据键迭代取指定索引的表的下一个键值对
     *
     * @param idx
     * @return
     */
    @Override
    public boolean next(int idx) {
        // 取指定索引的表。
        Object val = stack.get(idx);
        if (val instanceof LuaTable) {
            LuaTable t = (LuaTable) val;
            // 上一个键从栈顶弹出，再取其下一个键。
            // key 为空，
            Object key = stack.pop();
            Object nextKey = t.nextKey(key);

            // 遍历未结束，把下一个键值对推入栈中，返回 true；
            if (nextKey != null) {
                stack.push(nextKey);
                stack.push(t.get(nextKey));
                return true;
            }

            // 遍历已结束，返回 false。
            return false;
        }
        throw new RuntimeException("table expected!");
    }

    /**
     * 从栈顶弹出一个值作为错误抛出。
     *
     * @return
     */
    @Override
    public int error() {
        Object err = stack.pop();
        // TODO
        throw new RuntimeException(err.toString());
    }

    /**
     * 调用函数并处理异常
     *
     * @param nArgs
     * @param nResults
     * @param msgh
     * @return
     */
    @Override
    public ThreadStatus pCall(int nArgs, int nResults, int msgh) {
        LuaStack caller = stack;
        try {
            // 尝试正常调用函数
            call(nArgs, nResults);
            return LUA_OK;
        } catch (Exception e) {

            // 存在指定的错误处理器
            if (msgh != 0) {
                throw e;
            }
            while (stack != caller) {
                popLuaStack();
            }
            // TODO
            stack.push(e.getMessage());
            return LUA_ERRRUN;
        }
    }
}
