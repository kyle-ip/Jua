package com.ywh.jua.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.ywh.jua.api.LuaState.LUA_REGISTRYINDEX;

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
     * 间接访问注册表
     */
    LuaStateImpl state;

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

    /**
     * 局部变量的寄存器索引表
     */
    Map<Integer, UpvalueHolder> openuvs;

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
        if (idx >= 0 || idx <= LUA_REGISTRYINDEX) {
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
        // Upvalue
        if (idx < LUA_REGISTRYINDEX) {
            int uvIdx = LUA_REGISTRYINDEX - idx - 1;
            return closure != null && uvIdx < closure.upvals.length;
        }
        // 注册表
        if (idx == LUA_REGISTRYINDEX) {
            return true;
        }
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
        // 索引小于注册表索引，表示 Upvalue 伪索引，需要转换成真实索引（从 0 开始），再判断是否在有效范围内。
        if (idx < LUA_REGISTRYINDEX) {
            int uvIdx = LUA_REGISTRYINDEX - idx - 1;
            if (closure != null && closure.upvals.length > uvIdx && closure.upvals[uvIdx] != null) {
                return closure.upvals[uvIdx].get();
            }
            // 伪索引无效，返回空。
            else {
                return null;
            }
        }
        // 注册表
        if (idx == LUA_REGISTRYINDEX) {
            return state.registry;
        }
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
        if (idx < LUA_REGISTRYINDEX) { /* upvalues */
            int uvIdx = LUA_REGISTRYINDEX - idx - 1;
            if (closure != null && closure.upvals.length > uvIdx && closure.upvals[uvIdx] != null) {
                closure.upvals[uvIdx].set(val);
            }
            return;
        }
        if (idx == LUA_REGISTRYINDEX) {
            state.registry = (LuaTable) val;
            return;
        }
        slots.set(absIndex(idx) - 1, val);
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
