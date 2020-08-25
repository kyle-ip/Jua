package com.ywh.jua.compiler.ast.stats;


import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.Stat;

/**
 * do 语句
 * 用于为语句引入新作用域。
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class DoStat extends Stat {

    /**
     * 块
     */
    private Block block;

    public DoStat(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
