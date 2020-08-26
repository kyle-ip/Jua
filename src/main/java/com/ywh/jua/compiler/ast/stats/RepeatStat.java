package com.ywh.jua.compiler.ast.stats;


import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.BaseStat;

/**
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class RepeatStat extends BaseStat {

    private Block block;

    private BaseExp exp;

    public RepeatStat(Block block, BaseExp exp) {
        this.block = block;
        this.exp = exp;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public BaseExp getExp() {
        return exp;
    }

    public void setExp(BaseExp exp) {
        this.exp = exp;
    }
}
