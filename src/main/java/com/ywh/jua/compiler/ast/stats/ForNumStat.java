package com.ywh.jua.compiler.ast.stats;

import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.BaseStat;


/**
 * 数值 for 语句
 * 以 for 开始，接着是标识符和等号，然后是逗号分隔的初始值、限制和可选的步长表达式，后跟一条 do 语句。
 *
 * for Name '=' exp ',' exp [',' exp] do block end
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class ForNumStat extends BaseStat {

    /**
     * for 所在行号
     */
    private int lineOfFor;

    /**
     * do 所在行号
     */
    private int lineOfDo;

    /**
     * 变量名称
     */
    private String varName;

    /**
     * 初始表达式
     */
    private BaseExp initExp;

    /**
     * 限制表达式
     */
    private BaseExp limitExp;

    /**
     * 步长表达式
     */
    private BaseExp stepExp;

    /**
     * 代码块
     */
    private Block block;

    public int getLineOfFor() {
        return lineOfFor;
    }

    public void setLineOfFor(int lineOfFor) {
        this.lineOfFor = lineOfFor;
    }

    public int getLineOfDo() {
        return lineOfDo;
    }

    public void setLineOfDo(int lineOfDo) {
        this.lineOfDo = lineOfDo;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public BaseExp getInitExp() {
        return initExp;
    }

    public void setInitExp(BaseExp initExp) {
        this.initExp = initExp;
    }

    public BaseExp getLimitExp() {
        return limitExp;
    }

    public void setLimitExp(BaseExp limitExp) {
        this.limitExp = limitExp;
    }

    public BaseExp getStepExp() {
        return stepExp;
    }

    public void setStepExp(BaseExp stepExp) {
        this.stepExp = stepExp;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
