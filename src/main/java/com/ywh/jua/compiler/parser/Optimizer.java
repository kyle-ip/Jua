package com.ywh.jua.compiler.parser;


import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.exps.*;
import com.ywh.jua.number.LuaMath;
import com.ywh.jua.number.LuaNumber;

import static com.ywh.jua.compiler.lexer.TokenKind.TOKEN_OP_POW;

/**
 * 优化器
 * 如果运算符表达式的值可以在编译期算出，则 Lua 编译器会完全把它优化。
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
class Optimizer {

    /**
     * 逻辑或
     *
     * @param exp
     * @return
     */
    static BaseExp optimizeLogicalOr(BinopExp exp) {
        // 短路计算
        if (isTrue(exp.getExp1())) {
            // true or x => true
            return exp.getExp1();
        }
        if (isFalse(exp.getExp1()) && !isVarargOrFuncCall(exp.getExp2())) {
            // false or x => x
            return exp.getExp2();
        }
        return exp;
    }

    /**
     * 逻辑且
     *
     * @param exp
     * @return
     */
    static BaseExp optimizeLogicalAnd(BinopExp exp) {
        // 短路计算
        if (isFalse(exp.getExp1())) {
            // false and x => false
            return exp.getExp1();
        }
        if (isTrue(exp.getExp1()) && !isVarargOrFuncCall(exp.getExp2())) {
            // true and x => x
            return exp.getExp2();
        }
        return exp;
    }

    /**
     * 按位
     *
     * @param exp
     * @return
     */
    static BaseExp optimizeBitwiseBinaryOp(BinopExp exp) {
        Long i = castToInteger(exp.getExp1());
        if (i != null) {
            Long j = castToInteger(exp.getExp2());
            if (j != null) {
                switch (exp.getOp()) {
                    case TOKEN_OP_BAND:
                        return new IntegerExp(exp.getLine(), i & j);
                    case TOKEN_OP_BOR:
                        return new IntegerExp(exp.getLine(), i | j);
                    case TOKEN_OP_BXOR:
                        return new IntegerExp(exp.getLine(), i ^ j);
                    case TOKEN_OP_SHL:
                        return new IntegerExp(exp.getLine(), LuaMath.shiftLeft(i, j));
                    case TOKEN_OP_SHR:
                        return new IntegerExp(exp.getLine(), LuaMath.shiftRight(i, j));
                    default:
                        break;
                }
            }
        }
        return exp;
    }

    /**
     * 四则运算
     *
     * @param exp
     * @return
     */
    static BaseExp optimizeArithBinaryOp(BinopExp exp) {
        if (exp.getExp1() instanceof IntegerExp
                && exp.getExp2() instanceof IntegerExp) {
            IntegerExp x = (IntegerExp) exp.getExp1();
            IntegerExp y = (IntegerExp) exp.getExp2();
            switch (exp.getOp()) {
                case TOKEN_OP_ADD:
                    return new IntegerExp(exp.getLine(), x.getVal() + y.getVal());
                case TOKEN_OP_SUB:
                    return new IntegerExp(exp.getLine(), x.getVal() - y.getVal());
                case TOKEN_OP_MUL:
                    return new IntegerExp(exp.getLine(), x.getVal() * y.getVal());
                case TOKEN_OP_IDIV:
                    if (y.getVal() != 0) {
                        return new IntegerExp(exp.getLine(), Math.floorDiv(x.getVal(), y.getVal()));
                    }
                    break;
                case TOKEN_OP_MOD:
                    if (y.getVal() != 0) {
                        return new IntegerExp(exp.getLine(), Math.floorMod(x.getVal(), y.getVal()));
                    }
                    break;
                default:
                    break;
            }
        }

        Double f = castToFloat(exp.getExp1());
        if (f != null) {
            Double g = castToFloat(exp.getExp2());
            if (g != null) {
                switch (exp.getOp()) {
                    case TOKEN_OP_ADD:
                        return new FloatExp(exp.getLine(), f + g);
                    case TOKEN_OP_SUB:
                        return new FloatExp(exp.getLine(), f - g);
                    case TOKEN_OP_MUL:
                        return new FloatExp(exp.getLine(), f * g);
                    case TOKEN_OP_POW:
                        return new FloatExp(exp.getLine(), Math.pow(f, g));
                    default:
                        break;
                }
                if (g != 0) {
                    switch (exp.getOp()) {
                        case TOKEN_OP_DIV:
                            return new FloatExp(exp.getLine(), f / g);
                        case TOKEN_OP_IDIV:
                            return new FloatExp(exp.getLine(), LuaMath.floorDiv(f, g));
                        case TOKEN_OP_MOD:
                            return new FloatExp(exp.getLine(), LuaMath.floorMod(f, g));
                        default:
                            break;
                    }
                }
            }
        }
        return exp;
    }

    /**
     * 乘方
     *
     * @param exp
     * @return
     */
    static BaseExp optimizePow(BaseExp exp) {
        if (exp instanceof BinopExp) {
            BinopExp binopExp = (BinopExp) exp;
            if (binopExp.getOp() == TOKEN_OP_POW) {
                binopExp.setExp2(optimizePow(binopExp.getExp2()));
            }
            return optimizeArithBinaryOp(binopExp);
        }
        return exp;
    }

    /**
     * 优化运算符表达式
     *
     * @param exp
     * @return
     */
    static BaseExp optimizeUnaryOp(UnopExp exp) {
        switch (exp.getOp()) {
            case TOKEN_OP_UNM:
                return optimizeUnm(exp);
            case TOKEN_OP_NOT:
                return optimizeNot(exp);
            case TOKEN_OP_BNOT:
                return optimizeBnot(exp);
            default: return exp;
        }
    }

    /**
     * 优化数值运算
     *
     * @param exp
     * @return
     */
    private static BaseExp optimizeUnm(UnopExp exp) {
        if (exp.getExp() instanceof IntegerExp) {
            IntegerExp iExp = (IntegerExp) exp.getExp();
            iExp.setVal(-iExp.getVal());
            return iExp;
        }
        if (exp.getExp() instanceof FloatExp) {
            FloatExp fExp = (FloatExp) exp.getExp();
            fExp.setVal(-fExp.getVal());
            return fExp;
        }
        return exp;
    }

    /**
     *
     * @param exp
     * @return
     */
    private static BaseExp optimizeNot(UnopExp exp) {
        BaseExp subExp = exp.getExp();
        if (subExp instanceof NilExp || subExp instanceof FalseExp) {
            return new TrueExp(exp.getLine());
        }
        if (subExp instanceof TrueExp || subExp instanceof IntegerExp || subExp instanceof FloatExp || subExp instanceof StringExp) {
            return new FalseExp(exp.getLine());
        }
        return exp;
    }

    /**
     *
     * @param exp
     * @return
     */
    private static BaseExp optimizeBnot(UnopExp exp) {
        if (exp.getExp() instanceof IntegerExp) {
            IntegerExp iExp = (IntegerExp) exp.getExp();
            iExp.setVal(~iExp.getVal());
            return iExp;
        }
        if (exp.getExp() instanceof FloatExp) {
            FloatExp fExp = (FloatExp) exp.getExp();
            double f = fExp.getVal();
            if (LuaNumber.isInteger(f)) {
                return new IntegerExp(fExp.getLine(), ~((int) f));
            }
        }
        return exp;
    }

    /**
     *
     * @param exp
     * @return
     */
    private static boolean isFalse(BaseExp exp) {
        return exp instanceof FalseExp || exp instanceof NilExp;
    }

    /**
     *
     * @param exp
     * @return
     */
    private static boolean isTrue(BaseExp exp) {
        return exp instanceof TrueExp || exp instanceof IntegerExp || exp instanceof FloatExp || exp instanceof StringExp;
    }

    /**
     *
     * @param exp
     * @return
     */
    private static boolean isVarargOrFuncCall(BaseExp exp) {
        return exp instanceof VarargExp || exp instanceof FuncCallExp;
    }

    /**
     *
     * @param exp
     * @return
     */
    private static Long castToInteger(BaseExp exp) {
        if (exp instanceof IntegerExp) {
            return ((IntegerExp) exp).getVal();
        }
        if (exp instanceof FloatExp) {
            double f = ((FloatExp) exp).getVal();
            return LuaNumber.isInteger(f) ? (long) f : null;
        }
        return null;
    }

    /**
     *
     * @param exp
     * @return
     */
    private static Double castToFloat(BaseExp exp) {
        if (exp instanceof IntegerExp) {
            return (double) ((IntegerExp) exp).getVal();
        }
        if (exp instanceof FloatExp) {
            return ((FloatExp) exp).getVal();
        }
        return null;
    }

}
