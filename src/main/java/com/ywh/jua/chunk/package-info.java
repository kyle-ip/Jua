package com.ywh.jua.chunk;

// 作为 Lua 字节码的载体，类似 Java Class：
//      .lua <=> .java
//      .out <=> .class
// 预编译：由 Lua 编译器编译成内部结构（包含字节码信息），再由虚拟机执行字节码。