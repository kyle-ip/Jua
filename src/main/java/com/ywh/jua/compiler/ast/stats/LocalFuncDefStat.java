package com.ywh.jua.compiler.ast.stats;

import com.ywh.jua.compiler.ast.BaseStat;
import com.ywh.jua.compiler.ast.exps.FuncDefExp;

/**
 * 局部函数定义语句
 * local function Name funcbody
 *
 * local function f (params) body end
 * => local f; f = function (params) body end
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class LocalFuncDefStat extends BaseStat {

    /**
     * 函数名
     */
    private String name;

    /**
     * 函数表达式
     */
    private FuncDefExp exp;

    public LocalFuncDefStat(String name, FuncDefExp exp) {
        this.name = name;
        this.exp = exp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FuncDefExp getExp() {
        return exp;
    }

    public void setExp(FuncDefExp exp) {
        this.exp = exp;
    }
}
