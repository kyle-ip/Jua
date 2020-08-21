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

    final Prototype proto;

    final JavaFunction javaFunc;

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
