package com.ywh.jua.compiler.ast.stats;


import com.ywh.jua.compiler.ast.Exp;
import com.ywh.jua.compiler.ast.Stat;

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
public class AssignStat extends Stat {

    /**
     * 等号左侧的都好分割的 var 表达式列表
     */
    private List<Exp> varList;

    /**
     * 等号右侧的逗号分隔的任意表达式列表
     */
    private List<Exp> expList;

    public AssignStat(int lastLine, List<Exp> varList, List<Exp> expList) {
        setLastLine(lastLine);
        this.varList = varList;
        this.expList = expList;
    }

    public List<Exp> getVarList() {
        return varList;
    }

    public void setVarList(List<Exp> varList) {
        this.varList = varList;
    }

    public List<Exp> getExpList() {
        return expList;
    }

    public void setExpList(List<Exp> expList) {
        this.expList = expList;
    }
}
