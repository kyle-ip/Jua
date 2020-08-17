package com.ywh.jua.chunk;

import java.nio.ByteBuffer;

/**
 * 局部变量
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public class LocVar {

    private String varName;

    private int startPC;

    private int endPC;

    public String getVarName() {
        return varName;
    }

    public int getStartPC() {
        return startPC;
    }

    public int getEndPC() {
        return endPC;
    }

    void read(ByteBuffer buf) {
        varName = BinaryChunk.getLuaString(buf);
        startPC = buf.getInt();
        endPC = buf.getInt();
    }

}
