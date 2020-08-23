package com.ywh.jua.api;

/**
 * Lua（解释器）状态：Lua API 体现为一系列操作 LuaState 结构的函数。
 * 主要包括基础栈操作方法、栈访问方法、压栈方法三类。
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public interface LuaState {

    /**
     * Lua 栈初始深度
     */
    int LUA_MINSTACK = 20;

    /**
     * Lua 栈最大深度
     */
    int LUAI_MAXSTACK = 1_000_000;

    /**
     * 伪索引，用于操作注册表和 Upvalue。
     * 一般不需要 Lua 有很大的容量，所以定义常量 {@link #LUAI_MAXSTACK} 用于表示 Lua 的最大索引；
     * 由于索引也可能是负数，所以正负 {@link #LUAI_MAXSTACK} 就是有效索引的最大值和最小值，负值再 -1000 即表示注册表的伪索引。
     *
     * LUA_REGISTRYINDEX < min valid index < 0 < max valid index
     *
     * 小于等于该值表示伪索引。
     */
    int LUA_REGISTRYINDEX = -LUAI_MAXSTACK - 1_000;

    /**
     * 全局环境在注册表的索引
     */
    long LUA_RIDX_GLOBALS = 2;

    /**
     * 返回栈顶
     *
     * @return
     */
    int getTop();

    /**
     * 转换为绝对索引
     *
     * @param idx
     * @return
     */
    int absIndex(int idx);

    /**
     * 检查栈是否有剩余空间
     *
     * @param n
     * @return
     */
    boolean checkStack(int n);

    /**
     * 弹出 n 个值
     *
     * @param n
     */
    void pop(int n);

    /**
     * 把 fromIdx 的值复制到 toIdx
     *
     * @param fromIdx
     * @param toIdx
     */
    void copy(int fromIdx, int toIdx);

    /**
     * 把指定索引处的值推入栈顶
     *
     * @param idx
     */
    void pushValue(int idx);

    /**
     * 将栈顶值弹出，写入指定位置
     *
     * @param idx
     */
    void replace(int idx);

    /**
     * 将栈顶值弹出，插入指定位置
     *
     * @param idx
     */
    void insert(int idx);

    /**
     * 删除指定索引处的值
     *
     * @param idx
     */
    void remove(int idx);

    /**
     * 旋转：
     * 5        5        4        3
     * 4        4        5        2
     * 3   ->   1   ->   1   ->   1
     * 2        2        2        5
     * 1        3        3        4
     *
     * @param idx
     * @param n
     */
    void rotate(int idx, int n);

    /**
     * 将栈顶索引设置为指定值，小于当前栈顶索引的值全部弹出
     *
     * @param idx
     */
    void setTop(int idx);

    /**
     * 获取类型名称
     *
     * @param tp
     * @return
     */
    String typeName(LuaType tp);

    /**
     * 获取指定索引位置的值的类型
     *
     * @param idx
     * @return
     */
    LuaType type(int idx);

    // 判断指定索引位置的值的类型

    boolean isNone(int idx);

    boolean isNil(int idx);

    boolean isNoneOrNil(int idx);

    boolean isBoolean(int idx);

    boolean isInteger(int idx);

    boolean isNumber(int idx);

    boolean isString(int idx);

    boolean isTable(int idx);

    boolean isThread(int idx);

    boolean isFunction(int idx);

    // 转换索引位置的值的类型

    boolean toBoolean(int idx);

    long toInteger(int idx);

    Long toIntegerX(int idx);

    double toNumber(int idx);

    Double toNumberX(int idx);

    String toString(int idx);

    // 指定类型的值入栈

    /* push functions (Go -> stack); */
    void pushNil();

    void pushBoolean(boolean b);

    void pushInteger(long n);

    void pushNumber(double n);

    void pushString(String s);


    /**
     * 算数、按位运算：
     * 依次弹出右操作数、左操作数，调用 {@link com.ywh.jua.state.Arithmetic #arith 执行计算 }；
     * 如果操作符为取反、符号，则左右操作数为同一个。
     *
     * @param op
     */
    void arith(ArithOp op);

    /**
     * 比较运算：
     * 比较栈中指定下标的两个元素
     *
     * @param idx1
     * @param idx2
     * @param op
     * @return
     */
    boolean compare(int idx1, int idx2, CmpOp op);

    /**
     *
     */
    void newTable();

    /**
     * @param nArr
     * @param nRec
     */
    void createTable(int nArr, int nRec);

    /**
     * @param idx
     * @return
     */
    LuaType getTable(int idx);

    /**
     * @param idx
     * @param k
     * @return
     */
    LuaType getField(int idx, String k);

    /**
     * @param idx
     * @param i
     * @return
     */
    LuaType getI(int idx, long i);

    /* set functions (stack -> Lua) */

    /**
     * @param idx
     */
    void setTable(int idx);

    /**
     * @param idx
     * @param k
     */
    void setField(int idx, String k);

    /**
     * @param idx
     * @param i
     */
    void setI(int idx, long i);

    ThreadStatus load(byte[] chunk, String chunkName, String mode);

    void call(int nArgs, int nResults);

    /* miscellaneous functions */

    /**
     * 求长度：
     * 取指定索引的值，求出其长度后推入栈顶。
     *
     * @param idx
     */
    void len(int idx);

    /**
     * 拼接：
     * 从栈顶弹出 n 个值，对这些值进行拼接，再把结果推入栈顶。
     * 当 n 为 0，则推入空串；
     * 要求这 n 个值都是字符串。
     *
     * @param n
     */
    void concat(int n);

    /**
     * 判断指定索引的值是否 Java 函数闭包。
     *
     * @param idx
     * @return
     */
    boolean isJavaFunction(int idx);

    /**
     * 转换指定索引的闭包为 Java 函数。
     *
     * @param idx
     * @return
     */
    JavaFunction toJavaFunction(int idx);

    /**
     * 把 Java 函数转换为闭包，推入栈顶。
     *
     * @param f
     */
    void pushJavaFunction(JavaFunction f);

    void pushJavaClosure(JavaFunction f, int n);

    void pushGlobalTable();

    LuaType getGlobal(String name);

    void setGlobal(String name);

    void register(String name, JavaFunction f);
}
