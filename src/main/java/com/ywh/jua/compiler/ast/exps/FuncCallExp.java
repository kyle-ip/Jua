package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.BasePrefixExp;

import java.util.List;

/**
 * 函数调用表达式
 * line 记录左小括号所在行号；
 * lastLine 记录右方括号所在行号。
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class FuncCallExp extends BasePrefixExp {

    private BaseExp prefixExp;

    /**
     * SELF 指令支持（语法糖）
     */
    private StringExp nameExp;

    /**
     * 参数表
     */
    private List<BaseExp> args;

    public BaseExp getPrefixExp() {
        return prefixExp;
    }

    public void setPrefixExp(BaseExp prefixExp) {
        this.prefixExp = prefixExp;
    }

    public StringExp getNameExp() {
        return nameExp;
    }

    public void setNameExp(StringExp nameExp) {
        this.nameExp = nameExp;
    }

    public List<BaseExp> getArgs() {
        return args;
    }

    public void setArgs(List<BaseExp> args) {
        this.args = args;
    }
}
