package com.ywh.jua.compiler.ast.exps;


import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.BasePrefixExp;

/**
 * 圆括号表达式
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class ParensExp extends BasePrefixExp {

    private BaseExp exp;

    public ParensExp(BaseExp exp) {
        this.exp = exp;
    }

    public BaseExp getExp() {
        return exp;
    }

    public void setExp(BaseExp exp) {
        this.exp = exp;
    }
}
