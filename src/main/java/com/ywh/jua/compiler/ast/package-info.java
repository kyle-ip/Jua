package com.ywh.jua.compiler.ast;

// 抽象语法树（AST）
// 对应具体语法树（CST）或解析树（PT）是源代码解析的结果，完整保留源代码的各种信息；
// 对于编译器而言 CST 中包含的许多信息（分号、关键字等）是多余的，把 CST 中多余的信息去掉，得到的就是 AST。
// 编译器在语法分析阶段直接生成 AST，而不必生成 CST 再转换成 AST。
// 编程语言一般使用上下文无关法（CFG）来描述，而 CFG 一般使用巴科斯范式（BNF）或其扩展（EBNF）来书写（铁路图）。

// Lua 语句大致可分为控制语句、声明和赋值语句以及其他语句三类。其 EBNF 描述：
// stat ::= ';'
//      | varlist '=' explist
//      | functioncall
//      | label
//      | break
//      | goto Name
//      | do block end
//      | while exp do block end
//      | repeat block until exp
//      | if exp then block {elseif exp then block} [else block] end
//      | for Name '=' exp ',' exp [',' exp] do block end
//      | for namelist in explist do block end
//      | function funcname funcbody
//      | local function Name funcbody
//      | local namelist ['=' explist]

// Lua 表达式大致可以分为字面量表达式、构造器表达式、运算符表达式、vararg 表达式和前缀表达式五类。其 EBNF 描述：
// exp ::=  nil
//      | false
//      | true
//      | Numeral
//      | LiteralString
//      | ‘...’
//      | functiondef
//      | prefixexp
//      | tableconstructor
//      | exp binop exp
//      | unop exp
