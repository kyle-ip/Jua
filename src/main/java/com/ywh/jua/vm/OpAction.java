package com.ywh.jua.vm;

import com.ywh.jua.api.LuaVM;

/**
 * 指令执行
 */
@FunctionalInterface
public interface OpAction {

    void execute(int i, LuaVM vm);

}
