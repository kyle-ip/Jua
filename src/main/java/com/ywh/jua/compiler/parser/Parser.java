package com.ywh.jua.compiler.parser;


import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.lexer.Lexer;
import com.ywh.jua.compiler.lexer.TokenKind;

/**
 * 解析器
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class Parser {

    public static Block parse(String chunk, String chunkName) {
        Lexer lexer = new Lexer(chunk, chunkName);
        Block block = BlockParser.parseBlock(lexer);
        lexer.nextTokenOfKind(TokenKind.TOKEN_EOF);
        return block;
    }

}
