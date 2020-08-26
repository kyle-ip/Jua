package com.ywh.jua.compiler.ast.stats;


import com.ywh.jua.compiler.ast.BaseStat;

/**
 * break 语句
 * 产生一条跳转指令，需要记录其行号。
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class BreakStat extends BaseStat {
    public BreakStat(int line) {
        setLine(line);
    }

}
