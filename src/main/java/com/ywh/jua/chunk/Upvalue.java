package com.ywh.jua.chunk;

import java.nio.ByteBuffer;

/**
 * Upvalue
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public class Upvalue {

    private byte instack;

    private byte idx;

    public byte getInstack() {
        return instack;
    }

    public byte getIdx() {
        return idx;
    }

    void read(ByteBuffer buf) {
        instack = buf.get();
        idx = buf.get();
    }

}
