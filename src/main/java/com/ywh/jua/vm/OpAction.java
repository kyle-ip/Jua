package com.ywh.jua.vm;

import com.ywh.jua.api.LuaVM;

/**
 * 指令执行
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
@FunctionalInterface
public interface OpAction {

    /**
     * 执行指令
     *
     * @param i
     * @param vm
     */
    void execute(int i, LuaVM vm);

}
