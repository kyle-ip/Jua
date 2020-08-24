package com.ywh.jua.api;

/**
 * 线程状态
 *
 * @author ywh
 * @since 2020/8/20/020
 */
public enum ThreadStatus {

    /**
     * 执行成功
     */
    LUA_OK,

    LUA_YIELD,

    LUA_ERRRUN,

    LUA_ERRSYNTAX,

    LUA_ERRMEM,

    LUA_ERRGCMM,

    LUA_ERRERR,

    LUA_ERRFILE,
    ;

}