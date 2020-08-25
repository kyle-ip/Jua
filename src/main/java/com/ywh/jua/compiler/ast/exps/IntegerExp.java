package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.Exp;

/**
 * 整数表达式
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class IntegerExp extends Exp {

    private long val;

    public IntegerExp(int line, long val) {
        this.val = val;
        setLine(line);
    }

    public long getVal() {
        return val;
    }

    public void setVal(long val) {
        this.val = val;
    }
}
