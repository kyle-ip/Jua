package com.ywh.jua.api;

/**
 * 基础 API
 *
 * @author ywh
 * @since 2020/8/26 11:26
 */
public interface LuaBasicAPI {


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

    // ========== 判断指定索引位置的值的类型 ==========

    /**
     *
     * @param idx
     * @return
     */
    boolean isNone(int idx);

    /**
     *
     * @param idx
     * @return
     */
    boolean isNil(int idx);

    /**
     *
     * @param idx
     * @return
     */
    boolean isNoneOrNil(int idx);

    /**
     *
     * @param idx
     * @return
     */
    boolean isBoolean(int idx);

    /**
     *
     * @param idx
     * @return
     */
    boolean isInteger(int idx);

    /**
     *
     * @param idx
     * @return
     */
    boolean isNumber(int idx);

    /**
     *
     * @param idx
     * @return
     */
    boolean isString(int idx);

    /**
     *
     * @param idx
     * @return
     */
    boolean isTable(int idx);

    /**
     *
     * @param idx
     * @return
     */
    boolean isThread(int idx);

    /**
     *
     * @param idx
     * @return
     */
    boolean isFunction(int idx);

    // ========== 转换索引位置的值的类型并返回 ==========

    boolean toBoolean(int idx);

    long toInteger(int idx);

    Long toIntegerX(int idx);

    double toNumber(int idx);

    Double toNumberX(int idx);

    String toString(int idx);

    // ========== 指定类型的值入栈 ==========

    /* push functions (Go -> stack); */

    /**
     * nil 入栈
     */
    void pushNil();

    /**
     * boolean 类型入栈
     *
     * @param b
     */
    void pushBoolean(boolean b);

    /**
     * interger 类型入栈
     *
     * @param n
     */
    void pushInteger(long n);

    /**
     * number 类型入栈
     *
     * @param n
     */
    void pushNumber(double n);

    /**
     * 字符串类型入栈
     *
     * @param s
     */
    void pushString(String s);

    /**
     * 适用于任何类型的字符串类型入栈
     *
     * @param fmt
     * @param a
     */
    void pushFString(String fmt, Object... a);

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
     * 创建空表，将其推入栈顶。
     */
    void newTable();

    /**
     * 创建空表（指定数组和哈希表的初始容量），将其推入栈顶。
     *
     * @param nArr
     * @param nRec
     */
    void createTable(int nArr, int nRec);

    /**
     * 根据索引从栈中取表，再根据从栈顶弹出的键取值（并将其推入栈顶）。
     *
     * @param idx
     * @return
     */
    LuaType getTable(int idx);

    /**
     * {@link #getTable}，键为字符串。
     *
     * @param idx
     * @param k
     * @return
     */
    LuaType getField(int idx, String k);

    /**
     * {@link #getTable}，键为整数。
     *
     * @param idx
     * @param i
     * @return
     */
    LuaType getI(int idx, long i);

    /* set functions (stack -> Lua) */

    /**
     * 根据索引取出指定的表，从栈中先后弹出值、键，把键值对设置到表中。
     *
     * @param idx
     */
    void setTable(int idx);

    /**
     * 根据索引取出指定的表，从栈中弹出值，把键值对设置到表中（字符串）。
     *
     * @param idx
     * @param k
     */
    void setField(int idx, String k);

    /**
     * 根据索引取出指定的表，从栈中弹出值，把键值对设置到表中（整数）。
     *
     * @param idx
     * @param i
     */
    void setI(int idx, long i);

    /**
     * 加载二进制 chunk 或 Lua 脚本，把主函数原型实例化为闭包并推入栈顶。
     *
     * 通过参数 mode（可选 “b”、“t”、“bt”）选定加载模式：
     * b：如果加载二进制 chunk，则只需读文件、解析函数原型、实例化为闭包、推入栈顶；
     * t：如果加载文本 Lua 脚本，则先进行编译。
     * bt：都可以，根据实际情况处理。
     *
     * 如果 load 方法无法加载 chunk，则要在栈顶留下一条错误消息。
     * 返回一个状态码，0 表示成功，其他表示失败。
     *
     * @param chunk
     * @param chunkName
     * @param mode
     * @return
     */
    ThreadStatus load(byte[] chunk, String chunkName, String mode);

    /**
     * 调用 Lua 函数
     * 在执行之前，必须先把被调用函数入栈，然后把参数值依次入栈；
     * call 方法调用结束后，参数值和函数会被弹出，取而代之的是指定数量的返回值。
     *
     * 接收两个参数，其一是准备传递给被调用函数的参数数量（同时隐含给出被调用函数在栈中的位置）；
     * 其二是需要的返回值数量（多退少补），-1 表示返回值全部留在栈顶。
     *
     * @param nArgs
     * @param nResults
     */
    void call(int nArgs, int nResults);


    ThreadStatus pCall(int nArgs, int nResults, int msgh);

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

    /**
     * 把Java 函数转换成 Java 闭包推入栈顶（捕获 Upvalue）。
     *
     * @param f
     * @param n
     */
    void pushJavaClosure(JavaFunction f, int n);

    /**
     * 把全局环境推入栈顶
     */
    void pushGlobalTable();

    /**
     * 获取全局变量
     *
     * @param name
     * @return
     */
    LuaType getGlobal(String name);

    /**
     * 设置全局变量（值为栈顶的）
     *
     * @param name
     */
    void setGlobal(String name);

    /**
     * 给全局环境设置 Java 函数（值）
     *
     * @param name
     * @param f
     */
    void register(String name, JavaFunction f);

    /**
     * 取给定索引的值关联的元表置于栈顶。
     *
     * @param idx
     * @return
     */
    boolean getMetatable(int idx);

    /**
     * 设置指定索引的值为栈顶的元表
     *
     * @param idx
     */
    void setMetatable(int idx);

    /**
     * 取长度（忽略元方法）
     *
     * @param idx
     * @return
     */
    int rawLen(int idx);

    /**
     * 判断相等（忽略元方法）
     *
     * @param idx1
     * @param idx2
     * @return
     */
    boolean rawEqual(int idx1, int idx2);

    /**
     * 从表中取值（忽略元方法）
     *
     * @param idx
     * @return
     */
    LuaType rawGet(int idx);

    /**
     * 从表中取整型值（忽略元方法）
     *
     * @param idx
     * @param i
     * @return
     */
    LuaType rawGetI(int idx, long i);

    /**
     * 从表中设置值（忽略元方法）
     *
     * @param idx
     */
    void rawSet(int idx);

    /**
     * 从表中设置整型值（忽略元方法）
     *
     * @param idx
     * @param i
     */
    void rawSetI(int idx, long i);

    /**
     * 根据键迭代取表的下一个键值对
     *
     * @param idx
     * @return
     */
    boolean next(int idx);

    /**
     * 从栈顶弹出一个值作为错误抛出。
     *
     * @return
     */
    int error();


    boolean stringToNumber(String s);
}
