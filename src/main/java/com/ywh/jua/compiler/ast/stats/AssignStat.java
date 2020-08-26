package com.ywh.jua.compiler.ast.stats;


import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.BaseStat;

import java.util.List;

/**
 * 赋值表达式
 * varlist '=' explist
 * varlist ::= var {',' var}
 * var ::= Name | prefixexp '[' exp ']' | prefix exp '.' Name
 * explist ::= exp {',' exp}
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class AssignStat extends BaseStat {

    /**
     * 等号左侧的都好分割的 var 表达式列表
     */
    private List<BaseExp> varList;

    /**
     * 等号右侧的逗号分隔的任意表达式列表
     */
    private List<BaseExp> expList;

    public AssignStat(int lastLine, List<BaseExp> varList, List<BaseExp> expList) {
        setLastLine(lastLine);
        this.varList = varList;
        this.expList = expList;
    }

    public List<BaseExp> getVarList() {
        return varList;
    }

    public void setVarList(List<BaseExp> varList) {
        this.varList = varList;
    }

    public List<BaseExp> getExpList() {
        return expList;
    }

    public void setExpList(List<BaseExp> expList) {
        this.expList = expList;
    }
}
