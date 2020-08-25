package com.ywh.jua.compiler.ast.stats;


import com.ywh.jua.compiler.ast.Stat;
import com.ywh.jua.compiler.ast.exps.FuncCallExp;

/**
 * 函数调用语句
 * 可以是语句或表达式。
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class FuncCallStat extends Stat {

    private FuncCallExp exp;

    public FuncCallStat(FuncCallExp exp) {
        this.exp = exp;
    }

    public FuncCallExp getExp() {
        return exp;
    }

    public void setExp(FuncCallExp exp) {
        this.exp = exp;
    }
}
