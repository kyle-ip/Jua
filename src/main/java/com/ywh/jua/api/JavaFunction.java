package com.ywh.jua.api;

/**
 * Java API
 * Java 函数需要满足签名：接收要给 {@link LuaState} 接口类型的参数，返回一个整数。
 * 执行之前，Lua 栈里是传入的参数值；执行结束，把需要返回的值留在栈顶，返回一个整数表示返回值个数。
 *
 * @author ywh
 * @since 2020/8/21 11:26
 */
@FunctionalInterface
public interface JavaFunction {

    int invoke(LuaState ls);

}