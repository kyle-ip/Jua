package com.ywh.jua.compiler.codegen;

import com.ywh.jua.compiler.ast.exps.FuncDefExp;
import com.ywh.jua.compiler.lexer.TokenKind;
import com.ywh.jua.vm.FPB;
import com.ywh.jua.vm.OpCode;

import java.util.*;

import static com.ywh.jua.compiler.lexer.TokenKind.*;
import static com.ywh.jua.vm.Instruction.MAXARG_sBx;

/**
 * 函数内部结构
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
class FuncInfo {

    private static final Map<TokenKind, OpCode> ARITH_AND_BITWISE_BINOPS = new HashMap<>();

    static {
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_ADD, OpCode.ADD);
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_SUB, OpCode.SUB);
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_MUL, OpCode.MUL);
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_MOD, OpCode.MOD);
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_POW, OpCode.POW);
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_DIV, OpCode.DIV);
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_IDIV, OpCode.IDIV);
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_BAND, OpCode.BAND);
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_BOR, OpCode.BOR);
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_BXOR, OpCode.BXOR);
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_SHL, OpCode.SHL);
        ARITH_AND_BITWISE_BINOPS.put(TOKEN_OP_SHR, OpCode.SHR);
    }

    /**
     * Upvalue
     */
    static class UpvalInfo {
        /**
         * 该局部变量所占用的寄存器索引（Upvalue 捕获直接外围函数的局部变量）。
         */
        int locVarSlot;

        /**
         * 该 Upvalue 再直接外围函数 Upvalue 表中的索引。
         */
        int upvalIndex;

        /**
         *  Upvalue 再函数中出现的顺序。
         */
        int index;
    }

    /**
     * 局部变量
     * 每个局部变量都会占用一个寄存器索引。
     * 块结束以后作用域随之消失，局部变量也不存在，因此寄存器会被回收。
     * 同一个局部变量名可以先后绑定不同的寄存器，因此可用，使用单向链表串联同名的局部变量。
     * function()
     *      local a, b = 1, 2; print(a, b)          --> 1 2
     *      local a, b = 3, 4; print(a, b)          --> 3 4
     *      do
     *          print(a, b)                         --> 3 4
     *          local a, b = 5, 6; print(a, b)      --> 5 6
     *      end
     *      print(a, b)                             --> 3 4
     * end
     */
    static class LocVarInfo {
        /**
         * 单向链表，指向上一层
         */
        LocVarInfo prev;

        /**
         * 局部变量名
         */
        String name;

        /**
         * 局部变量所在作用域层次
         * 从 0 开始，没进入一个作用域 +1，
         */
        int scopeLv;

        /**
         * 与局部变量名绑定的寄存器索引
         */
        int slot;

        int startPC;

        int endPC;

        /**
         * 是否被闭包捕获
         */
        boolean captured;
    }

    /**
     * 父函数信息
     */
    private FuncInfo parent;

    /**
     * 子函数信息
     */
    List<FuncInfo> subFuncs = new ArrayList<>();

    // ========== 寄存器：每个局部变量和临时变量都需要分配寄存器，退出作用域、使用完毕后回收。 ==========

    /**
     * 使用寄存器数量
     */
    int usedRegs;

    /**
     * 最大寄存器数量
     */
    int maxRegs;

    private int scopeLv;

    /**
     * 局部变量表（按顺序记录函数内部声明的局部变量）
     */
    List<LocVarInfo> locVars = new ArrayList<>();

    /**
     * 局部变量集合（记录当前生效的局部变量）
     */
    private Map<String, LocVarInfo> locNames = new HashMap<>();

    /**
     * Upvalue 表
     * 闭包按照词法作用域捕获的外围函数中的局部变量，因此需要把 Upvalue 名和外围函数的局部变量绑定。
     */
    Map<String, UpvalInfo> upvalues = new HashMap<>();

    /**
     * 常量表，存放函数体内出现的 nil、布尔、数字或者字符串字面量。
     */
    Map<Object, Integer> constants = new HashMap<>();

    /**
     * break 表
     * for、repeat 和 while 循环语句块内部，可以使用 break 打破循环。
     * 问题在于 break 语句可能在更深层次的块中，所以需要穿透块找到离 break 语句最近的 for、repeat 或者 while 块；
     * 而且 break 语句使用跳转指令实现，但是在处理 break 语句时，块可能还未结束，所以跳转的目标地址还不确定。
     * 为了解决以上问题，需要把跳转指令的地址记录在对应的 for、repeat 或者 while 块里，在块技术时再修复跳转的目标地址。
     */
    private List<List<Integer>> breaks = new ArrayList<>();

    /**
     * 字节码
     */
    List<Integer> insts = new ArrayList<>();

    List<Integer> lineNums = new ArrayList<>();

    int line;

    int lastLine;

    int numParams;

    /**
     * 是否变长参数
     */
    boolean isVararg;

    FuncInfo(FuncInfo parent, FuncDefExp fd) {
        this.parent = parent;
        line = fd.getLine();
        lastLine = fd.getLastLine();
        numParams = fd.getParList() != null ? fd.getParList().size() : 0;
        isVararg = fd.isVararg();
        breaks.add(null);
    }

    /* constants */

    int indexOfConstant(Object k) {
        Integer idx = constants.get(k);
        if (idx != null) {
            return idx;
        }

        idx = constants.size();
        constants.put(k, idx);
        return idx;
    }

    /* registers */

    /**
     * 分配寄存器
     *
     * @return
     */
    int allocReg() {
        usedRegs++;
        if (usedRegs >= 255) {
            throw new RuntimeException("function or expression needs too many registers");
        }
        // 扩展寄存器数量
        if (usedRegs > maxRegs) {
            maxRegs = usedRegs;
        }
        // 返回寄存器索引（从 0 开始，不超过 255）
        return usedRegs - 1;
    }

    /**
     * 回收寄存器
     */
    void freeReg() {
        if (usedRegs <= 0) {
            throw new RuntimeException("usedRegs <= 0 !");
        }
        usedRegs--;
    }

    /**
     * 分配寄存器（多个）
     *
     * @param n
     * @return
     */
    int allocRegs(int n) {
        if (n <= 0) {
            throw new RuntimeException("n <= 0 !");
        }
        for (int i = 0; i < n; i++) {
            allocReg();
        }
        return usedRegs - n;
    }

    /**
     * 回收寄存器（多个）
     *
     * @param n
     * @return
     */
    void freeRegs(int n) {
        if (n < 0) {
            throw new RuntimeException("n < 0 !");
        }
        for (int i = 0; i < n; i++) {
            freeReg();
        }
    }

    /* lexical scope */

    /**
     * 局部变量进入新作用域
     *
     * @param breakable
     */
    void enterScope(boolean breakable) {
        scopeLv++;
        // 循环块
        if (breakable) {
            breaks.add(new ArrayList<>());
        }
        // 非循坏块
        else {
            breaks.add(null);
        }
    }

    /**
     * 局部变量离开当前作用域
     *
     * @param endPC
     */
    void exitScope(int endPC) {
        List<Integer> pendingBreakJmps = breaks.remove(breaks.size() - 1);

        // 退出作用域时修复跳转指令。
        if (pendingBreakJmps != null) {
            int a = getJmpArgA();
            for (int pc : pendingBreakJmps) {
                int sBx = pc() - pc;
                int i = (sBx + MAXARG_sBx) << 14 | a << 6 | OpCode.JMP.ordinal();
                insts.set(pc, i);
            }
        }

        scopeLv--;
        for (LocVarInfo locVar : new ArrayList<>(locNames.values())) {
            // out of scope
            if (locVar.scopeLv > scopeLv) {
                locVar.endPC = endPC;
                removeLocVar(locVar);
            }
        }
    }

    /**
     * 递归删除作用域内的局部变量（当局部变量退出作用域时）
     *
     * @param locVar
     */
    private void removeLocVar(LocVarInfo locVar) {
        freeReg();
        if (locVar.prev == null) {
            // 解绑局部变量名
            locNames.remove(locVar.name);
        } else if (locVar.prev.scopeLv == locVar.scopeLv) {
            removeLocVar(locVar.prev);
        } else {
            locNames.put(locVar.name, locVar.prev);
        }
    }

    /**
     * 在当前作用域添加一个局部变量，返回其分配的寄存器索引。
     *
     * @param name
     * @param startPC
     * @return
     */
    int addLocVar(String name, int startPC) {
        LocVarInfo newVar = new LocVarInfo();
        newVar.name = name;
        newVar.prev = locNames.get(name);
        newVar.scopeLv = scopeLv;
        newVar.slot = allocReg();
        newVar.startPC = startPC;
        newVar.endPC = 0;

        locVars.add(newVar);
        locNames.put(name, newVar);

        return newVar.slot;
    }

    /**
     * 检查局部变量名是否已经和某个寄存器绑定，是则返回寄存器索引，否则返回 -1。
     *
     * @param name
     * @return
     */
    int slotOfLocVar(String name) {
        return locNames.containsKey(name) ? locNames.get(name).slot : -1;
    }

    /**
     * 把 break 语句对应的跳转指令添加到最近的循环块中，如果找不到循环块则报错。
     *
     * @param pc
     */
    void addBreakJmp(int pc) {
        for (int i = scopeLv; i >= 0; i--) {

            // breakable
            if (breaks.get(i) != null) {
                breaks.get(i).add(pc);
                return;
            }
        }

        throw new RuntimeException("<break> at line ? not inside a loop!");
    }

    /* upvalues */

    /**
     * 判断名字是否已经与 Upvalue 绑定，是则返回 Upvalue 索引，否则尝试绑定然后返回索引，绑定失败返回 -1。
     *
     * @param name
     * @return
     */
    int indexOfUpval(String name) {
        if (upvalues.containsKey(name)) {
            return upvalues.get(name).index;
        }
        if (parent != null) {
            if (parent.locNames.containsKey(name)) {
                LocVarInfo locVar = parent.locNames.get(name);
                int idx = upvalues.size();
                UpvalInfo upval = new UpvalInfo();
                upval.locVarSlot = locVar.slot;
                upval.upvalIndex = -1;
                upval.index = idx;
                upvalues.put(name, upval);
                locVar.captured = true;
                return idx;
            }
            int uvIdx = parent.indexOfUpval(name);
            if (uvIdx >= 0) {
                int idx = upvalues.size();
                UpvalInfo upval = new UpvalInfo();
                upval.locVarSlot = -1;
                upval.upvalIndex = uvIdx;
                upval.index = idx;
                upvalues.put(name, upval);
                return idx;
            }
        }
        return -1;
    }

    /**
     * 闭合 Upvalue
     *
     * @param line
     */
    void closeOpenUpvals(int line) {
        int a = getJmpArgA();
        if (a > 0) {
            emitJmp(line, a, 0);
        }
    }

    /**
     * 获取 JMP 指令的操作数 A
     *
     * @return
     */
    int getJmpArgA() {
        boolean hasCapturedLocVars = false;
        int minSlotOfLocVars = maxRegs;
        for (LocVarInfo locVar : locNames.values()) {
            if (locVar.scopeLv == scopeLv) {
                for (LocVarInfo v = locVar; v != null && v.scopeLv == scopeLv; v = v.prev) {
                    if (v.captured) {
                        hasCapturedLocVars = true;
                    }
                    if (v.slot < minSlotOfLocVars && v.name.charAt(0) != '(') {
                        minSlotOfLocVars = v.slot;
                    }
                }
            }
        }
        if (hasCapturedLocVars) {
            return minSlotOfLocVars + 1;
        } else {
            return 0;
        }
    }

    /* code */

    /**
     * 取程序计数器
     *
     * @return
     */
    int pc() {
        return insts.size() - 1;
    }

    /**
     *
     * @param pc
     * @param sBx
     */
    void fixSbx(int pc, int sBx) {
        int i = insts.get(pc);
        i = i << 18 >> 18;                  // clear sBx
        i = i | (sBx + MAXARG_sBx) << 14; // reset sBx
        insts.set(pc, i);
    }

    // TODO: rename?
    void fixEndPC(String name, int delta) {
        for (int i = locVars.size() - 1; i >= 0; i--) {
            LocVarInfo locVar = locVars.get(i);
            if (locVar.name.equals(name)) {
                locVar.endPC += delta;
                return;
            }
        }
    }

    // ========== 生成指令 ==========

    /**
     * 生成 iABC 指令
     * @param line
     * @param opcode
     * @param a
     * @param b
     * @param c
     */
    void emitABC(int line, OpCode opcode, int a, int b, int c) {
        int i = b << 23 | c << 14 | a << 6 | opcode.ordinal();
        insts.add(i);
        lineNums.add(line);
    }

    /**
     * 生成 iABx 指令
     *
     * @param line
     * @param opcode
     * @param a
     * @param bx
     */
    private void emitABx(int line, OpCode opcode, int a, int bx) {
        int i = bx << 14 | a << 6 | opcode.ordinal();
        insts.add(i);
        lineNums.add(line);
    }

    /**
     * 生成 iAsBx 指令
     *
     * @param line
     * @param opcode
     * @param a
     * @param sBx
     */
    private void emitAsBx(int line, OpCode opcode, int a, int sBx) {
        int i = (sBx + MAXARG_sBx) << 14 | a << 6 | opcode.ordinal();
        insts.add(i);
        lineNums.add(line);
    }

    /**
     * 生成 iAx 指令
     *
     * @param line
     * @param opcode
     * @param ax
     */
    private void emitAx(int line, OpCode opcode, int ax) {
        int i = ax << 6 | opcode.ordinal();
        insts.add(i);
        lineNums.add(line);
    }

    /**
     * r[a] = r[b]
     *
     * @param line
     * @param a
     * @param b
     */
    void emitMove(int line, int a, int b) {
        emitABC(line, OpCode.MOVE, a, b, 0);
    }

    /**
     * r[a], r[a+1], ..., r[a+b] = nil
     *
     * @param line
     * @param a
     * @param n
     */
    void emitLoadNil(int line, int a, int n) {
        emitABC(line, OpCode.LOADNIL, a, n - 1, 0);
    }

    /**
     * r[a] = (bool)b; if (c) pc++
     *
     * @param line
     * @param a
     * @param b
     * @param c
     */
    void emitLoadBool(int line, int a, int b, int c) {
        emitABC(line, OpCode.LOADBOOL, a, b, c);
    }

    /**
     * r[a] = kst[bx]
     *
     * @param line
     * @param a
     * @param k
     */
    void emitLoadK(int line, int a, Object k) {
        int idx = indexOfConstant(k);
        if (idx < (1 << 18)) {
            emitABx(line, OpCode.LOADK, a, idx);
        } else {
            emitABx(line, OpCode.LOADKX, a, 0);
            emitAx(line, OpCode.EXTRAARG, idx);
        }
    }

    /**
     * r[a], r[a+1], ..., r[a+b-2] = vararg
     *
     * @param line
     * @param a
     * @param n
     */
    void emitVararg(int line, int a, int n) {
        emitABC(line, OpCode.VARARG, a, n + 1, 0);
    }

    /**
     * r[a] = emitClosure(proto[bx])
     *
     * @param line
     * @param a
     * @param bx
     */
    void emitClosure(int line, int a, int bx) {
        emitABx(line, OpCode.CLOSURE, a, bx);
    }

    /**
     * r[a] = {}
     *
     * @param line
     * @param a
     * @param nArr
     * @param nRec
     */
    void emitNewTable(int line, int a, int nArr, int nRec) {
        emitABC(line, OpCode.NEWTABLE,
            a, FPB.int2fb(nArr), FPB.int2fb(nRec));
    }

    /**
     * r[a][(c-1)*FPF+i] = r[a+i], 1 <= i <= b
     *
     * @param line
     * @param a
     * @param b
     * @param c
     */
    void emitSetList(int line, int a, int b, int c) {
        emitABC(line, OpCode.SETLIST, a, b, c);
    }

    // r[a] = r[b][rk(c)]
    void emitGetTable(int line, int a, int b, int c) {
        emitABC(line, OpCode.GETTABLE, a, b, c);
    }

    // r[a][rk(b)] = rk(c)
    void emitSetTable(int line, int a, int b, int c) {
        emitABC(line, OpCode.SETTABLE, a, b, c);
    }

    // r[a] = upval[b]
    void emitGetUpval(int line, int a, int b) {
        emitABC(line, OpCode.GETUPVAL, a, b, 0);
    }

    // upval[b] = r[a]
    void emitSetUpval(int line, int a, int b) {
        emitABC(line, OpCode.SETUPVAL, a, b, 0);
    }

    // r[a] = upval[b][rk(c)]
    void emitGetTabUp(int line, int a, int b, int c) {
        emitABC(line, OpCode.GETTABUP, a, b, c);
    }

    // upval[a][rk(b)] = rk(c)
    void emitSetTabUp(int line, int a, int b, int c) {
        emitABC(line, OpCode.SETTABUP, a, b, c);
    }

    // r[a], ..., r[a+c-2] = r[a](r[a+1], ..., r[a+b-1])
    void emitCall(int line, int a, int nArgs, int nRet) {
        emitABC(line, OpCode.CALL, a, nArgs + 1, nRet + 1);
    }

    // return r[a](r[a+1], ... ,r[a+b-1])
    void emitTailCall(int line, int a, int nArgs) {
        emitABC(line, OpCode.TAILCALL, a, nArgs + 1, 0);
    }

    /**
     * return r[a], ... ,r[a+b-2]
     *
     * @param line
     * @param a
     * @param n
     */
    void emitReturn(int line, int a, int n) {
        emitABC(line, OpCode.RETURN, a, n + 1, 0);
    }

    /**
     * r[a+1] = r[b]; r[a] = r[b][rk(c)]
     *
     * @param line
     * @param a
     * @param b
     * @param c
     */
    void emitSelf(int line, int a, int b, int c) {
        emitABC(line, OpCode.SELF, a, b, c);
    }

    /**
     * pc+=sBx; if (a) close all upvalues >= r[a - 1]
     *
     * @param line
     * @param a
     * @param sBx
     * @return
     */
    int emitJmp(int line, int a, int sBx) {
        emitAsBx(line, OpCode.JMP, a, sBx);
        return insts.size() - 1;
    }

    /**
     * if not (r[a] <=> c) then pc++
     *
     * @param line
     * @param a
     * @param c
     */
    void emitTest(int line, int a, int c) {
        emitABC(line, OpCode.TEST, a, 0, c);
    }

    /**
     * if (r[b] <=> c) then r[a] = r[b] else pc++
     *
     * @param line
     * @param a
     * @param b
     * @param c
     */
    void emitTestSet(int line, int a, int b, int c) {
        emitABC(line, OpCode.TESTSET, a, b, c);
    }

    /**
     *
     * @param line
     * @param a
     * @param sBx
     * @return
     */
    int emitForPrep(int line, int a, int sBx) {
        emitAsBx(line, OpCode.FORPREP, a, sBx);
        return insts.size() - 1;
    }

    /**
     * r[a] = op r[b]
     *
     * @param line
     * @param a
     * @param sBx
     * @return
     */
    int emitForLoop(int line, int a, int sBx) {
        emitAsBx(line, OpCode.FORLOOP, a, sBx);
        return insts.size() - 1;
    }

    /**
     * r[a] = op r[b]
     *
     * @param line
     * @param a
     * @param c
     */
    void emitTForCall(int line, int a, int c) {
        emitABC(line, OpCode.TFORCALL, a, 0, c);
    }

    /**
     * r[a] = op r[b]
     *
     * @param line
     * @param a
     * @param sBx
     */
    void emitTForLoop(int line, int a, int sBx) {
        emitAsBx(line, OpCode.TFORLOOP, a, sBx);
    }

    /**
     * r[a] = op r[b]
     *
     * @param line
     * @param op
     * @param a
     * @param b
     */
    void emitUnaryOp(int line, TokenKind op, int a, int b) {
        switch (op) {
            case TOKEN_OP_NOT:
                emitABC(line, OpCode.NOT, a, b, 0);
                break;
            case TOKEN_OP_BNOT:
                emitABC(line, OpCode.BNOT, a, b, 0);
                break;
            case TOKEN_OP_LEN:
                emitABC(line, OpCode.LEN, a, b, 0);
                break;
            case TOKEN_OP_UNM:
                emitABC(line, OpCode.UNM, a, b, 0);
                break;
            default:
                break;
        }
    }

    /**
     * r[a] = rk[b] op rk[c]
     * arith & bitwise & relational
     *
     * @param line
     * @param op
     * @param a
     * @param b
     * @param c
     */
    void emitBinaryOp(int line, TokenKind op, int a, int b, int c) {
        if (ARITH_AND_BITWISE_BINOPS.containsKey(op)) {
            emitABC(line, ARITH_AND_BITWISE_BINOPS.get(op), a, b, c);
        } else {
            switch (op) {
                case TOKEN_OP_EQ:
                    emitABC(line, OpCode.EQ, 1, b, c);
                    break;
                case TOKEN_OP_NE:
                    emitABC(line, OpCode.EQ, 0, b, c);
                    break;
                case TOKEN_OP_LT:
                    emitABC(line, OpCode.LT, 1, b, c);
                    break;
                case TOKEN_OP_GT:
                    emitABC(line, OpCode.LT, 1, c, b);
                    break;
                case TOKEN_OP_LE:
                    emitABC(line, OpCode.LE, 1, b, c);
                    break;
                case TOKEN_OP_GE:
                    emitABC(line, OpCode.LE, 1, c, b);
                    break;
            }
            emitJmp(line, 0, 1);
            emitLoadBool(line, a, 0, 1);
            emitLoadBool(line, a, 1, 0);
        }
    }

}
