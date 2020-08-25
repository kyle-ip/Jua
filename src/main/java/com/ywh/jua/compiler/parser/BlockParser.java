package com.ywh.jua.compiler.parser;


import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.Exp;
import com.ywh.jua.compiler.ast.Stat;
import com.ywh.jua.compiler.ast.stats.EmptyStat;
import com.ywh.jua.compiler.lexer.Lexer;
import com.ywh.jua.compiler.lexer.TokenKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ywh.jua.compiler.parser.ExpParser.parseExpList;
import static com.ywh.jua.compiler.parser.StatParser.parseStat;


/**
 * 块解析器
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
class BlockParser {

    /**
     * 解析块（递归下降）
     * 由于最终可能会调用到 {@link StatParser #parseDoStat} 等函数，块和语句解析函数存在递归调用关系。
     *
     * block ::= {stat} [retstat]
     *
     * @param lexer
     * @return
     */
    static Block parseBlock(Lexer lexer) {
        Block block = new Block();

        // 解析语句序列。
        block.setStats(parseStats(lexer));

        // 解析（可选的）返回语句。
        block.setRetExps(parseRetExps(lexer));

        // 记录末尾行号。
        block.setLastLine(lexer.line());
        return block;
    }

    /**
     * 解析语句序列
     *
     * @param lexer
     * @return
     */
    private static List<Stat> parseStats(Lexer lexer) {
        List<Stat> stats = new ArrayList<>();

        // 逐个 token 取出解析，直到块结束
        while (!isReturnOrBlockEnd(lexer.LookAhead())) {
            Stat stat = parseStat(lexer);
            if (!(stat instanceof EmptyStat)) {
                stats.add(stat);
            }
        }
        return stats;
    }

    /**
     * 前瞻判断关键字 return 或块结束标识符
     *
     * @param kind
     * @return
     */
    private static boolean isReturnOrBlockEnd(TokenKind kind) {
        switch (kind) {
            // 块的后面只能是以 return, EOF, end, else, elseif, until 结束。
            case TOKEN_KW_RETURN:
            case TOKEN_EOF:
            case TOKEN_KW_END:
            case TOKEN_KW_ELSE:
            case TOKEN_KW_ELSEIF:
            case TOKEN_KW_UNTIL:
                return true;
            default:
                return false;
        }
    }

    /**
     * 解析可选的返回语句
     *
     * retstat ::= return [explist] [‘;’]
     * explist ::= exp {‘,’ exp}
     *
     * @param lexer
     * @return
     */
    private static List<Exp> parseRetExps(Lexer lexer) {

        // 前瞻下一个 token，如果不是 return 则表示没有返回语句，直接返回 null。
        if (lexer.LookAhead() != TokenKind.TOKEN_KW_RETURN) {
            return null;
        }

        // 跳过 return，并前瞻下一个 token。
        lexer.nextToken();
        switch (lexer.LookAhead()) {
            // 块已结束，则返回语句没有任何表达式，返回空的表达式列表。
            case TOKEN_EOF:
            case TOKEN_KW_END:
            case TOKEN_KW_ELSE:
            case TOKEN_KW_ELSEIF:
            case TOKEN_KW_UNTIL:
                return Collections.emptyList();
            // 块已结束，跳过分号，返回空的表达式列表。
            case TOKEN_SEP_SEMI:
                lexer.nextToken();
                return Collections.emptyList();
            // 块未结束，解析表达式列表。
            default:
                List<Exp> exps = parseExpList(lexer);
                // 跳过分号。
                if (lexer.LookAhead() == TokenKind.TOKEN_SEP_SEMI) {
                    lexer.nextToken();
                }
                return exps;
        }
    }

}
