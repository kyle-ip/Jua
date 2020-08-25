package com.ywh.jua.chunk;

import java.nio.ByteBuffer;

/**
 * Upvalue，即闭包内部捕获的非局部变量。
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public class Upvalue {

    /**
     *
     */
    private byte instack;

    /**
     *
     */
    private byte idx;

    public byte getInstack() {
        return instack;
    }

    public void setInstack(byte instack) {
        this.instack = instack;
    }

    public byte getIdx() {
        return idx;
    }

    public void setIdx(byte idx) {
        this.idx = idx;
    }

    void read(ByteBuffer buf) {
        instack = buf.get();
        idx = buf.get();
    }

}
