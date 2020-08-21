package com.ywh.jua.state;

// Lua 是动态弱类型语言，Lua 数据类型包括：nil、boolean、number、string、table、function、thread、userdata
// table 本质上是关联数组，同时具备 Map 和 Array 的能力。除了 nil 值和浮点数 NaN 以外，其他值都可以作为键使用。
//
// t[false] = nil
// t["pi"] = 3.14
// t[t] = "table"; assert(t[t] == "table")
// t[10] = assert; assert(t[10] == assert)
//
// local arr = {"a", "b", "c", "d"}
// assert(arr[1] == "a")
// assert(arr[2] == "b")
// assert(#arr == 4)


// Lua 变量分为三类：
//      局部变量：在函数内部定义，本质上是函数调用帧里的寄存器；
//      Upvalue：直接或间接外围函数定义的局部变量；
//      全局变量：全局环境表的字段，通过隐藏的 Upvalue（即_ENV）进行访问。

