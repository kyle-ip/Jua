package com.ywh.jua.compiler.ast.exps;


import com.ywh.jua.compiler.ast.Exp;
import com.ywh.jua.compiler.ast.PrefixExp;

/**
 * 圆括号表达式
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class ParensExp extends PrefixExp {

    private Exp exp;

    public ParensExp(Exp exp) {
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }
}
