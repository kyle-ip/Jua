package com.ywh.jua.vm;

/**
 * 指令编码：表示存储在二进制 chunk 中的指令。
 *
 * Lua 虚拟机采用偏移二进制码的方式编码，比如把 sBx 解释成无符号整数时它的值为 x，那么解释成有符号整数时值为 x-K，
 * 其中 K 取最大无符号整数值的一半，即 MAXARG_sBx
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public class Instruction {

    /**
     * 262143
     */
    public static final int MAXARG_Bx = (1 << 18) - 1;

    /**
     * 131071
     */
    public static final int MAXARG_sBx = MAXARG_Bx >> 1;

    /**
     * 取操作码
     *
     * @param i
     * @return
     */
    public static OpCode getOpCode(int i) {
        return OpCode.values()[i & 0x3F];
    }

    public static int getA(int i) {
        return (i >> 6) & 0xFF;
    }

    public static int getC(int i) {
        return (i >> 14) & 0x1FF;
    }

    public static int getB(int i) {
        return (i >> 23) & 0x1FF;
    }

    /**
     * 获取 Bx，表示范围 [0, 262143]
     * @param i
     * @return
     */
    public static int getBx(int i) {
        return i >>> 14;
    }

    /**
     * 获取 sBx，表示范围 [-131071, 131072]
     *
     * @param i
     * @return
     */
    public static int getSBx(int i) {
        return getBx(i) - MAXARG_sBx;
    }

    /**
     * 获取 Ax
     * @param i
     * @return
     */
    public static int getAx(int i) {
        return i >>> 6;
    }

}
