package com.ywh.jua.api;


/**
 * Lua 算数、按位运算符
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public enum ArithOp {

    /**
     * +
     */
    LUA_OPADD,

    /**
     * -
     */
    LUA_OPSUB,

    /**
     * *
     */
    LUA_OPMUL,

    /**
     * %
     */
    LUA_OPMOD,

    /**
     * ^
     */
    LUA_OPPOW,

    /**
     * /
     */
    LUA_OPDIV,

    /**
     * //
     */
    LUA_OPIDIV,

    /**
     * &
     */
    LUA_OPBAND,

    /**
     * |
     */
    LUA_OPBOR,

    /**
     * ~
     */
    LUA_OPBXOR,

    /**
     * <<
     */
    LUA_OPSHL,

    /**
     * >>
     */
    LUA_OPSHR,

    /**
     * -
     */
    LUA_OPUNM,

    /**
     * ~
     */
    LUA_OPBNOT,
    ;

}
