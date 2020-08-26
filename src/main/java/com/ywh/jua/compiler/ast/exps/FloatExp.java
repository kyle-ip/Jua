package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.BaseExp;

/**
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class FloatExp extends BaseExp {

    private double val;

    public FloatExp(int line, Double val) {
        setLine(line);
        this.val = val;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }
}
