package com.ywh.jua.chunk;

import java.nio.ByteBuffer;

/**
 * 函数原型，Lua 编译器以函数为单位进行编译（自动添加一个 main 函数）。
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public class Prototype {

    private static final int TAG_NIL = 0x00;

    private static final int TAG_BOOLEAN = 0x01;

    private static final int TAG_NUMBER = 0x03;

    private static final int TAG_INTEGER = 0x13;

    private static final int TAG_SHORT_STR = 0x04;

    private static final int TAG_LONG_STR = 0x14;

    /**
     * 源文件（debug），即函数的来源，记录二进制 chunk 从哪个源文件编译出来的，只有在主函数原型中该字段才有值，否则为空串；
     * 如果以“@”开头，表示该二进制 chunk 是从 Lua 源文件编译而来的，去掉“@”才是真正文件名；
     * 如果以”=“开头，表示该二进制 chunk 是从标准输入编译而来的；
     * 否则表示该二进制 chunk 是从程序提供的字符串编译而来的，来源存放的就是该字符串。
     */
    private String source;

    /**
     * 起止行号（两个 cint 型整数），用于记录原型对应的函数在源文件中的起止行号；
     * 对于主函数都是 0，对于普通函数都大于 0。
     */
    private int lineDefined;
    private int lastLineDefined;

    /**
     * 固定参数个数（1byte），记录了函数固定参数个数，是相对于变长参数而言的。
     * 对于 Lua 编译器生成的主函数没有固定参数，该值为 0。
     */
    private byte numParams;

    /**
     * 是否 Vararg 函数，即是否含有变长参数，0 表示否，1表示是。
     */
    private byte isVararg;

    /**
     * 寄存器数量（1byte），也称作最大栈深度。
     * Lua 虚拟机在执行函数时，真正使用的其实是一 枝结构，这种栈结构除了可以进行常规推入和弹出操作以外，还可以按索引访问。
     */
    private byte maxStackSize;

    /**
     * 指令表，每条指令 4bytes。
     */
    private int[] code;

    /**
     * 常量表（nil、布尔值、整型、浮点型、字符串），每个常量都以 字节 tag 开头，用来标识后续存储的是哪种类型的常量值。
     */
    private Object[] constants;

    /**
     * Upvalue 表（2bytes），每个元素 2bytes。
     */
    private Upvalue[] upvalues;

    /**
     * 子函数原型表
     */
    private Prototype[] protos;

    /**
     * 行号表（debug）
     */
    private int[] lineInfo;

    /**
     * 局部变量表（debug），表中每个元素都包含变量名（字符串）和起止指令索引（cint）。
     */
    private LocVar[] locVars;

    /**
     * Upvalue 名列表（debug），元素都为字符串，与前面的 Upvalue 表中的元素一一对应，分别记录每个 Upvalue 在源代码中的名字。
     */
    private String[] upvalueNames;

    public String getSource() {
        return source;
    }

    public int getLineDefined() {
        return lineDefined;
    }

    public int getLastLineDefined() {
        return lastLineDefined;
    }

    public byte getNumParams() {
        return numParams;
    }

    public byte getIsVararg() {
        return isVararg;
    }

    public byte getMaxStackSize() {
        return maxStackSize;
    }

    public int[] getCode() {
        return code;
    }

    public Object[] getConstants() {
        return constants;
    }

    public Upvalue[] getUpvalues() {
        return upvalues;
    }

    public Prototype[] getProtos() {
        return protos;
    }

    public int[] getLineInfo() {
        return lineInfo;
    }

    public LocVar[] getLocVars() {
        return locVars;
    }

    public String[] getUpvalueNames() {
        return upvalueNames;
    }

    /**
     * 读取函数原型
     *
     * @param buf
     * @param parentSource
     */
    void read(ByteBuffer buf, String parentSource) {
        source = BinaryChunk.getLuaString(buf);
        if (source.isEmpty()) {
            source = parentSource;
        }
        lineDefined = buf.getInt();
        lastLineDefined = buf.getInt();
        numParams = buf.get();
        isVararg = buf.get();
        maxStackSize = buf.get();
        readCode(buf);
        readConstants(buf);
        readUpvalues(buf);
        readProtos(buf, source);
        readLineInfo(buf);
        readLocVars(buf);
        readUpvalueNames(buf);
    }

    /**
     * 读取指令表
     *
     * @param buf
     */
    private void readCode(ByteBuffer buf) {
        code = new int[buf.getInt()];
        for (int i = 0; i < code.length; i++) {
            code[i] = buf.getInt();
        }
    }

    /**
     * 读取常量表
     *
     * @param buf
     */
    private void readConstants(ByteBuffer buf) {
        constants = new Object[buf.getInt()];
        for (int i = 0; i < constants.length; i++) {
            constants[i] = readConstant(buf);
        }
    }

    /**
     * 读取常量
     *
     * @param buf
     * @return
     */
    private Object readConstant(ByteBuffer buf) {
        switch (buf.get()) {
            case TAG_NIL:
                return null;
            case TAG_BOOLEAN:
                return buf.get() != 0;
            case TAG_INTEGER:
                return buf.getLong();
            case TAG_NUMBER:
                return buf.getDouble();
            case TAG_SHORT_STR:
            case TAG_LONG_STR:
                return BinaryChunk.getLuaString(buf);
            default:
                // TODO
                throw new RuntimeException("corrupted!");
        }
    }

    /**
     * 读取 Upvalue 表
     *
     * @param buf
     */
    private void readUpvalues(ByteBuffer buf) {
        upvalues = new Upvalue[buf.getInt()];
        for (int i = 0; i < upvalues.length; i++) {
            upvalues[i] = new Upvalue();
            upvalues[i].read(buf);
        }
    }

    /**
     * 读取子函数原型
     *
     * @param buf
     * @param parentSource
     */
    private void readProtos(ByteBuffer buf, String parentSource) {
        protos = new Prototype[buf.getInt()];
        for (int i = 0; i < protos.length; i++) {
            protos[i] = new Prototype();
            protos[i].read(buf, parentSource);
        }
    }

    /**
     * 读取行号表
     *
     * @param buf
     */
    private void readLineInfo(ByteBuffer buf) {
        lineInfo = new int[buf.getInt()];
        for (int i = 0; i < lineInfo.length; i++) {
            lineInfo[i] = buf.getInt();
        }
    }

    /**
     * 读取局部变量表
     *
     * @param buf
     */
    private void readLocVars(ByteBuffer buf) {
        locVars = new LocVar[buf.getInt()];
        for (int i = 0; i < locVars.length; i++) {
            locVars[i] = new LocVar();
            locVars[i].read(buf);
        }
    }

    /**
     * 读取 Upvalue 名列表
     *
     * @param buf
     */
    private void readUpvalueNames(ByteBuffer buf) {
        upvalueNames = new String[buf.getInt()];
        for (int i = 0; i < upvalueNames.length; i++) {
            upvalueNames[i] = BinaryChunk.getLuaString(buf);
        }
    }

}
