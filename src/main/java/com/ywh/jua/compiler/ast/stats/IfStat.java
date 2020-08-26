package com.ywh.jua.compiler.ast.stats;


import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.BaseStat;

import java.util.List;

/**
 * if 语句
 * 分为三个部分：开头是 if-then 表达式和块，中间可以出现任意次 elseif-then 表达式和块，最后是可选的 else 块。
 *
 * if exp then block {elseif exp then block} [else block] end
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class IfStat extends BaseStat {

    /**
     * 表达式列表
     */
    private List<BaseExp> exps;

    /**
     * 与表达式列表对应的块列表
     */
    private List<Block> blocks;

    public IfStat(List<BaseExp> exps, List<Block> blocks) {
        this.exps = exps;
        this.blocks = blocks;
    }

    public List<BaseExp> getExps() {
        return exps;
    }

    public void setExps(List<BaseExp> exps) {
        this.exps = exps;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }
}
