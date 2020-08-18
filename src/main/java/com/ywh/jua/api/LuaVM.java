package com.ywh.jua.api;


/**
 * Lua 虚拟机
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public interface LuaVM extends LuaState {

    /**
     *
     * @return
     */
    int getPC();

    /**
     *
     * @param n
     */
    void addPC(int n);

    /**
     *
     * @return
     */
    int fetch();

    /**
     *
     * @param idx
     */
    void getConst(int idx);

    /**
     *
     * @param rk
     */
    void getRK(int rk);

}
