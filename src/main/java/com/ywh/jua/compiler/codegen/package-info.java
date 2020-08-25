package com.ywh.jua.compiler.codegen;

// 代码生成器
// 目的是把语法分析器输出的 AST 转换成函数原型，分成两个阶段：
//      对 AST 进行处理，生成自定义的内部结构；
//      把内部结构转换成函数原型。