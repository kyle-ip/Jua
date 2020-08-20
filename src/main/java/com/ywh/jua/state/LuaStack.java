package com.ywh.jua.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lua 栈：在 Lua API 中，索引从 1 开始；便于用户使用，索引可以是负数（相对索引），如 -1 表示从栈顶开始递减；
 * 容量是 n，栈顶索引 是 top，则有效索引（写）范围为 [1, top]，可接受索引（读写）范围 [1, n]，无效范围相当于存放 nil 值。
 *
 * 实际上栈中的一个位置视为一个寄存器。
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
class LuaStack {

    /**
     * 自动扩容，无需判断是否有空闲
     */
    private final ArrayList<Object> slots;

    // ========== 调用栈相关 ==========

    /**
     * 闭包
     */
    Closure closure;

    /**
     * 变长参数
     */
    List<Object> varargs;

    /**
     * 程序计数器
     */
    int pc;

    /**
     * 前一个栈帧指针
     */
    LuaStack prev;

    public LuaStack(int stackSize) {
        this.slots = new ArrayList<>(stackSize);
    }

    /**
     * 栈顶索引
     *
     * @return
     */
    int top() {
        return slots.size();
    }

    /**
     * 入栈
     *
     * @param val
     */
    void push(Object val) {

        // TODO
        if (slots.size() > 10000) {
            throw new StackOverflowError();
        }
        slots.add(val);
    }


    /**
     * 出栈
     *
     * @return
     */
    Object pop() {
        return slots.remove(slots.size() - 1);
    }

    /**
     * 入栈 n 个
     *
     * @param vals
     * @param n
     */
    void pushN(List<Object> vals, int n) {
        int nVals = vals == null ? 0 : vals.size();
        if (n < 0) {
            n = nVals;
        }
        for (int i = 0; i < n; i++) {
            push(i < nVals ? vals.get(i) : null);
        }
    }

    /**
     * 出栈 n 个
     *
     * @param n
     * @return
     */
    List<Object> popN(int n) {
        List<Object> vals = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            vals.add(pop());
        }
        Collections.reverse(vals);
        return vals;
    }

    /**
     * 绝对索引
     *
     * @param idx
     * @return
     */
    int absIndex(int idx) {
        if (idx >= 0) {
            return idx;
        } else {
            return idx + slots.size() + 1;
        }
    }

    /**
     * 是否有效索引
     *
     * @param idx
     * @return
     */
    boolean isValid(int idx) {
        int absIdx = absIndex(idx);
        return absIdx > 0 && absIdx <= slots.size();
    }


    /**
     * 取值
     *
     * @param idx
     * @return
     */
    Object get(int idx) {
        int absIdx = absIndex(idx);
        if (absIdx > 0 && absIdx <= slots.size()) {
            return slots.get(absIdx - 1);
        } else {
            return null;
        }
    }

    /**
     * 设值
     *
     * @param idx
     * @param val
     */
    void set(int idx, Object val) {
        int absIdx = absIndex(idx);
        slots.set(absIdx - 1, val);
    }

    /**
     * 翻转（按入栈顺序下标）
     * [1]                   3
     *  2      from: 2       2
     * [3]       to: 4       1
     *  4   -------------->  4
     *  5                    5
     *
     * @param from
     * @param to
     */
    void reverse(int from, int to) {
        Collections.reverse(slots.subList(from, to + 1));
    }


}
