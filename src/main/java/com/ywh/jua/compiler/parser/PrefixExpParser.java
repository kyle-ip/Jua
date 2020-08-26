package com.ywh.jua.compiler.parser;

import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.exps.*;
import com.ywh.jua.compiler.lexer.Lexer;
import com.ywh.jua.compiler.lexer.Token;

import java.util.Collections;
import java.util.List;

import static com.ywh.jua.compiler.lexer.TokenKind.*;
import static com.ywh.jua.compiler.parser.ExpParser.*;

/**
 * 前缀表达式解析器
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
class PrefixExpParser {

    /**
     * 解析前缀表达式
     * 只能以标识符或左圆括号开始
     *
     * prefixexp ::= Name
     *     | ‘(’ exp ‘)’
     *     | prefixexp ‘[’ exp ‘]’
     *     | prefixexp ‘.’ Name
     *     | prefixexp [‘:’ Name] args
     *
     * @param lexer
     * @return
     */
    static BaseExp parsePrefixExp(Lexer lexer) {
        BaseExp exp;
        // 先前瞻一个 token，根据情况解析出标识符或圆括号表达式。
        if (lexer.LookAhead() == TOKEN_IDENTIFIER) {
            // Name
            Token id = lexer.nextIdentifier();
            exp = new NameExp(id.getLine(), id.getValue());
        } else { // ‘(’ exp ‘)’
            exp = parseParensExp(lexer);
        }
        // 完成后续解析。
        return finishPrefixExp(lexer, exp);
    }

    /**
     * 解析圆括号表达式
     *
     * @param lexer
     * @return
     */
    private static BaseExp parseParensExp(Lexer lexer) {
        // (
        lexer.nextTokenOfKind(TOKEN_SEP_LPAREN);
        // exp
        BaseExp exp = parseExp(lexer);
        // )
        lexer.nextTokenOfKind(TOKEN_SEP_RPAREN);

        if (exp instanceof VarargExp || exp instanceof FuncCallExp || exp instanceof NameExp || exp instanceof TableAccessExp) {
            return new ParensExp(exp);
        }

        // no need to keep parens
        return exp;
    }

    /**
     *
     * @param lexer
     * @param exp
     * @return
     */
    private static BaseExp finishPrefixExp(Lexer lexer, BaseExp exp) {
        while (true) {
            switch (lexer.LookAhead()) {
                // prefixexp ‘[’ exp ‘]’
                case TOKEN_SEP_LBRACK: {
                    // ‘[’
                    lexer.nextToken();
                    // exp
                    BaseExp keyExp = parseExp(lexer);
                    // ‘]’
                    lexer.nextTokenOfKind(TOKEN_SEP_RBRACK);
                    exp = new TableAccessExp(lexer.line(), exp, keyExp);
                    break;
                }
                // prefixexp ‘.’ Name
                case TOKEN_SEP_DOT: {
                    // ‘.’
                    lexer.nextToken();
                    // Name
                    Token name = lexer.nextIdentifier();
                    BaseExp keyExp = new StringExp(name);
                    exp = new TableAccessExp(name.getLine(), exp, keyExp);
                    break;
                }
                // prefixexp ‘:’ Name args
                case TOKEN_SEP_COLON:
                case TOKEN_SEP_LPAREN:
                case TOKEN_SEP_LCURLY:
                // prefixexp args
                case TOKEN_STRING:
                    exp = finishFuncCallExp(lexer, exp);
                    break;
                default:
                    return exp;
            }
        }
    }

    /**
     * 解析函数调用表达式
     *
     * functioncall ::=  prefixexp args | prefixexp ‘:’ Name args
     *
     * @param lexer
     * @param prefixExp
     * @return
     */
    private static FuncCallExp finishFuncCallExp(Lexer lexer, BaseExp prefixExp) {
        FuncCallExp fcExp = new FuncCallExp();
        fcExp.setPrefixExp(prefixExp);
        fcExp.setNameExp(parseNameExp(lexer));

        // TODO
        fcExp.setLine(lexer.line());
        fcExp.setArgs(parseArgs(lexer));
        fcExp.setLastLine(lexer.line());
        return fcExp;
    }

    /**
     * 解析名称表达式
     *
     * @param lexer
     * @return
     */
    private static StringExp parseNameExp(Lexer lexer) {
        if (lexer.LookAhead() == TOKEN_SEP_COLON) {
            lexer.nextToken();
            Token name = lexer.nextIdentifier();
            return new StringExp(name);
        }
        return null;
    }

    /**
     * 解析函数参数
     *
     * args ::=  ‘(’ [explist] ‘)’ | tableconstructor | LiteralString
     *
     * @param lexer
     * @return
     */
    private static List<BaseExp> parseArgs(Lexer lexer) {
        switch (lexer.LookAhead()) {
            // ‘(’ [explist] ‘)’
            case TOKEN_SEP_LPAREN:
                // TOKEN_SEP_LPAREN
                lexer.nextToken();
                List<BaseExp> args = null;
                if (lexer.LookAhead() != TOKEN_SEP_RPAREN) {
                    args = parseExpList(lexer);
                }
                lexer.nextTokenOfKind(TOKEN_SEP_RPAREN);
                return args;
            // ‘{’ [fieldlist] ‘}’
            case TOKEN_SEP_LCURLY:
                return Collections.singletonList(parseTableConstructorExp(lexer));
            // LiteralString
            default:
                Token str = lexer.nextTokenOfKind(TOKEN_STRING);
                return Collections.singletonList(new StringExp(str));
        }
    }

}
