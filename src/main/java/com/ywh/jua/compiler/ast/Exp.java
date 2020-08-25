package com.ywh.jua.compiler.ast;

/**
 * 抽象表达式
 *
 * exp ::=  nil | false | true | Numeral | LiteralString | ‘...’ | functiondef |
 * 	 prefixexp | tableconstructor | exp binop exp | unop exp
 * prefixexp ::= var | functioncall | ‘(’ exp ‘)’
 *
 * var ::=  Name | prefixexp ‘[’ exp ‘]’ | prefixexp ‘.’ Name
 *
 * functioncall ::=  prefixexp args | prefixexp ‘:’ Name args
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public abstract class Exp extends Node {

}
