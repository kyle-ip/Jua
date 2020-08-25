package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.Exp;
import com.ywh.jua.compiler.ast.PrefixExp;

import java.util.List;

/**
 * 函数调用表达式
 * line 记录左小括号所在行号；
 * lastLine 记录右方括号所在行号。
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class FuncCallExp extends PrefixExp {

    private Exp prefixExp;

    /**
     * SELF 指令支持（语法糖）
     */
    private StringExp nameExp;

    /**
     * 参数表
     */
    private List<Exp> args;

    public Exp getPrefixExp() {
        return prefixExp;
    }

    public void setPrefixExp(Exp prefixExp) {
        this.prefixExp = prefixExp;
    }

    public StringExp getNameExp() {
        return nameExp;
    }

    public void setNameExp(StringExp nameExp) {
        this.nameExp = nameExp;
    }

    public List<Exp> getArgs() {
        return args;
    }

    public void setArgs(List<Exp> args) {
        this.args = args;
    }
}
