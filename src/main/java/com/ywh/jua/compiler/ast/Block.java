package com.ywh.jua.compiler.ast;

import java.util.List;

// chunk ::= block
// type Chunk *Block

// block ::= {stat} [retstat]
// retstat ::= return [explist] [‘;’]
// explist ::= exp {‘,’ exp}

/**
 * 代码块
 * 由 {@link com.ywh.jua.chunk.BinaryChunk} 转换而成，只包含后续处理所必要的信息，包括语句序列和返回语句里的表达式序列等（丢弃关键字、分号等）。
 *
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class Block extends BaseNode {

    /**
     * 语句
     */
    private List<BaseStat> stats;

    /**
     * 返回语句
     */
    private List<BaseExp> retExps;

    public List<BaseStat> getStats() {
        return stats;
    }

    public void setStats(List<BaseStat> stats) {
        this.stats = stats;
    }

    public List<BaseExp> getRetExps() {
        return retExps;
    }

    public void setRetExps(List<BaseExp> retExps) {
        this.retExps = retExps;
    }
}
