package com.ywh.jua.state;

import com.ywh.jua.chunk.Prototype;

/**
 * 闭包，存放函数原型。
 *
 * @author ywh
 * @since 2020/8/20/020
 */
public class Closure {

    public Prototype getProto() {
        return proto;
    }

    Closure(Prototype proto) {
        this.proto = proto;
    }

    final Prototype proto;
}
