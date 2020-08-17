package com.ywh.jua.chunk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 *
 * chunk 格式（包括 Lua 虚拟机指令）属于 Lua 虚拟机内部实现细节，并没标准化，以官方源码为准；
 * chunk 没有考虑跨平台需求，对于需要使用超过一个字节表示的数据，必须要考虑大小端（Endianness）问题；
 * chunk 格式的设计没有考虑不同 Lua 版本之间的兼容问题；
 * chunk 格式主要是为了获得更好的运行速度，并没有被刻意设计得很紧凑（因此编译后可能会比文本形式的源文件更大）。
 *
 * chunk 内部使用的数据类型大致可以分为数值、字符串和列表：
 *      数值：即字节、C 整型、C size_t、Lua 整型、Lua 浮点型，都按照固定长度存储，除了字节以外其他都占用多个字节，占用个数记录在头部；
 *      字符串：即字节数组，长度不固定，因此长度也会记录在 chunk；还可以分为短字符串和长字符串（NULL、n <= 0xFD、n >= 0xFE）；
 *      列表：存放指令表、常量表、子函数原型表等信息，先用一个 C 整型记录长度，后紧接着存储 n 个列表元素。
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public class BinaryChunk {

    /**
     * 签名（4bytes），即魔数，用于快速识别文件格式。
     */
    private static final byte[] LUA_SIGNATURE = {0x1b, 'L', 'u', 'a'};

    /**
     * 版本号（1byte），如 5.3.4 表示大版本号 5、小版本号 3，发布号 4（不被考虑）。
     */
    private static final int LUAC_VERSION = 0x53;

    /**
     * 格式号（1byte），官方实现使用格式号为 0。
     */
    private static final int LUAC_FORMAT = 0;

    /**
     * LUAC_DATA（6bytes），用作进一步校验；
     * 前两个字节 0x1993 表示 1.0 版本发布年份，后四个字节依次是回车符（OxOD）、换行符（OxOA）、替换符（OxlA）和另一个换行符
     */
    private static final byte[] LUAC_DATA = {0x19, (byte) 0x93, '\r', '\n', 0x1a, '\n'};

    /**
     * 整数和 Lua 虚拟机指令宽度（5bytes），分别记录 cint、size_t、Lua 虚拟机指令、Lua 整数和 Lua 浮点数这 5 种数据类型在二进 chunk 里占用的字节数。
     */
    private static final int CINT_SIZE = 4;
    private static final int CSIZET_SIZE = 8;
    private static final int INSTRUCTION_SIZE = 4;
    private static final int LUA_INTEGER_SIZE = 8;
    private static final int LUA_NUMBER_SIZE = 8;

    /**
     * 整数 0x5678（在本实现中整数占用 8bytes），用于检测二进制 chunk 的大小端方式。
     */
    private static final int LUAC_INT = 0x5678;

    /**
     * 浮点数 370.5（在本实现中整数占用 8bytes），用于检测二进制 chunk 所使用的浮点数格式（一般采用 IEEE 754 浮点数格式）。
     */
    private static final double LUAC_NUM = 370.5;

    /**
     * 用于解析二进制 chunk
     *
     * @param data
     * @return
     */
    public static Prototype undump(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        checkHead(buf);
        buf.get(); // size_upvalues
        Prototype mainFunc = new Prototype();
        mainFunc.read(buf, "");
        return mainFunc;
    }


    /**
     * 校验头部，即依次检查含签名 本号、 式号、各种整数类型占用 字节数，以及大 端和浮点数格式识别信息等
     *
     * @param buf
     */
    private static void checkHead(ByteBuffer buf) {
        if (!Arrays.equals(LUA_SIGNATURE, getBytes(buf, 4))) {
            throw new RuntimeException("not a precompiled chunk!");
        }
        if (buf.get() != LUAC_VERSION) {
            throw new RuntimeException("version mismatch!");
        }
        if (buf.get() != LUAC_FORMAT) {
            throw new RuntimeException("format mismatch!");
        }

        if (!Arrays.equals(LUAC_DATA, getBytes(buf, 6))) {
            throw new RuntimeException("corrupted!");
        }
        if (buf.get() != CINT_SIZE) {
            throw new RuntimeException("int size mismatch!");
        }
        if (buf.get() != CSIZET_SIZE) {
            throw new RuntimeException("size_t size mismatch!");
        }
        if (buf.get() != INSTRUCTION_SIZE) {
            throw new RuntimeException("instruction size mismatch!");
        }
        if (buf.get() != LUA_INTEGER_SIZE) {
            throw new RuntimeException("lua_Integer size mismatch!");
        }
        if (buf.get() != LUA_NUMBER_SIZE) {
            throw new RuntimeException("lua_Number size mismatch!");
        }
        if (buf.getLong() != LUAC_INT) {
            throw new RuntimeException("endianness mismatch!");
        }
        if (buf.getDouble() != LUAC_NUM) {
            throw new RuntimeException("float format mismatch!");
        }
    }

    /**
     * 读取字符串
     *
     * @param buf
     * @return
     */
    static String getLuaString(ByteBuffer buf) {
        int size = buf.get() & 0xFF;
        if (size == 0) {
            return "";
        }
        if (size == 0xFF) {
            // size_t
            size = (int) buf.getLong();
        }
        byte[] a = getBytes(buf, size - 1);

        // todo
        return new String(a);
    }

    private static byte[] getBytes(ByteBuffer buf, int n) {
        byte[] a = new byte[n];
        buf.get(a);
        return a;
    }

}
