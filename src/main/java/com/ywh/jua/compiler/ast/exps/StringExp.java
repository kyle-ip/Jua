package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.Exp;
import com.ywh.jua.compiler.lexer.Token;

/**
 * 字符串表达式
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class StringExp extends Exp {

    private String str;

    public StringExp(Token token) {
        setLine(token.getLine());
        this.str = token.getValue();
    }

    public StringExp(int line, String str) {
        setLine(line);
        this.str = str;
    }


    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
}
