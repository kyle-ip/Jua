package com.ywh.jua.vm;


import static com.ywh.jua.vm.OpArgMask.*;
import static com.ywh.jua.vm.OpMode.*;

/**
 * 操作码：用于识别指令，由于用 6bits 表示，因此最多共 64 条（Lua 5.3 定义了 0 ~ 46）。
 * 指令表：Lua 官方实现把每一条指令的基本信息（编码模式、是否设置寄存器 A、操作数 B 和 C 的使用类型等）编码为一个字节。
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public enum OpCode {

    /*       T  A    B       C     mode */

    /**
     * R(A) := R(B)
     */
    MOVE(0, 1, OpArgR, OpArgN, iABC),

    /**
     * R(A) := Kst(Bx)
     */
    LOADK(0, 1, OpArgK, OpArgN, iABx),

    /**
     * R(A) := Kst(extra arg)
     */
    LOADKX(0, 1, OpArgN, OpArgN, iABx),

    /**
     * R(A) := (bool)B; if (C) pc++
     */
    LOADBOOL(0, 1, OpArgU, OpArgU, iABC),

    /**
     * R(A), R(A+1), ..., R(A+B) := nil
     */
    LOADNIL(0, 1, OpArgU, OpArgN, iABC),

    /**
     * R(A) := UpValue[B]
     */
    GETUPVAL(0, 1, OpArgU, OpArgN, iABC),

    /**
     * R(A) := UpValue[B][RK(C)]
     */
    GETTABUP(0, 1, OpArgU, OpArgK, iABC),

    /**
     * R(A) := R(B)[RK(C)]
     */
    GETTABLE(0, 1, OpArgR, OpArgK, iABC),

    /**
     * UpValue[A][RK(B)] := RK(C)
     */
    SETTABUP(0, 0, OpArgK, OpArgK, iABC),

    /**
     * UpValue[B] := R(A)
     */
    SETUPVAL(0, 0, OpArgU, OpArgN, iABC),

    /**
     * R(A)[RK(B)] := RK(C)
     */
    SETTABLE(0, 0, OpArgK, OpArgK, iABC),

    /**
     * R(A) := {} (size = B,C)
     */
    NEWTABLE(0, 1, OpArgU, OpArgU, iABC),

    /**
     * R(A+1) := R(B); R(A) := R(B)[RK(C)]
     */
    SELF(0, 1, OpArgR, OpArgK, iABC),

    /**
     * R(A) := RK(B) + RK(C)
     */
    ADD(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := RK(B) - RK(C)
     */
    SUB(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := RK(B) * RK(C)
     */
    MUL(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := RK(B) % RK(C)
     */
    MOD(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := RK(B) ^ RK(C)
     */
    POW(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := RK(B) / RK(C)
     */
    DIV(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := RK(B) // RK(C)
     */
    IDIV(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := RK(B) & RK(C)
     */
    BAND(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := RK(B) | RK(C)
     */
    BOR(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := RK(B) ~ RK(C)
     */
    BXOR(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := RK(B) << RK(C)
     */
    SHL(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := RK(B) >> RK(C)
     */
    SHR(0, 1, OpArgK, OpArgK, iABC),

    /**
     * R(A) := -R(B)
     */
    UNM(0, 1, OpArgR, OpArgN, iABC),

    /**
     * R(A) := ~R(B)
     */
    BNOT(0, 1, OpArgR, OpArgN, iABC),

    /**
     * R(A) := not R(B)
     */
    NOT(0, 1, OpArgR, OpArgN, iABC),

    /**
     * R(A) := length of R(B)
     */
    LEN(0, 1, OpArgR, OpArgN, iABC),

    /**
     * R(A) := R(B).. ... ..R(C)
     */
    CONCAT(0, 1, OpArgR, OpArgR, iABC),

    /**
     * pc+=sBx; if (A) close all upvalues >= R(A - 1)
     */
    JMP(0, 0, OpArgR, OpArgN, iAsBx),

    /**
     * if ((RK(B) == RK(C)) ~= A) then pc++
     */
    EQ(1, 0, OpArgK, OpArgK, iABC),

    /**
     * if ((RK(B) <  RK(C)) ~= A) then pc++
     */
    LT(1, 0, OpArgK, OpArgK, iABC),

    /**
     * if ((RK(B) <= RK(C)) ~= A) then pc++
     */
    LE(1, 0, OpArgK, OpArgK, iABC),

    /**
     * if not (R(A) <=> C) then pc++
     */
    TEST(1, 0, OpArgN, OpArgU, iABC),

    /**
     * if (R(B) <=> C) then R(A) := R(B) else pc++
     */
    TESTSET(1, 1, OpArgR, OpArgU, iABC),

    /**
     * R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
     */
    CALL(0, 1, OpArgU, OpArgU, iABC),

    /**
     * return R(A)(R(A+1), ... ,R(A+B-1))
     */
    TAILCALL(0, 1, OpArgU, OpArgU, iABC),

    /**
     * return R(A), ... ,R(A+B-2)
     */
    RETURN(0, 0, OpArgU, OpArgN, iABC),

    /**
     * R(A)+=R(A+2); if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }
     */
    FORLOOP(0, 1, OpArgR, OpArgN, iAsBx),

    /**
     * R(A)-=R(A+2); pc+=sBx
     */
    FORPREP(0, 1, OpArgR, OpArgN, iAsBx),

    /**
     * R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2));
     */
    TFORCALL(0, 0, OpArgN, OpArgU, iABC),

    /**
     * if R(A+1) ~= nil then { R(A)=R(A+1); pc += sBx }
     */
    TFORLOOP(0, 1, OpArgR, OpArgN, iAsBx),

    /**
     * R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B
     */
    SETLIST(0, 0, OpArgU, OpArgU, iABC),

    /**
     * R(A) := closure(KPROTO[Bx])
     */
    CLOSURE(0, 1, OpArgU, OpArgN, iABx),

    /**
     * R(A), R(A+1), ..., R(A+B-2) = vararg
     */
    VARARG(0, 1, OpArgU, OpArgN, iABC),

    /**
     * extra (larger) argument for previous opcode
     */
    EXTRAARG(0, 0, OpArgU, OpArgU, iAx),
    ;

    private final int testFlag; // operator is a test (next instruction must be a jump)

    private final int setAFlag; // instruction set register A

    private final OpArgMask argBMode; // B arg mode

    private final OpArgMask argCMode; // C arg mode

    private final OpMode opMode; // op mode

    public int getTestFlag() {
        return testFlag;
    }

    public int getSetAFlag() {
        return setAFlag;
    }

    public OpArgMask getArgBMode() {
        return argBMode;
    }

    public OpArgMask getArgCMode() {
        return argCMode;
    }

    public OpMode getOpMode() {
        return opMode;
    }

    OpCode(int testFlag, int setAFlag, OpArgMask argBMode, OpArgMask argCMode, OpMode opMode) {
        this.testFlag = testFlag;
        this.setAFlag = setAFlag;
        this.argBMode = argBMode;
        this.argCMode = argCMode;
        this.opMode = opMode;
    }

}
