package com.ywh.jua.state;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Lua 栈：在 Lua API 中，索引从 1 开始；便于用户使用，索引可以是负数（相对索引），如 -1 表示从栈顶开始递减；
 * 容量是 n，栈顶索引 是 top，则有效索引（写）范围为 [1, top]，可接受索引（读写）范围 [1, n]，无效范围相当于存放 nil 值。
 *
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
class LuaStack {

    /**
     * 自动扩容，无需判断是否有空闲
     *
     * TODO Lua 栈初始容量为 20，后续再调整
     */
    private final ArrayList<Object> slots = new ArrayList<>(20);


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

    void print() {
        while (true) {
            System.out.println(pop());
        }
    }

}
