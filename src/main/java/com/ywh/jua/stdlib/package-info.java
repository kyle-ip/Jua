package com.ywh.jua.stdlib;

// 标准库：包括 math、string、utf8、table、IO、OS、package、coroutine、debug 十个
//      基础库提供 24 个全局变量，其中 _VERSION 是字符串类型，表示 Lua 版本号；_G 是表类型；其余都是函数类型（全局函数）。
//          print(_VERSION)
//          print(_G._VERSION)
//          print(_G)
//          print(print)
//      全局函数包括类型相关、错误处理相关、迭代器相关、元编程相关、加载等六类。

// 辅助 API：用于解决基础 API 的易用性而存在，与标准库的关系：
// +----------------+
// |      标准库     |
// |    +-----------+
// |    |  辅助 API  |
// +----+-----------+
// |     基础 API    |
// +----------------+