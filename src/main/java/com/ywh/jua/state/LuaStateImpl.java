package com.ywh.jua.state;


import com.ywh.jua.api.*;
import com.ywh.jua.chunk.Prototype;

import static com.ywh.jua.api.ArithOp.LUA_OPBNOT;
import static com.ywh.jua.api.ArithOp.LUA_OPUNM;
import static com.ywh.jua.api.LuaType.*;

/**
 * Lua 状态实现
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public class LuaStateImpl implements LuaState, LuaVM {

    private LuaStack stack = new LuaStack();

    private Prototype proto;

    /**
     * 程序计数器，记录当前执行的指令
     */
    private int pc;

    public LuaStateImpl(Prototype proto) {
        this.proto = proto;
    }

    public LuaStateImpl() {
        proto = null;
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
     * 将栈顶索引设置为指定值，小于当前栈顶索引得值全部弹出
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

    // ========== 转换索引位置的值的类型 ==========

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
        Object result = Arithmetic.arith(a, b, op);
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
                return Comparison.eq(a, b);
            case LUA_OPLT:
                return Comparison.lt(a, b);
            case LUA_OPLE:
                return Comparison.le(a, b);
            default:
                throw new RuntimeException("invalid compare op!");
        }
    }

    /* miscellaneous functions */

    /**
     * 求长度：
     * 取指定索引的值，求出其长度后推入栈顶。
     *
     * @param idx
     */
    @Override
    public void len(int idx) {
        Object val = stack.get(idx);
        if (val instanceof String) {
            pushInteger(((String) val).length());
        } else {
            throw new RuntimeException("length error!");
        }
    }

    /**
     * 拼接：
     * 从栈顶弹出 n 个值，对这些值进行拼接，再把结果推入栈顶。
     * 当 n 为 0，则推入空串；
     * 要求这 n 个值都是字符串。
     *
     * @param n
     */
    @Override
    public void concat(int n) {
        if (n < 0) {
            throw new RuntimeException("n error!");
        }
        if (n == 0) {
            stack.push("");
            return;
        }
        if (n == 1) {
            return;
        }
        for (int i = 1; i < n; i++) {
            if (isString(-1) && isString(-2)) {
                String s2 = toString(-1);
                String s1 = toString(-2);
                pop(2);
                pushString(s1 + s2);
                continue;
            }
            throw new RuntimeException("concatenation error!");
        }
    }

    @Override
    public int getPC() {
        return pc;
    }

    @Override
    public void addPC(int n) {
        pc += n;
    }

    @Override
    public int fetch() {
        return proto.getCode()[pc++];
    }

    @Override
    public void getConst(int idx) {
        stack.push(proto.getConstants()[idx]);
    }

    @Override
    public void getRK(int rk) {

        // constant
        if (rk > 0xFF) {
            getConst(rk & 0xFF);
        } else { // register
            pushValue(rk + 1);
        }
    }
}
