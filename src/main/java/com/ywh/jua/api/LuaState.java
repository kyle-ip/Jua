package com.ywh.jua.api;

/**
 * Lua （解释器）状态：Lua API 体现为一系列操作 LuaState 结构的函数。
 * 主要包括基础栈操纵方法、栈访问方法、压栈方法三类。
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public interface LuaState {

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
     * 将栈顶索引设置为指定值，小于当前栈顶索引得值全部弹出
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

}
