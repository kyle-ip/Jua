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
public class Block extends Node {

    private List<Stat> stats;

    private List<Exp> retExps;

    public List<Stat> getStats() {
        return stats;
    }

    public void setStats(List<Stat> stats) {
        this.stats = stats;
    }

    public List<Exp> getRetExps() {
        return retExps;
    }

    public void setRetExps(List<Exp> retExps) {
        this.retExps = retExps;
    }
}
