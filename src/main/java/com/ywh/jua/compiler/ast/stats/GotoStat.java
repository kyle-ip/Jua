package com.ywh.jua.compiler.ast.stats;


import com.ywh.jua.compiler.ast.Stat;

/**
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class GotoStat extends Stat {

    private String name;

    public GotoStat(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
