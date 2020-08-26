package com.ywh.jua.api;


/**
 * Lua 数据类型
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public enum LuaType {

    /**
     * nil
     */
    LUA_TNIL,

    /**
     * boolean
     */
    LUA_TBOOLEAN,

    /**
     *
     */
    LUA_TLIGHTUSERDATA,

    /**
     * number
     */
    LUA_TNUMBER,

    /**
     * string
     */
    LUA_TSTRING,

    /**
     * table
     */
    LUA_TTABLE,

    /**
     * function
     */
    LUA_TFUNCTION,

    /**
     *
     */
    LUA_TUSERDATA,

    /**
     * thread
     */
    LUA_TTHREAD,

    /**
     * none（Lua 栈可以按索引取值，因此提供无效索引），-1
     */
    LUA_TNONE,
    ;

}
