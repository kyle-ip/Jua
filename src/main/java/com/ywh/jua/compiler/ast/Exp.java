package com.ywh.jua.compiler.ast;

/*
exp ::=  nil | false | true | Numeral | LiteralString | ‘...’ | functiondef |
	 prefixexp | tableconstructor | exp binop exp | unop exp

prefixexp ::= var | functioncall | ‘(’ exp ‘)’

var ::=  Name | prefixexp ‘[’ exp ‘]’ | prefixexp ‘.’ Name

functioncall ::=  prefixexp args | prefixexp ‘:’ Name args
*/

/**
 * 抽象表达式
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public abstract class Exp extends Node {

}
