package com.ywh.jua.compiler.ast.stats;

import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.BaseStat;

/**
 * while 语句
 * while exp do block end
 * repeat block until exp
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class WhileStat extends BaseStat {

    /**
     * 条件表达式
     */
    private BaseExp exp;

    /**
     * 代码块
     */
    private Block block;

    public WhileStat(BaseExp exp, Block block) {
        this.exp = exp;
        this.block = block;
    }

    public BaseExp getExp() {
        return exp;
    }

    public void setExp(BaseExp exp) {
        this.exp = exp;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
