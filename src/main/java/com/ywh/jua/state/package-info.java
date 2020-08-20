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