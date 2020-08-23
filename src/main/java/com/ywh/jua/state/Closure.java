package com.ywh.jua.state;

import com.ywh.jua.api.JavaFunction;
import com.ywh.jua.chunk.Prototype;

/**
 * 闭包，存放 Lua 函数原型或 Java 函数。
 *
 * @author ywh
 * @since 2020/8/20/020
 */
public class Closure {

    /**
     * Lua 函数原型
     */
    final Prototype proto;

    /**
     * Java 函数原型
     */
    final JavaFunction javaFunc;

    /**
     * Upvalue 列表，增加一个间接层：
     *      1. 对于某个 Upvalue，对它的任何改动都反应在其他该 Upvalue 可见的地方；
     *      2. 当嵌套函数执行时，外围函数的局部变量有可能已经退出作用域。
     * 其长度由编译器计算好。
     */
    final UpvalueHolder[] upvals;

    Closure(Prototype proto) {
        this.proto = proto;
        this.javaFunc = null;
        this.upvals = new UpvalueHolder[proto.getUpvalues().length];
    }

    Closure(JavaFunction javaFunc, int nUpvals) {
        this.proto = null;
        this.javaFunc = javaFunc;
        this.upvals = new UpvalueHolder[nUpvals];
    }


    public Prototype getProto() {
        return proto;
    }

    public JavaFunction getJavaFunc() {
        return javaFunc;
    }
}
