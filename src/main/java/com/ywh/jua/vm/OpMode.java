package com.ywh.jua.vm;

/**
 * 编码模式：Lua 虚拟机指令占用 4bytes，共 32bits；其中低 6bits 用于操作码、高 26bits 用于操作数；
 * 按照高 26bits 的操作码有以下分类（以 iABC 居多）：
 *      iABC:    [  B:9  ][  C:9  ][ A:8  ][OP:6]
 *      iABx:    [      Bx:18     ][ A:8  ][OP:6]
 *      iAsBx:   [     sBx:18     ][ A:8  ][OP:6]   sBx 操作数被解释成有符号整数，其余情况都是无符号整数
 *      iAx:     [           Ax:26        ][OP:6]
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public enum OpMode {

    /**
     *
     */
    iABC,
    iABx,
    iAsBx,
    iAx,
    ;

}
