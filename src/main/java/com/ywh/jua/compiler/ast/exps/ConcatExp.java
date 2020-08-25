package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.Exp;

import java.util.List;

/**
 * 拼接运算符
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class ConcatExp extends Exp {

    /**
     * 拼接内容列表
     */
    private List<Exp> exps;

    public ConcatExp(int line, List<Exp> exps) {
        setLine(line);
        this.exps = exps;
    }

    public List<Exp> getExps() {
        return exps;
    }

    public void setExps(List<Exp> exps) {
        this.exps = exps;
    }
}
