package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.BasePrefixExp;

/**
 * 名称表达式
 * t.k <=> t["k"]
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class NameExp extends BasePrefixExp {

    private String name;

    public NameExp(int line, String name) {
        setLine(line);
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
