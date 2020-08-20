package com.ywh.jua.vm;


import com.ywh.jua.api.ArithOp;
import com.ywh.jua.api.CmpOp;
import com.ywh.jua.api.LuaVM;

import static com.ywh.jua.api.ArithOp.*;
import static com.ywh.jua.api.CmpOp.*;
import static com.ywh.jua.api.LuaType.*;

/**
 * 指令集
 *
 * @author ywh
 * @since 2020/8/19 11:26
 */
public class Instructions {


    /* number of list items to accumulate before a SETLIST instruction */
    public static final int LFIELDS_PER_FLUSH = 50;

    /* ========== 移动和跳转指令（misc）========== */

    /**
     * MOVE 指令（iABC 模式）
     * 把源寄存器（索引由操作数指定）里的值移动到目标寄存器（索引由操作数指定）里；但实际上是复制，因为源寄存器的值原封不动。
     * 常用于局部变量赋值和传参，局部变量实际存在于寄存器中，由于 MOVE 等指令使用操作数 A 表示目标寄存器索引，所以局部变量数量不超过 255 个。
     * 
     * R(A) := R(B)
     *
     * @param i
     * @param vm
     */
    public static void move(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.copy(b, a);
    }

    /**
     * JMP 指令（iAsBx 模式）
     * 执行无条件跳转（Lua 支持 tag 和 goto）。
     * 
     * pc+=sBx; if (A) close all upvalues >= R(A - 1)
     *
     * @param i
     * @param vm
     */
    public static void jmp(int i, LuaVM vm) {
        int a = Instruction.getA(i);
        int sBx = Instruction.getSBx(i);
        vm.addPC(sBx);
        if (a != 0) {
            throw new RuntimeException("todo: jmp!");
        }
    }

    /* ========== 加载指令（load）========== */

    /**
     * LOCDNIL 指令（iABC 模式）
     * 给连续 n 个寄存器放置 nil 值。
     * 
     * R(A), R(A+1), ..., R(A+B) := nil
     *
     * @param i
     * @param vm
     */
    public static void loadNil(int i, LuaVM vm) {
        // 取当前指令的操作数 A（寄存器起始索引）、操作数 B（寄存器数量），操作数 C 没有用
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);

        // 推入一个 nil
        vm.pushNil();

        // 从栈顶拷贝到从 a 到 a + b 的 B 个寄存器
        for (int j = a; j <= a + b; j++) {
            vm.copy(-1, j);
        }

        // 推出栈顶的 nil
        vm.pop(1);
    }

    /**
     * LOADBOOL 命令（iABC 模式）
     * 给单个寄存器设置布尔值
     * 
     * R(A) := (bool)B; if (C) pc++
     *
     * @param i
     * @param vm
     */
    public static void loadBool(int i, LuaVM vm) {

        // 寄存器索引
        int a = Instruction.getA(i) + 1;

        // 布尔值（非 0 为真）
        int b = Instruction.getB(i);

        // 标记
        int c = Instruction.getC(i);

        // 布尔值入栈，替换到 A 的位置
        vm.pushBoolean(b != 0);
        vm.replace(a);

        // C 非 0 则跳过下一条指令
        if (c != 0) {
            vm.addPC(1);
        }
    }

    /**
     * LOADK 指令（iABx 模式）
     * 将常量表里某个常量加载到指定寄存器
     * 
     * Lua 函数里出现的字面量（数字、字符串）会被百年一起收集，放进常量表。
     * 
     * R(A) := Kst(Bx)
     *
     * @param i
     * @param vm
     */
    public static void loadK(int i, LuaVM vm) {

        // 寄存器索引
        int a = Instruction.getA(i) + 1;

        // 常量表索引
        int bx = Instruction.getBx(i);

        // 取出 Bx 位置的常量、推入栈顶
        vm.getConst(bx);

        // 从栈顶弹出，替换到位置 A
        vm.replace(a);
    }

    /**
     * LOADKX 指令（iABx 模式）
     * 需要和 EXTRAARG 指令（iAx 模式）搭配使用，用后者的 Ax 操作数来指导常量索引。
     * 
     * R(A) := Kst(extra arg)
     *
     * @param i
     * @param vm
     */
    public static void loadKx(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int ax = Instruction.getAx(vm.fetch());
        vm.getConst(ax);
        vm.replace(a);
    }

    /* ========== 运算符指令（arith）========== */

    /**
     * +
     *
     * @param i
     * @param vm
     */
    public static void add(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPADD);
    }

    /**
     * -
     *
     * @param i
     * @param vm
     */
    public static void sub(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPSUB);
    }

    /**
     * *
     *
     * @param i
     * @param vm
     */
    public static void mul(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPMUL);
    }

    /**
     * %
     *
     * @param i
     * @param vm
     */
    public static void mod(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPMOD);
    }

    /**
     * ^
     *
     * @param i
     * @param vm
     */
    public static void pow(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPPOW);
    }

    /**
     * /
     *
     * @param i
     * @param vm
     */
    public static void div(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPDIV);
    }

    /**
     * //
     *
     * @param i
     * @param vm
     */
    public static void idiv(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPIDIV);
    }

    /**
     * &
     *
     * @param i
     * @param vm
     */
    public static void band(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPBAND);
    }

    /**
     * |
     *
     * @param i
     * @param vm
     */
    public static void bor(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPBOR);
    }

    /**
     * ~
     *
     * @param i
     * @param vm
     */
    public static void bxor(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPBXOR);
    }

    /**
     * <<
     *
     * @param i
     * @param vm
     */
    public static void shl(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPSHL);
    }

    /**
     * >>
     *
     * @param i
     * @param vm
     */
    public static void shr(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPSHR);
    }

    /**
     * -
     *
     * @param i
     * @param vm
     */
    public static void unm(int i, LuaVM vm) {
        unaryArith(i, vm, LUA_OPUNM);
    }

    /**
     * ~
     *
     * @param i
     * @param vm
     */
    public static void bnot(int i, LuaVM vm) {
        unaryArith(i, vm, LUA_OPBNOT);
    }

    /**
     * 二元算数运算指令（iABC 模式）
     * 对两个寄存器或常量值（索引由操作数 B 和 C 指定）进行运算，将结果放入另一个寄存器（索引由操作数 A 指定）
     * 
     * R(A) := RK(B) op RK(C)
     *
     * @param i
     * @param vm
     * @param op
     */
    private static void binaryArith(int i, LuaVM vm, ArithOp op) {

        // 目标寄存器
        int a = Instruction.getA(i) + 1;

        // 右操作数
        int b = Instruction.getB(i);

        // 左操作数
        int c = Instruction.getC(i);

        // 左操作数、右操作数运算后推入栈顶，并替换到 A
        vm.getRK(b);
        vm.getRK(c);
        vm.arith(op);
        vm.replace(a);
    }

    /**
     * 二元算数运算指令（iABC 模式）
     * 
     * 对操作数 B 所指的寄存器的值进行运算，然后把结果放入操作数 A 指定的寄存器中。
     * 
     * R(A) := op R(B)
     *
     * @param i
     * @param vm
     * @param op
     */
    private static void unaryArith(int i, LuaVM vm, ArithOp op) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.pushValue(b);
        vm.arith(op);
        vm.replace(a);
    }

    /* ========== 比较指令（compare）========== */

    /**
     * ==
     *
     * @param i
     * @param vm
     */
    public static void eq(int i, LuaVM vm) {
        compare(i, vm, LUA_OPEQ);
    }

    /**
     * <
     *
     * @param i
     * @param vm
     */
    public static void lt(int i, LuaVM vm) {
        compare(i, vm, LUA_OPLT);
    }

    /**
     * <=
     *
     * @param i
     * @param vm
     */
    public static void le(int i, LuaVM vm) {
        compare(i, vm, LUA_OPLE);
    }

    /**
     * >
     *
     * @param i
     * @param vm
     */
    public static void gt(int i, LuaVM vm) {
        compare(i, vm, LUA_OPGT);
    }

    /**
     * >=
     *
     * @param i
     * @param vm
     */
    public static void ge(int i, LuaVM vm) {
        compare(i, vm, LUA_OPGE);
    }

    // if ((RK(B) op RK(C)) ~= A) then pc++

    /**
     * 比较指令（iABC 模式）
     * 比较寄存器或常量表的两个值（B 和 C），如果比较结果和操作数 A 匹配，则跳过下一条指令。
     *
     * @param i
     * @param vm
     * @param op
     */
    private static void compare(int i, LuaVM vm, CmpOp op) {
        int a = Instruction.getA(i);
        int b = Instruction.getB(i);
        int c = Instruction.getC(i);
        vm.getRK(b);
        vm.getRK(c);
        if (vm.compare(-2, -1, op) == (a == 0)) {
            vm.addPC(1);
        }
        vm.pop(2);
    }

    /* logical */

    /* ========== 逻辑指令（logical）========== */

    /**
     * NOT 指令（iABC 模式）
     * 
     * R(A) := not R(B)
     *
     * @param i
     * @param vm
     */
    public static void not(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.pushBoolean(!vm.toBoolean(b));
        vm.replace(a);
    }

    /**
     * TEST 指令（iABC 模式）
     * 判断寄存器 A 中的值转换为布尔值之后是否和操作数 C 表示的布尔值一致，一致则跳过下一条指令
     * 
     * if not (R(A) <=> C) then pc++
     *
     * @param i
     * @param vm
     */
    public static void test(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int c = Instruction.getC(i);
        if (vm.toBoolean(a) != (c != 0)) {
            vm.addPC(1);
        }
    }

    /**
     * TESTSET 指令（iABC 模式）
     * 判断寄存器 B 中的值转换为布尔值之后是否和操作数 C 表示的布尔值一致，一致则把寄存器 B 中的值复制到寄存器 A
     * 
     * 用于 Lua 中的逻辑与和逻辑或
     * 
     * if (R(B) <=> C) then R(A) := R(B) else pc++
     *
     * @param i
     * @param vm
     */
    public static void testSet(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        int c = Instruction.getC(i);
        if (vm.toBoolean(b) == (c != 0)) {
            vm.copy(b, a);
        } else {
            vm.addPC(1);
        }
    }

    /* ========== 长度指令（len）========== */

    /**
     * LEN 指令（iABC 模式）
     * 
     * R(A) := length of R(B)
     *
     * @param i
     * @param vm
     */
    public static void length(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;

        // 取出 B 索引所指的值，求出其长度后推入栈顶，并替换到索引 A 的寄存器
        vm.len(b);
        vm.replace(a);
    }

    /* ========== 拼接指令（concat）========== */

    /**
     * CONCAT 指令（iABC 模式）
     * 将连续 n 个寄存器的值拼接，放入另一个寄存器
     * 
     * R(A) := R(B).. ... ..R(C)
     *
     * @param i
     * @param vm
     */
    public static void concat(int i, LuaVM vm) {

        // 目标寄存器
        int a = Instruction.getA(i) + 1;

        // 开始位置
        int b = Instruction.getB(i) + 1;

        // 结束位置
        int c = Instruction.getC(i) + 1;

        // 先检查栈是否足够满足条件（足够长），把从 B 到 C 的 n 个值推入栈顶，拼接后替换到 A 的寄存器
        int n = c - b + 1;
        vm.checkStack(n);
        for (int j = b; j <= c; j++) {
            vm.pushValue(j);
        }
        vm.concat(n);
        vm.replace(a);
    }

    /* ========== 循环指令（for）========== */

    // 循环有两种形式，数值形式（按一定步长遍历某个范围内的数值）和通用形式（遍历表）
    // 其中数值 for 需要 FORPREP 和 FORLOOP 两条指令实现

    /**
     * FORPREP 指令（iAsBx 模式）
     * 在循环开始之前预先给数值减去步长
     * 
     * R(A)-=R(A+2); pc+=sBx
     *
     * @param i
     * @param vm
     */
    public static void forPrep(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int sBx = Instruction.getSBx(i);
        // a、a + 1、a + 2 三个索引所指寄存器分别表示数值、限制和步长，将这三个值都改为数值
        if (vm.type(a) == LUA_TSTRING) {
            vm.pushNumber(vm.toNumber(a));
            vm.replace(a);
        }
        if (vm.type(a + 1) == LUA_TSTRING) {
            vm.pushNumber(vm.toNumber(a + 1));
            vm.replace(a + 1);
        }
        if (vm.type(a + 2) == LUA_TSTRING) {
            vm.pushNumber(vm.toNumber(a + 2));
            vm.replace(a + 2);
        }
        // 把“数值”、“步长”入栈，相减后替换到“数值”
        vm.pushValue(a);
        vm.pushValue(a + 2);
        vm.arith(LUA_OPSUB);
        vm.replace(a);

        // 跳转到 sBx 所指的 FORLOOP 指令
        vm.addPC(sBx);
    }

    /**
     * FORLOOP 指令（iAsBx 模式）
     * 先给数值加上步长，判断是否还在范围内，已超出范围则结束；
     * 否则把数值拷贝给用户定义的局部变量，然后跳转到循环体内部开始执行具体代码块。
     * 
     * R(A)+=R(A+2);
     * if R(A) <?= R(A+1) then {
     * pc+=sBx; R(A+3)=R(A)
     * }
     *
     * @param i
     * @param vm
     */
    public static void forLoop(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int sBx = Instruction.getSBx(i);

        // R(A)+=R(A+2);
        vm.pushValue(a + 2);
        vm.pushValue(a);
        vm.arith(LUA_OPADD);
        vm.replace(a);

        // 当步长是正/负数，则表示继续循环的条件是“数值”不大/小于“限制”
        boolean isPositiveStep = vm.toNumber(a + 2) >= 0;
        if (isPositiveStep && vm.compare(a, a + 1, LUA_OPLE) || !isPositiveStep && vm.compare(a + 1, a, LUA_OPLE)) {
            // pc+=sBx; R(A+3)=R(A)
            vm.addPC(sBx);
            vm.copy(a, a + 3);
        }
    }

    /* ========== 表指令（for）========== */

    /**
     * NEWTABLE 指令（iABC 模式）
     * 创建空表，将其放入指定寄存器。
     * 
     * R(A) := {} (size = B,C)
     *
     * @param i
     * @param vm
     */
    public static void newTable(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;

        // 数组初始容量
        int b = Instruction.getB(i);

        // 哈希表初始容量
        int c = Instruction.getC(i);

        // NEWTABLE 指令是 iABC 模式，操作数 B 和 C 只有 9bits，如果当作无符号整数使用，最大不超过 512。
        // 如果表初始容量不够大，会导致表频繁扩容影相数据加载效率。
        // 因此操作数 B 和 C 采用浮点字节编码
        vm.createTable(FPB.fb2int(b), FPB.fb2int(c));
        vm.replace(a);
    }

    /**
     * GETTABLE 指令（iABC 模式）
     * 根据键从表里取值，并放入目标寄存器中。
     * 
     * R(A) := R(B)[RK(C)]
     *
     * @param i
     * @param vm
     */
    public static void getTable(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;

        // 表索引（寄存器中）
        int b = Instruction.getB(i) + 1;

        // 键索引（寄存器或常量表中）
        int c = Instruction.getC(i);

        vm.getRK(c);
        vm.getTable(b);
        vm.replace(a);
    }

    /**
     * SETTABLE 指令（iABC 模式）
     * 根据键往表里赋值。
     * 通用指令，每次只处理一个键值对，具体操作交给表处理，不关心实际写入的是哈希表还是数组部分。
     * 
     * R(A)[RK(B)] := RK(C)
     *
     * @param i
     * @param vm
     */
    public static void setTable(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;

        // 键和值可能存在于寄存器或常量表中，索引由操作数 B、C 指定。
        int b = Instruction.getB(i);
        int c = Instruction.getC(i);
        vm.getRK(b);
        vm.getRK(c);
        vm.setTable(a);
    }

    /**
     * SETLIST 指令（iABC 模式）
     * 为数组准备，用于按索引批量设置数组元素。
     * 
     * R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B
     *
     * @param i
     * @param vm
     */
    public static void setList(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;

        // 需要写入数组的一系列值（于寄存器中，紧挨着数组）的数量。
        int b = Instruction.getB(i);

        // 数组起始索引（需要换算）。
        int c = Instruction.getC(i);

        // 如果 B 操作数为 0，表示没有要设置到数组得值。
        // 可以使用 CALL 指令留在栈顶得全部返回值。
        boolean bIsZero = b == 0;
        if (bIsZero) {
            b = ((int) vm.toInteger(-1)) - a - 1;
            vm.pop(1);
        }

        // C 操作数只有 9bits，直接用于表示数组索引不够用。
        // 因此 C 操作数表示的是批次数，批次数 * 批大小（默认 50）计算出数组起始索引，可扩展到可 50 * 2 ^ 9 == 25600。
        // 因此数组的起始索引必然是批次数 LFIELDS_PER_FLUSH 的整数倍。
        // 对于数组索引大于 25600 的情况，SETLIST 指令后跟着一条 EXTRAARG 指令，用 Ax 操作数表示批次数。

        // C 操作数大于 0 则批次数 + 1，否则真正的批次数存放在下一条指令（EXTRAARG）中。
        c = c > 0 ? c - 1 : Instruction.getAx(vm.fetch());
        vm.checkStack(1);
        int idx = c * LFIELDS_PER_FLUSH;
        for (int j = 1; j <= b; j++) {
            idx++;
            vm.pushValue(a + j);
            vm.setI(a, idx);
        }

        // 处理栈顶的值。
        if (bIsZero) {
            for (int j = vm.registerCount() + 1; j <= vm.getTop(); j++) {
                idx++;
                vm.pushValue(j);
                vm.setI(a, idx);
            }

            // clear stack
            vm.setTop(vm.registerCount());
        }
    }

    /* ========== 函数调用指令（call）========== */

    /**
     * SELF 指令（iABC 模式）
     * 把对象和方法拷贝到相邻两个目标寄存器中。
     *
     * 主要用于优化方法调用语法糖，比如 obj:f(a, b, c) => obj.f(obj, a, b, c)
     * Lua 编译器不是先去掉语法糖再按普通函数调用处理，而生成 SELF 指令，从而节省一条指令。
     *
     * R(A+1) := R(B); R(A) := R(B)[RK(C)]
     *
     * @param i
     * @param vm
     */
    public static void self(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;

        // 对象（表）索引。
        int b = Instruction.getB(i) + 1;

        // 方法名（常量表中）索引。
        int c = Instruction.getC(i);

        // 把对象复制到目标寄存器。
        vm.copy(b, a + 1);

        // 把方法名置于栈顶。
        vm.getRK(c);

        // 对象以方法名为键取值，置于栈顶，并替换到目标寄存器。
        vm.getTable(b);
        vm.replace(a);
    }

    /**
     * CLOSURE 指令（iABx 模式）
     * 把当前函数的子函数原型实例化为闭包，放入由操作数 A 指定的寄存器中。
     * 
     * R(A) := closure(KPROTO[Bx])
     *
     * @param i
     * @param vm
     */
    public static void closure(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;

        // 子函数原型索引：来自当前函数原型的子函数原型表
        int bx = Instruction.getBx(i);
        vm.loadProto(bx);
        vm.replace(a);
    }

    /**
     * VARARG 指令（iABC 模式）
     * 把传递给当前函数的变长参数加载到连续多个寄存器中。
     *
     * R(A), R(A+1), ..., R(A+B-2) = vararg
     *
     * @param i
     * @param vm
     */
    public static void vararg(int i, LuaVM vm) {
        // 第一个寄存器索引
        int a = Instruction.getA(i) + 1;

        // 寄存器数量索引
        int b = Instruction.getB(i);
        if (b != 1) { // b==0 or b>1
            vm.loadVararg(b - 1);
            popResults(a, b, vm);
        }
    }

    /**
     * TAILCALL 指令（iABC 模式）
     * 尾递归使被调用函数重用主调用函数的调用帧，缓解调用栈溢出的问题。。
     *
     * return R(A)(R(A+1), ... ,R(A+B-1))
     *
     * @param i
     * @param vm
     */
    public static void tailCall(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        // TODO: optimize tail call!
        int c = 0;
        int nArgs = pushFuncAndArgs(a, b, vm);
        vm.call(nArgs, c - 1);
        popResults(a, c, vm);
    }

    /**
     * CALL 指令（iABC 模式）
     * 调用函数，其中被调用函数、传递的参数值在寄存器中
     * 
     * R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
     *
     * @param i
     * @param vm
     */
    public static void call(int i, LuaVM vm) {

        // 被调用函数索引
        int a = Instruction.getA(i) + 1;

        // 需要传递的参数值数量索引
        int b = Instruction.getB(i);

        // 返回值数量索引
        int c = Instruction.getC(i);

        // 获取返回值数量
        int nArgs = pushFuncAndArgs(a, b, vm);
        vm.call(nArgs, c - 1);
        popResults(a, c, vm);
    }

    /**
     * RETURN 指令（iABC 模式）
     * 把存放再连续多个寄存器里的值返回给主调用函数。
     *
     * return R(A), ... ,R(A+B-2)
     *
     * @param i
     * @param vm
     */
    public static void _return(int i, LuaVM vm) {
        // 第一个寄存器索引
        int a = Instruction.getA(i) + 1;

        // 寄存器数量索引
        int b = Instruction.getB(i);

        // b == 1，不需要返回值。
        if (b == 1) {
        }
        else if (b > 1) {
            // 返回 b - 1 个返回值。
            vm.checkStack(b - 1);
            for (int j = a; j <= a + b - 2; j++) {
                vm.pushValue(j);
            }
        }
        // 一部分返回值已在栈顶。
        else {
            fixStack(a, vm);
        }
    }

    /**
     * 指定索引的函数、参数值入栈
     *
     * @param a
     * @param b
     * @param vm
     * @return
     */
    private static int pushFuncAndArgs(int a, int b, LuaVM vm) {

        // b > 0，则需要传参 b - 1 个
        if (b > 0) {
            vm.checkStack(b);
            for (int i = a; i < a + b; i++) {
                vm.pushValue(i);
            }
            return b - 1;
        }
        // b == 0
        else {
            fixStack(a, vm);
            return vm.getTop() - vm.registerCount() - 1;
        }
    }

    /**
     * 处理 b == 0 的情况
     * 后半部分参数值已经在栈顶，只需要把函数和前半部分参数推入栈顶，再旋转栈即可。
     * 
     * @param a
     * @param vm
     */
    private static void fixStack(int a, LuaVM vm) {
        int x = (int) vm.toInteger(-1);
        vm.pop(1);

        vm.checkStack(x - a);
        for (int i = a; i < x; i++) {
            vm.pushValue(i);
        }
        vm.rotate(vm.registerCount() + 1, x - a);
    }

    /**
     * 处理函数调用返回值（重新入栈）
     *
     * @param a
     * @param c
     * @param vm
     */
    private static void popResults(int a, int c, LuaVM vm) {
        
        // c == 1，没有返回值。
        if (c == 1) {
            // no results
        }
        // c > 1，则返回值 c - 1 个。
        else if (c > 1) {
            for (int i = a + c - 2; i >= a; i--) {
                vm.replace(i);
            }
        } 
        // c == 0，需要把被调用函数的返回值全部返回，先把返回值留在栈顶，并推入一个整数作为标记。
        else {
            // leave results on stack
            vm.checkStack(1);
            vm.pushInteger(a);
        }
    }
}
