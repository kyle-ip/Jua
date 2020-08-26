package com.ywh.jua.compiler.ast;

/**
 * AST 节点
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public abstract class BaseNode {

    /**
     * 当前行号
     */
    private int line;

    /**
     * 代码块的末尾行号
     */
    private int lastLine;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getLastLine() {
        return lastLine;
    }

    public void setLastLine(int lastLine) {
        this.lastLine = lastLine;
    }
}
