package com.ywh.jua.compiler.ast.stats;

import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.BaseStat;

import java.util.List;

/**
 * 通用 for 语句
 * for namelist in explist do block end
 * namelist ::= Name {',' Name}
 * explist ::= exp {',' exp}
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class ForInStat extends BaseStat {

    /**
     * do 所在行号
     */
    private int lineOfDo;

    /**
     * in 左侧的标识符列表
     */
    private List<String> nameList;

    /**
     * in 右侧的表达式列表
     */
    private List<BaseExp> expList;

    /**
     * 代码块
     */
    private Block block;

    public int getLineOfDo() {
        return lineOfDo;
    }

    public void setLineOfDo(int lineOfDo) {
        this.lineOfDo = lineOfDo;
    }

    public List<String> getNameList() {
        return nameList;
    }

    public void setNameList(List<String> nameList) {
        this.nameList = nameList;
    }

    public List<BaseExp> getExpList() {
        return expList;
    }

    public void setExpList(List<BaseExp> expList) {
        this.expList = expList;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
