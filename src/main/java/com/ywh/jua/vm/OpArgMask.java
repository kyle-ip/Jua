package com.ywh.jua.vm;

/**
 * 操作数类型
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public enum OpArgMask {

    /**
     * 不表示任何任何信息，不会被使用（argument is not used）
     * 比如 MOVC 指令（iABC 模式），只使用 A 和 B 操作数，不使用 C 操作数
     */
    OpArgN,

    /**
     * 操作数表示布尔值、整数值、upvalue 索引、子函数时（argument is used）
     */
    OpArgU,

    /**
     * iABC 模式下表示寄存器索引或 iAsBx 模式下表示跳转偏移（argument is a register or a jump offset）
     */
    OpArgR,

    /**
     * 常量表索引或寄存器索引（argument is a constant or register/constant）
     */
    OpArgK,
    ;

}
