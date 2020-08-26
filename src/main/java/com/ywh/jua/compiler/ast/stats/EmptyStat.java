package com.ywh.jua.compiler.ast.stats;


import com.ywh.jua.compiler.ast.BaseStat;

/**
 * 空语句
 * 仅用于分割。
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class EmptyStat extends BaseStat {

    public static final EmptyStat INSTANCE = new EmptyStat();

}
