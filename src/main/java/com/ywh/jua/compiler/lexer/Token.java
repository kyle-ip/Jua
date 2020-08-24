package com.ywh.jua.compiler.lexer;

import java.util.HashMap;
import java.util.Map;

/**
 * Token
 *
 * @author ywh
 * @since 2020/8/24 11:26
 */
public class Token {


    /**
     * 关键字与常量值的对应关系
     */
    static final Map<String, TokenKind> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("and", TokenKind.TOKEN_OP_AND);
        KEYWORDS.put("break", TokenKind.TOKEN_KW_BREAK);
        KEYWORDS.put("do", TokenKind.TOKEN_KW_DO);
        KEYWORDS.put("else", TokenKind.TOKEN_KW_ELSE);
        KEYWORDS.put("elseif", TokenKind.TOKEN_KW_ELSEIF);
        KEYWORDS.put("end", TokenKind.TOKEN_KW_END);
        KEYWORDS.put("false", TokenKind.TOKEN_KW_FALSE);
        KEYWORDS.put("for", TokenKind.TOKEN_KW_FOR);
        KEYWORDS.put("function", TokenKind.TOKEN_KW_FUNCTION);
        KEYWORDS.put("goto", TokenKind.TOKEN_KW_GOTO);
        KEYWORDS.put("if", TokenKind.TOKEN_KW_IF);
        KEYWORDS.put("in", TokenKind.TOKEN_KW_IN);
        KEYWORDS.put("local", TokenKind.TOKEN_KW_LOCAL);
        KEYWORDS.put("nil", TokenKind.TOKEN_KW_NIL);
        KEYWORDS.put("not", TokenKind.TOKEN_OP_NOT);
        KEYWORDS.put("or", TokenKind.TOKEN_OP_OR);
        KEYWORDS.put("repeat", TokenKind.TOKEN_KW_REPEAT);
        KEYWORDS.put("return", TokenKind.TOKEN_KW_RETURN);
        KEYWORDS.put("then", TokenKind.TOKEN_KW_THEN);
        KEYWORDS.put("true", TokenKind.TOKEN_KW_TRUE);
        KEYWORDS.put("until", TokenKind.TOKEN_KW_UNTIL);
        KEYWORDS.put("while", TokenKind.TOKEN_KW_WHILE);
    }

    private final int line;

    private final TokenKind kind;

    private final String value;

    public Token(int line, TokenKind kind, String value) {
        this.line = line;
        this.kind = kind;
        this.value = value;
    }

    public static Map<String, TokenKind> getKeywords() {
        return KEYWORDS;
    }

    public int getLine() {
        return line;
    }

    public TokenKind getKind() {
        return kind;
    }

    public String getValue() {
        return value;
    }
}
