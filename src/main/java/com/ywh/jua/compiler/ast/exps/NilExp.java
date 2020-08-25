package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.Exp;

/**
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class NilExp extends Exp {
    public NilExp(int line) {
        setLine(line);
    }

}
