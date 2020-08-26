package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.lexer.Token;
import com.ywh.jua.compiler.lexer.TokenKind;

/**
 * 二元运算符表达式
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class BinopExp extends BaseExp {

    /**
     * 运算符
     */
    private TokenKind op;

    /**
     * 表达式 1
     */
    private BaseExp exp1;

    /**
     * 表达式 2
     */
    private BaseExp exp2;

    public BinopExp(Token op, BaseExp exp1, BaseExp exp2) {
        setLine(op.getLine());
        this.exp1 = exp1;
        this.exp2 = exp2;

        if (op.getKind() == TokenKind.TOKEN_OP_MINUS) {
            this.op = TokenKind.TOKEN_OP_SUB;
        } else if (op.getKind() == TokenKind.TOKEN_OP_WAVE) {
            this.op = TokenKind.TOKEN_OP_BXOR;
        } else {
            this.op = op.getKind();
        }
    }

    public TokenKind getOp() {
        return op;
    }

    public void setOp(TokenKind op) {
        this.op = op;
    }

    public BaseExp getExp1() {
        return exp1;
    }

    public void setExp1(BaseExp exp1) {
        this.exp1 = exp1;
    }

    public BaseExp getExp2() {
        return exp2;
    }

    public void setExp2(BaseExp exp2) {
        this.exp2 = exp2;
    }
}
