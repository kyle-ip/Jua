package com.ywh.jua.compiler.ast.exps;


import com.ywh.jua.compiler.ast.Exp;
import com.ywh.jua.compiler.ast.PrefixExp;

/**
 * 表访问表达式
 * lastLine 记录右方括号所在行号。
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class TableAccessExp extends PrefixExp {

    private Exp prefixExp;

    private Exp keyExp;

    public TableAccessExp(int lastLine, Exp prefixExp, Exp keyExp) {
        setLastLine(lastLine);
        this.prefixExp = prefixExp;
        this.keyExp = keyExp;
    }

    public Exp getPrefixExp() {
        return prefixExp;
    }

    public void setPrefixExp(Exp prefixExp) {
        this.prefixExp = prefixExp;
    }

    public Exp getKeyExp() {
        return keyExp;
    }

    public void setKeyExp(Exp keyExp) {
        this.keyExp = keyExp;
    }
}
