package com.ywh.jua.compiler.ast.stats;

import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.BaseStat;

import java.util.Collections;
import java.util.List;

/**
 * 局部变量声明语句
 *
 * local namelist ['=' explist]
 * namelist ::= Name {',' Name}
 * explist ::= exp {',' exp}
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class LocalVarDeclStat extends BaseStat {

    /**
     * 等号左侧的标识符列表
     */
    private List<String> nameList;

    /**
     * 等号右侧的表达式列表
     */
    private List<BaseExp> expList;

    public LocalVarDeclStat(int lastLine, List<String> nameList, List<BaseExp> expList) {
        setLastLine(lastLine);
        this.nameList = nameList != null ? nameList : Collections.emptyList();
        this.expList = expList != null ? expList : Collections.emptyList();
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
}
