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

    /**
     *
     * @return
     */
    int registerCount();

    /**
     *
     * @param n
     */
    void loadVararg(int n);

    /**
     *
     * @param idx
     */
    void loadProto(int idx);

    /**
     *
     * @param a
     */
    void closeUpvalues(int a);
}
