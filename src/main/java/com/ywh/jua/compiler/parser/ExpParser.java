package com.ywh.jua.compiler.parser;


import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.exps.*;
import com.ywh.jua.compiler.lexer.Lexer;
import com.ywh.jua.compiler.lexer.Token;
import com.ywh.jua.compiler.lexer.TokenKind;
import com.ywh.jua.number.LuaNumber;

import java.util.ArrayList;
import java.util.List;

import static com.ywh.jua.compiler.lexer.TokenKind.*;
import static com.ywh.jua.compiler.parser.BlockParser.parseBlock;
import static com.ywh.jua.compiler.parser.Optimizer.*;
import static com.ywh.jua.compiler.parser.PrefixExpParser.parsePrefixExp;

/**
 * 表达式解析器
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
class ExpParser {

    /**
     * 解析表达式列表
     *
     * explist ::= exp {‘,’ exp}
     *
     * @param lexer
     * @return
     */
    static List<BaseExp> parseExpList(Lexer lexer) {
        List <BaseExp> exps = new ArrayList<>();
        // 解析第一个表达式
        exps.add(parseExp(lexer));

        // 循环逐个解析以逗号分隔的表达式列表。
        while (lexer.LookAhead() == TOKEN_SEP_COMMA) {
            // 跳过逗号，添加解析后的表达式到返回列表。
            lexer.nextToken();
            exps.add(parseExp(lexer));
        }
        return exps;
    }


    /**
     * 解析表达式
     * 由于表达式存在优先级，语法上有歧义，因此不能用前瞻的方法解析；
     * 根据优先级划分为 12 个函数，从低到高：parseExp12 ~ parseExp1，parseExp0（运算符之外的其他表达式）。
     * 在二元运算符中，除了拼接和乘方具有右结合性，其他都具有左结合性。
     *
     * exp   ::=  nil | false | true | Numeral | LiteralString | ‘...’ | functiondef |
     *              prefixexp | tableconstructor | exp binop exp | unop exp
     * exp   ::= exp12
     * exp12 ::= exp11 {or exp11}
     * exp11 ::= exp10 {and exp10}
     * exp10 ::= exp9 {(‘<’ | ‘>’ | ‘<=’ | ‘>=’ | ‘~=’ | ‘==’) exp9}
     * exp9  ::= exp8 {‘|’ exp8}
     * exp8  ::= exp7 {‘~’ exp7}
     * exp7  ::= exp6 {‘&’ exp6}
     * exp6  ::= exp5 {(‘<<’ | ‘>>’) exp5}
     * exp5  ::= exp4 {‘..’ exp4}
     * exp4  ::= exp3 {(‘+’ | ‘-’ | ‘*’ | ‘/’ | ‘//’ | ‘%’) exp3}
     * exp2  ::= {(‘not’ | ‘#’ | ‘-’ | ‘~’)} exp1
     * exp1  ::= exp0 {‘^’ exp2}
     * exp0  ::= nil | false | true | Numeral | LiteralString
     *         | ‘...’ | functiondef | prefixexp | tableconstructor
     *
     *
     * @param lexer
     * @return
     */
    static BaseExp parseExp(Lexer lexer) {
        return parseExp12(lexer);
    }


    /**
     * 解析优先级 12 表达式
     *
     * x or y
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp12(Lexer lexer) {
        BaseExp exp = parseExp11(lexer);

        // or
        // 逻辑或具有左结合性，在循环中调用 parseExp11 解析更高优先级的运算符表达式。

        // a or b or c 的表达式 AST
        //      or
        //     /  \
        //    or   \
        //   / \    \
        // a    b    c

        while (lexer.LookAhead() == TOKEN_OP_OR) {
            Token op = lexer.nextToken();
            BinopExp lor = new BinopExp(op, exp, parseExp11(lexer));
            exp = optimizeLogicalOr(lor);
        }
        return exp;
    }


    /**
     * 解析优先级 11 表达式
     *
     * x and y
     *
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp11(Lexer lexer) {
        BaseExp exp = parseExp10(lexer);
        while (lexer.LookAhead() == TOKEN_OP_AND) {
            Token op = lexer.nextToken();
            BinopExp land = new BinopExp(op, exp, parseExp10(lexer));
            exp = optimizeLogicalAnd(land);
        }
        return exp;
    }

    /**
     * 解析优先级 10 表达式
     *
     * compare
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp10(Lexer lexer) {
        BaseExp exp = parseExp9(lexer);
        while (true) {
            switch (lexer.LookAhead()) {
                case TOKEN_OP_LT:
                case TOKEN_OP_GT:
                case TOKEN_OP_NE:
                case TOKEN_OP_LE:
                case TOKEN_OP_GE:
                case TOKEN_OP_EQ:
                    Token op = lexer.nextToken();
                    exp = new BinopExp(op, exp, parseExp9(lexer));
                    break;
                default:
                    return exp;
            }
        }
    }

    /**
     * 解析优先级 9 表达式
     *
     * x | y
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp9(Lexer lexer) {
        BaseExp exp = parseExp8(lexer);
        while (lexer.LookAhead() == TOKEN_OP_BOR) {
            Token op = lexer.nextToken();
            BinopExp bor = new BinopExp(op, exp, parseExp8(lexer));
            exp = optimizeBitwiseBinaryOp(bor);
        }
        return exp;
    }

    /**
     * 解析优先级 8 表达式
     *
     * x ~ y
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp8(Lexer lexer) {
        BaseExp exp = parseExp7(lexer);
        while (lexer.LookAhead() == TOKEN_OP_WAVE) {
            Token op = lexer.nextToken();
            BinopExp bxor = new BinopExp(op, exp, parseExp7(lexer));
            exp = optimizeBitwiseBinaryOp(bxor);
        }
        return exp;
    }

    /**
     * 解析优先级 7 表达式
     *
     * x & y
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp7(Lexer lexer) {
        BaseExp exp = parseExp6(lexer);
        while (lexer.LookAhead() == TOKEN_OP_BAND) {
            Token op = lexer.nextToken();
            BinopExp band = new BinopExp(op, exp, parseExp6(lexer));
            exp = optimizeBitwiseBinaryOp(band);
        }
        return exp;
    }

    /**
     * 解析优先级 6 表达式
     *
     * shift
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp6(Lexer lexer) {
        BaseExp exp = parseExp5(lexer);
        while (true) {
            switch (lexer.LookAhead()) {
                case TOKEN_OP_SHL:
                case TOKEN_OP_SHR:
                    Token op = lexer.nextToken();
                    BinopExp shx = new BinopExp(op, exp, parseExp5(lexer));
                    exp = optimizeBitwiseBinaryOp(shx);
                    break;
                default:
                    return exp;
            }
        }
    }

    /**
     * 解析优先级 5 表达式
     *
     * a .. b
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp5(Lexer lexer) {
        // 虽然拼接运算符也具有右结合性，但是由于其对应的 Lua 虚拟机指令 CONCAT 比较特别；
        // 所以需要做特殊处理：生成多叉树。

        // a .. b .. c 的表达式 AST
        //      ..
        //    /  |  \
        //   a   b   c

        BaseExp exp = parseExp4(lexer);
        if (lexer.LookAhead() != TOKEN_OP_CONCAT) {
            return exp;
        }

        List<BaseExp> exps = new ArrayList<>();
        exps.add(exp);
        int line = 0;
        while (lexer.LookAhead() == TOKEN_OP_CONCAT) {
            line = lexer.nextToken().getLine();
            exps.add(parseExp4(lexer));
        }
        return new ConcatExp(line, exps);
    }

    /**
     * 解析优先级 4 表达式
     *
     * x +/- y
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp4(Lexer lexer) {
        BaseExp exp = parseExp3(lexer);
        while (true) {
            switch (lexer.LookAhead()) {
                case TOKEN_OP_ADD:
                case TOKEN_OP_MINUS:
                    Token op = lexer.nextToken();
                    BinopExp arith = new BinopExp(op, exp, parseExp3(lexer));
                    exp = optimizeArithBinaryOp(arith);
                    break;
                default:
                    return exp;
            }
        }
    }

    /**
     * 解析优先级 3 表达式
     *
     * *, %, /, //
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp3(Lexer lexer) {
        BaseExp exp = parseExp2(lexer);
        while (true) {
            switch (lexer.LookAhead()) {
                case TOKEN_OP_MUL:
                case TOKEN_OP_MOD:
                case TOKEN_OP_DIV:
                case TOKEN_OP_IDIV:
                    Token op = lexer.nextToken();
                    BinopExp arith = new BinopExp(op, exp, parseExp2(lexer));
                    exp = optimizeArithBinaryOp(arith);
                    break;
                default:
                    return exp;
            }
        }
    }

    /**
     * 解析优先级 2 表达式
     * 一元运算符：- ~ # not 也可以认为具有右结合性，因此需要调用自己解析后面的一元运算符表达式
     *
     * unary
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp2(Lexer lexer) {
        switch (lexer.LookAhead()) {
            case TOKEN_OP_MINUS:
            case TOKEN_OP_WAVE:
            case TOKEN_OP_LEN:
            case TOKEN_OP_NOT:
                Token op = lexer.nextToken();
                UnopExp exp = new UnopExp(op, parseExp2(lexer));
                return optimizeUnaryOp(exp);
            default:
                break;
        }
        return parseExp1(lexer);
    }

    /**
     * 解析优先级 1 表达式
     *
     * x ^ y
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp1(Lexer lexer) {
        BaseExp exp = parseExp0(lexer);

        // 乘方具有右结合性，因此递归调用自己解析后面的乘方运算符表达式。
        // a ^ b ^ c 的表达式 AST
        //      ^
        //     / \
        //    /   ^
        //   /   /  \
        //  a   b    c
        if (lexer.LookAhead() == TOKEN_OP_POW) {
            Token op = lexer.nextToken();
            exp = new BinopExp(op, exp, parseExp2(lexer));
        }
        return optimizePow(exp);
    }

    /**
     * 解析优先级 0 表达式（运算符之外的其他表达式）
     * ... nil true false literalString { function
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseExp0(Lexer lexer) {

        // 前瞻一个 token 决定具体要解析哪种表达式。
        // 其中 vararg 和非数字字面量表达式比较简单，直接写在 case 语句里
        switch (lexer.LookAhead()) {
            case TOKEN_VARARG:
                return new VarargExp(lexer.nextToken().getLine());
            case TOKEN_KW_NIL:
                return new NilExp(lexer.nextToken().getLine());
            case TOKEN_KW_TRUE:
                return new TrueExp(lexer.nextToken().getLine());
            case TOKEN_KW_FALSE:
                return new FalseExp(lexer.nextToken().getLine());
            case TOKEN_STRING:
                return new StringExp(lexer.nextToken());
            case TOKEN_NUMBER:
                return parseNumberExp(lexer);
            case TOKEN_SEP_LCURLY:
                return parseTableConstructorExp(lexer);
            case TOKEN_KW_FUNCTION:
                lexer.nextToken();
                return parseFuncDefExp(lexer);
            default:
                return parsePrefixExp(lexer);
        }
    }

    /**
     * 解析数字字面量表达式
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseNumberExp(Lexer lexer) {
        Token token = lexer.nextToken();
        Long i = LuaNumber.parseInteger(token.getValue());
        if (i != null) {
            return new IntegerExp(token.getLine(), i);
        }
        Double f = LuaNumber.parseFloat(token.getValue());
        if (f != null) {
            return new FloatExp(token.getLine(), f);
        }
        throw new RuntimeException("not a number: " + token);
    }




    /**
     * 解析函数参数列表
     *
     * [parlist]
     * parlist ::= namelist [‘,’ ‘...’] | ‘...’
     *
     * @param lexer
     * @return
     */
    private static List<String> parseParList(Lexer lexer) {
        List<String> names = new ArrayList<>();

        // 无参（)）或变长参数（...）
        switch (lexer.LookAhead()) {
            case TOKEN_SEP_RPAREN:
                return names;
            case TOKEN_VARARG:
                lexer.nextToken();
                names.add("...");
                return names;
            default:
                break;
        }

        // 第一个参数
        names.add(lexer.nextIdentifier().getValue());

        // ,
        while (lexer.LookAhead() == TOKEN_SEP_COMMA) {
            lexer.nextToken();
            // 参数标识符
            if (lexer.LookAhead() == TOKEN_IDENTIFIER) {
                names.add(lexer.nextIdentifier().getValue());
            }
            // 变长参数
            else {
                lexer.nextTokenOfKind(TOKEN_VARARG);
                names.add("...");
                break;
            }
        }

        return names;
    }

    /**
     * 解析函数定义表达式
     *
     * functiondef ::= function funcbody
     * funcbody ::= ‘(’ [parlist] ‘)’ block end
     *
     * @param lexer
     * @return
     */
    static FuncDefExp parseFuncDefExp(Lexer lexer) {

        // function
        int line = lexer.line();
        // (
        lexer.nextTokenOfKind(TOKEN_SEP_LPAREN);
        // [parlist]
        List<String> parList = parseParList(lexer);
        // )
        lexer.nextTokenOfKind(TOKEN_SEP_RPAREN);
        // block
        Block block = parseBlock(lexer);
        // end
        lexer.nextTokenOfKind(TOKEN_KW_END);
        int lastLine = lexer.line();

        FuncDefExp fdExp = new FuncDefExp();
        fdExp.setLine(line);
        fdExp.setLastLine(lastLine);
        fdExp.setVararg(parList.remove("..."));
        fdExp.setParList(parList);
        fdExp.setBlock(block);
        return fdExp;
    }

    /**
     * 解析表构造表达式
     *
     * tableconstructor ::= ‘{’ [fieldlist] ‘}’
     *
     * @param lexer
     * @return
     */
    static TableConstructorExp parseTableConstructorExp(Lexer lexer) {
        TableConstructorExp tcExp = new TableConstructorExp();
        tcExp.setLine(lexer.line());
        // {
        lexer.nextTokenOfKind(TOKEN_SEP_LCURLY);
        // [fieldlist]
        parseFieldList(lexer, tcExp);
        // }
        lexer.nextTokenOfKind(TOKEN_SEP_RCURLY);
        tcExp.setLastLine(lexer.line());
        return tcExp;
    }

    /**
     * 解析可选字段列表
     *
     * fieldlist ::= field {fieldsep field} [fieldsep]
     *
     * @param lexer
     * @param tcExp
     */
    private static void parseFieldList(Lexer lexer, TableConstructorExp tcExp) {
        if (lexer.LookAhead() != TOKEN_SEP_RCURLY) {
            // field
            parseField(lexer, tcExp);

            while (isFieldSep(lexer.LookAhead())) {

                // fieldsep
                lexer.nextToken();
                if (lexer.LookAhead() != TOKEN_SEP_RCURLY) {

                    // field
                    parseField(lexer, tcExp);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * 处理字段分隔符（逗号或分号）
     *
     * fieldsep ::= ‘,’ | ‘;’
     *
     * @param kind
     * @return
     */
    private static boolean isFieldSep(TokenKind kind) {
        return kind == TOKEN_SEP_COMMA || kind == TOKEN_SEP_SEMI;
    }

    /**
     * field ::= ‘[’ exp ‘]’ ‘=’ exp | Name ‘=’ exp | exp
     *
     * @param lexer
     * @param tcExp
     */
    private static void parseField(Lexer lexer, TableConstructorExp tcExp) {
        if (lexer.LookAhead() == TOKEN_SEP_LBRACK) {
            // [
            lexer.nextToken();
            // exp
            tcExp.addKey(parseExp(lexer));
            // ]
            lexer.nextTokenOfKind(TOKEN_SEP_RBRACK);
            // =
            lexer.nextTokenOfKind(TOKEN_OP_ASSIGN);
            // exp
            tcExp.addVal(parseExp(lexer));
            return;
        }

        BaseExp exp = parseExp(lexer);
        if (exp instanceof NameExp) {
            if (lexer.LookAhead() == TOKEN_OP_ASSIGN) {
                // Name ‘=’ exp => ‘[’ LiteralString ‘]’ = exp
                tcExp.addKey(new StringExp(exp.getLine(), ((NameExp) exp).getName()));
                lexer.nextToken();
                tcExp.addVal(parseExp(lexer));
                return;
            }
        }

        tcExp.addKey(null);
        tcExp.addVal(exp);
    }

}
