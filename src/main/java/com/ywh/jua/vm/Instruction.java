package com.ywh.jua.vm;

/**
 * 指令编码：表示存储在二进制 chunk 中的指令。
 *
 * Lua 虚拟机指令占用 4bytes，共 32bits；其中低 6bits 用于操作码、高 26bits 用于操作数；
 * 按照高 26bits 的操作码有以下分类（以 iABC 居多）：
 *      iABC:    [  B:9  ][  C:9  ][ A:8  ][OP:6]
 *      iABx:    [      Bx:18     ][ A:8  ][OP:6]
 *      iAsBx:   [     sBx:18     ][ A:8  ][OP:6]   sBx 操作数被解释成有符号整数，其余情况都是无符号整数
 *      iAx:     [           Ax:26        ][OP:6]
 *
 * 采用偏移二进制码的方式编码，比如把 sBx 解释成无符号整数时它的值为 x，那么解释成有符号整数时值为 x-K，
 * 其中 K 取最大无符号整数值的一半，即 MAXARG_S_BX
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public class Instruction {

    /**
     * 262143
     */
    public static final int MAXARG_BX = (1 << 18) - 1;

    /**
     * 131071
     */
    public static final int MAXARG_S_BX = MAXARG_BX >> 1;

    /**
     * 取操作码
     *
     * @param i
     * @return
     */
    public static OpCode getOpCode(int i) {
        return OpCode.values()[i & 0x3F];
    }

    /**
     * 取 A：右移 6 位（越过 OP）
     * @param i
     * @return
     */
    public static int getA(int i) {
        return (i >> 6) & 0xFF;
    }

    /**
     * 取 C：右移 14 位（越过 OP、A）
     *
      * @param i
     * @return
     */
    public static int getC(int i) {
        return (i >> 14) & 0x1FF;
    }

    /**
     * 取 B：右移 14 位（越过 OP、A、C）
     *
     * @param i
     * @return
     */
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
        return getBx(i) - MAXARG_S_BX;
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
