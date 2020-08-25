package com.ywh.jua.compiler.ast.stats;

import com.ywh.jua.compiler.ast.Stat;

/**
 * label 语句
 * 与 {@link GotoStat} 结合使用实现任意跳转，需要记录标签名。
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class LabelStat extends Stat {

    /**
     * 标签名
     */
    private String name;

    public LabelStat(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
