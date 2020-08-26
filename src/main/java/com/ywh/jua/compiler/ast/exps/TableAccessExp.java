package com.ywh.jua.compiler.ast.exps;


import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.BasePrefixExp;

/**
 * 表访问表达式
 * lastLine 记录右方括号所在行号。
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class TableAccessExp extends BasePrefixExp {

    private BaseExp prefixExp;

    private BaseExp keyExp;

    public TableAccessExp(int lastLine, BaseExp prefixExp, BaseExp keyExp) {
        setLastLine(lastLine);
        this.prefixExp = prefixExp;
        this.keyExp = keyExp;
    }

    public BaseExp getPrefixExp() {
        return prefixExp;
    }

    public void setPrefixExp(BaseExp prefixExp) {
        this.prefixExp = prefixExp;
    }

    public BaseExp getKeyExp() {
        return keyExp;
    }

    public void setKeyExp(BaseExp keyExp) {
        this.keyExp = keyExp;
    }
}
