package com.ywh.jua.compiler.lexer;

/**
 * Token 类型
 *
 * @author ywh
 * @since 2020/8/24 11:26
 */
public enum TokenKind {

    /**
     * end-of-file
     */
    TOKEN_EOF,

    /**
     * ...
     */
    TOKEN_VARARG,

    /**
     * ;
     */
    TOKEN_SEP_SEMI,

    /**
     * ,
     */
    TOKEN_SEP_COMMA,

    /**
     * .
     */
    TOKEN_SEP_DOT,

    /**
     * :
     */
    TOKEN_SEP_COLON,

    /**
     * ::
     */
    TOKEN_SEP_LABEL,

    /**
     * (
     */
    TOKEN_SEP_LPAREN,

    /**
     * )
     */
    TOKEN_SEP_RPAREN,

    /**
     * [
     */
    TOKEN_SEP_LBRACK,

    /**
     * ]
     */
    TOKEN_SEP_RBRACK,

    /**
     * {
     */
    TOKEN_SEP_LCURLY,

    /**
     * }
     */
    TOKEN_SEP_RCURLY,

    /**
     * =
     */
    TOKEN_OP_ASSIGN,

    /**
     * - (sub or unm)，tokenize 阶段无法判断减号是二元减法运算符或一元取负运算符。
     */
    TOKEN_OP_MINUS,

    /**
     * ~ (bnot or bxor)
     */
    TOKEN_OP_WAVE,

    /**
     * +
     */
    TOKEN_OP_ADD,

    /**
     * *
     */
    TOKEN_OP_MUL,

    /**
     * /
     */
    TOKEN_OP_DIV,

    /**
     * //
     */
    TOKEN_OP_IDIV,

    /**
     * ^
     */
    TOKEN_OP_POW,

    /**
     * %
     */
    TOKEN_OP_MOD,

    /**
     * &
     */
    TOKEN_OP_BAND,

    /**
     * |
     */
    TOKEN_OP_BOR,

    /**
     * >>
     */
    TOKEN_OP_SHR,

    /**
     * <<
     */
    TOKEN_OP_SHL,

    /**
     * ..
     */
    TOKEN_OP_CONCAT,

    /**
     * <
     */
    TOKEN_OP_LT,

    /**
     * <=
     */
    TOKEN_OP_LE,

    /**
     * >
     */
    TOKEN_OP_GT,

    /**
     * >=
     */
    TOKEN_OP_GE,

    /**
     * ==
     */
    TOKEN_OP_EQ,

    /**
     * ~=
     */
    TOKEN_OP_NE,

    /**
     * #
     */
    TOKEN_OP_LEN,

    /**
     * and
     */
    TOKEN_OP_AND,

    /**
     * or
     */
    TOKEN_OP_OR,

    /**
     * not
     */
    TOKEN_OP_NOT,

    /**
     * break
     */
    TOKEN_KW_BREAK,

    /**
     * do
     */
    TOKEN_KW_DO,

    /**
     * else
     */
    TOKEN_KW_ELSE,

    /**
     * elseif
     */
    TOKEN_KW_ELSEIF,

    /**
     * end
     */
    TOKEN_KW_END,

    /**
     * false
     */
    TOKEN_KW_FALSE,

    /**
     * for
     */
    TOKEN_KW_FOR,

    /**
     * function
     */
    TOKEN_KW_FUNCTION,

    /**
     * goto
     */
    TOKEN_KW_GOTO,

    /**
     * if
     */
    TOKEN_KW_IF,

    /**
     * in
     */
    TOKEN_KW_IN,

    /**
     * local
     */
    TOKEN_KW_LOCAL,

    /**
     * nil
     */
    TOKEN_KW_NIL,

    /**
     * repeat
     */
    TOKEN_KW_REPEAT,

    /**
     * return
     */
    TOKEN_KW_RETURN,

    /**
     * then
     */
    TOKEN_KW_THEN,

    /**
     * true
     */
    TOKEN_KW_TRUE,

    /**
     * until
     */
    TOKEN_KW_UNTIL,

    /**
     * while
     */
    TOKEN_KW_WHILE,

    /**
     * identifier
     */
    TOKEN_IDENTIFIER,

    /**
     * number literal
     */
    TOKEN_NUMBER,

    /**
     * string literal
     */
    TOKEN_STRING,

    /**
     * = TOKEN_OP_MINUS // unary minus
     */
    TOKEN_OP_UNM,

    /**
     * = TOKEN_OP_MINUS
     */
    TOKEN_OP_SUB,

    /**
     * = TOKEN_OP_WAVE
     */
    TOKEN_OP_BNOT,

    /**
     * = TOKEN_OP_WAVE
     */
    TOKEN_OP_BXOR,
    ;

}
