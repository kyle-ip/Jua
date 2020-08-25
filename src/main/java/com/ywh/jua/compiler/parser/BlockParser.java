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

class BlockParser {

    // block ::= {stat} [retstat]
    static Block parseBlock(Lexer lexer) {
        Block block = new Block();
        block.setStats(parseStats(lexer));
        block.setRetExps(parseRetExps(lexer));
        block.setLastLine(lexer.line());
        return block;
    }

    private static List<Stat> parseStats(Lexer lexer) {
        List<Stat> stats = new ArrayList<>();
        while (!isReturnOrBlockEnd(lexer.LookAhead())) {
            Stat stat = parseStat(lexer);
            if (!(stat instanceof EmptyStat)) {
                stats.add(stat);
            }
        }
        return stats;
    }

    private static boolean isReturnOrBlockEnd(TokenKind kind) {
        switch (kind) {
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

    // retstat ::= return [explist] [‘;’]
    // explist ::= exp {‘,’ exp}
    private static List<Exp> parseRetExps(Lexer lexer) {
        if (lexer.LookAhead() != TokenKind.TOKEN_KW_RETURN) {
            return null;
        }

        lexer.nextToken();
        switch (lexer.LookAhead()) {
            case TOKEN_EOF:
            case TOKEN_KW_END:
            case TOKEN_KW_ELSE:
            case TOKEN_KW_ELSEIF:
            case TOKEN_KW_UNTIL:
                return Collections.emptyList();
            case TOKEN_SEP_SEMI:
                lexer.nextToken();
                return Collections.emptyList();
            default:
                List<Exp> exps = parseExpList(lexer);
                if (lexer.LookAhead() == TokenKind.TOKEN_SEP_SEMI) {
                    lexer.nextToken();
                }
                return exps;
        }
    }

}
