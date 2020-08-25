package com.ywh.jua.compiler.ast.stats;


import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.Exp;
import com.ywh.jua.compiler.ast.Stat;

/**
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class RepeatStat extends Stat {

    private Block block;

    private Exp exp;

    public RepeatStat(Block block, Exp exp) {
        this.block = block;
        this.exp = exp;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public Exp getExp() {
        return exp;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }
}
