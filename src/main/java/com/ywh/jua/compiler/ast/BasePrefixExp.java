package com.ywh.jua.compiler.ast;


/**
 * 前缀表达式
 *      prefixexp ::= Name
 *          | ‘(’ exp ‘)’
 *          | prefixexp ‘[’ exp ‘]’
 *          | prefixexp ‘.’ Name
 *          | prefixexp ‘:’ Name args
 *          | prefixexp args
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public abstract class BasePrefixExp extends BaseExp {

}
