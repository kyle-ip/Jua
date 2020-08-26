package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.BaseExp;

import java.util.List;

/**
 * 拼接运算符
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class ConcatExp extends BaseExp {

    /**
     * 拼接内容列表
     */
    private List<BaseExp> exps;

    public ConcatExp(int line, List<BaseExp> exps) {
        setLine(line);
        this.exps = exps;
    }

    public List<BaseExp> getExps() {
        return exps;
    }

    public void setExps(List<BaseExp> exps) {
        this.exps = exps;
    }
}
