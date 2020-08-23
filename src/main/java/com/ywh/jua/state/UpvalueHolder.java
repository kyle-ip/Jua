package com.ywh.jua.state;

/**
 * Upvalue 的内部数据结构
 *
 * 比如对于脚本：
 *      local u, v, w
 *      local function f() u = v end
 * 则编译时会被封装在一个主函数：
 *      function main(...)
 *          local u, v, w
 *          local function f() u = v end
 *      end
 * 其中 u 和 v 在 f 的作用域外定义，因此 f 的两个 Upvalue 为 u 和 v。
 * 可知 Lua 函数都是闭包，包括 main 函数不活了 _ENV 这个特殊的 Upvalue。
 *
 * 扁平闭包：需要借助外围函数来捕获更外围函数局部变量的闭包。
 *
 * @author ywh
 * @since 2020/8/20/020
 */
class UpvalueHolder {

    final int index;

    private LuaStack stack;

    private Object value;

    UpvalueHolder(Object value) {
        this.value = value;
        this.index = 0;
    }

    UpvalueHolder(LuaStack stack, int index) {
        this.stack = stack;
        this.index = index;
    }

    Object get() {
        return stack != null ? stack.get(index + 1) : value;
    }

    void set(Object value) {
        if (stack != null) {
            stack.set(index + 1, value);
        } else {
            this.value = value;
        }
    }

    /**
     * 把值从栈复制到寄存器。
     */
    void migrate() {
        if (stack == null) {
            return;
        }
        value = stack.get(index + 1);
        stack = null;
    }

}
