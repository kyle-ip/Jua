package com.ywh.jua.compiler.ast.stats;

import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.Exp;
import com.ywh.jua.compiler.ast.Stat;

/**
 * while 语句
 * while exp do block end
 * repeat block until exp
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class WhileStat extends Stat {

    /**
     * 条件表达式
     */
    private Exp exp;

    /**
     * 代码块
     */
    private Block block;

    public WhileStat(Exp exp, Block block) {
        this.exp = exp;
        this.block = block;
    }

    public Exp getExp() {
        return exp;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
