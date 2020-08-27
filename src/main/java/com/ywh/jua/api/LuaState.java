package com.ywh.jua.api;

/**
 * Lua（解释器）状态：Lua API 体现为一系列操作 LuaState 结构的函数。
 * 主要包括基础栈操作方法、栈访问方法、压栈方法三类。
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public interface LuaState extends LuaBasicAPI, LuaAuxLib {

    /**
     * Lua 栈初始深度
     */
    int LUA_MINSTACK = 20;

    /**
     * Lua 栈最大深度
     */
    int LUAI_MAXSTACK = 1_000_000;

    /**
     * 伪索引，用于操作注册表和 Upvalue。
     * 一般不需要 Lua 有很大的容量，所以定义常量 {@link #LUAI_MAXSTACK} 用于表示 Lua 的最大索引；
     * 由于索引也可能是负数，所以正负 {@link #LUAI_MAXSTACK} 就是有效索引的最大值和最小值，负值再 -1000 即表示注册表的伪索引。
     *
     * LUA_REGISTRYINDEX < min valid index < 0 < max valid index
     *
     * 小于等于该值表示伪索引。
     */
    int LUA_REGISTRYINDEX = -LUAI_MAXSTACK - 1_000;

    /**
     * 全局环境在注册表的索引
     */
    long LUA_RIDX_GLOBALS = 2;

    int LUA_MULTRET = -1;

    int LUA_RIDX_MAINTHREAD = 1;


}
