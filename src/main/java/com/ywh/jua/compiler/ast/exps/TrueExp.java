package com.ywh.jua.compiler.ast.exps;


import com.ywh.jua.compiler.ast.Exp;

/**
 * true 表达式
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class TrueExp extends Exp {

    public TrueExp(int line) {
        setLine(line);
    }
}
