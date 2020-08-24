package com.ywh.jua.compiler.lexer;

import java.util.regex.Pattern;
import static com.ywh.jua.compiler.lexer.TokenKind.*;


/**
 * 词法分析器
 *
 * @author ywh
 * @since 2020/8/24 11:26
 */
public class Lexer {

     private static final Pattern RE_SPACES = Pattern.compile("^\\s+");

    private static final Pattern RE_NEW_LINE = Pattern.compile("\r\n|\n\r|\n|\r");

    private static final Pattern RE_IDENTIFIER = Pattern.compile("^[_\\d\\w]+");

    private static final Pattern RE_NUMBER = Pattern.compile("^0[xX][0-9a-fA-F]*(\\.[0-9a-fA-F]*)?([pP][+\\-]?[0-9]+)?|^[0-9]*(\\.[0-9]*)?([eE][+\\-]?[0-9]+)?");

    private static final Pattern RE_SHORT_STR = Pattern.compile("(?s)(^'(\\\\\\\\|\\\\'|\\\\\\n|\\\\z\\s*|[^'\\n])*')|(^\"(\\\\\\\\|\\\\\"|\\\\\\n|\\\\z\\s*|[^\"\\n])*\")");

    private static final Pattern RE_OPENING_LONG_BRACKET = Pattern.compile("^\\[=*\\[");

    /**
     * 源代码
     */
    private CharSeq chunk;

    /**
     * 源文件名
     */
    private String chunkName;

    /**
     * 当前行号
     */
    private int line;

    /**
     * token 缓存（如果不希望直接跳过下一个 token，只是分析类型，则可以把它缓存下来、备份状态）
     */
    private Token cachedNextToken;

    /**
     * 备份行号
     */
	private int lineBackup;

    public Lexer(String chunk, String chunkName) {
        this.chunk = new CharSeq(chunk);
        this.chunkName = chunkName;
        this.line = 1;
    }

    /**
     * 取行号
     *
     * @return
     */
    public int line() {
    	return cachedNextToken != null ? lineBackup : line;
    }

    <T> T error(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        msg = String.format("%s:%d: %s", chunkName, line(), msg);
        throw new RuntimeException(msg);
    }

    /**
     * 取缓存（下一个） token 的类型。
     *
     * @return
     */
    public TokenKind LookAhead() {
        // 如果缓存不存在，则跳过下一个 token，并备份 token 和行号。
        if (cachedNextToken == null) {
            lineBackup = line;
            cachedNextToken = nextToken();
        }
        return cachedNextToken.getKind();
    }

    public Token nextIdentifier() {
        return nextTokenOfKind(TOKEN_IDENTIFIER);
    }

    public Token nextTokenOfKind(TokenKind kind) {
        Token token = nextToken();
        if (token.getKind() != kind) {
            error("syntax error near '%s'", token.getValue());
        }
        return token;
    }

    /**
     * 跳过空白字符和注释，取下一个 Token（行号和类型）。
     *
     * @return
     */
    public Token nextToken() {
        // 如果缓存不为空，则取缓存的 token，把缓存清空并返回。
        if (cachedNextToken != null) {
            Token token = cachedNextToken;
            cachedNextToken = null;
            return token;
        }

        // 跳过空白字符
        skipWhiteSpaces();
        if (chunk.length() <= 0) {
            return new Token(line, TOKEN_EOF, "EOF");
        }

        switch (chunk.charAt(0)) {
            case ';':
                chunk.next(1); return new Token(line, TOKEN_SEP_SEMI,   ";");
            case ',':
                chunk.next(1); return new Token(line, TOKEN_SEP_COMMA,  ",");
            case '(':
                chunk.next(1); return new Token(line, TOKEN_SEP_LPAREN, "(");
            case ')':
                chunk.next(1); return new Token(line, TOKEN_SEP_RPAREN, ")");
            case ']':
                chunk.next(1); return new Token(line, TOKEN_SEP_RBRACK, "]");
            case '{':
                chunk.next(1); return new Token(line, TOKEN_SEP_LCURLY, "{");
            case '}':
                chunk.next(1); return new Token(line, TOKEN_SEP_RCURLY, "}");
            case '+':
                chunk.next(1); return new Token(line, TOKEN_OP_ADD,     "+");
            case '-':
                chunk.next(1); return new Token(line, TOKEN_OP_MINUS,   "-");
            case '*':
                chunk.next(1); return new Token(line, TOKEN_OP_MUL,     "*");
            case '^':
                chunk.next(1); return new Token(line, TOKEN_OP_POW,     "^");
            case '%':
                chunk.next(1); return new Token(line, TOKEN_OP_MOD,     "%");
            case '&':
                chunk.next(1); return new Token(line, TOKEN_OP_BAND,    "&");
            case '|':
                chunk.next(1); return new Token(line, TOKEN_OP_BOR,     "|");
            case '#':
                chunk.next(1); return new Token(line, TOKEN_OP_LEN,     "#");
            case ':':
                if (chunk.startsWith("::")) {
                    chunk.next(2);
                    return new Token(line, TOKEN_SEP_LABEL, "::");
                } else {
                    chunk.next(1);
                    return new Token(line, TOKEN_SEP_COLON, ":");
                }
            case '/':
                if (chunk.startsWith("//")) {
                    chunk.next(2);
                    return new Token(line, TOKEN_OP_IDIV, "//");
                } else {
                    chunk.next(1);
                    return new Token(line, TOKEN_OP_DIV, "/");
                }
            case '~':
                if (chunk.startsWith("~=")) {
                    chunk.next(2);
                    return new Token(line, TOKEN_OP_NE, "~=");
                } else {
                    chunk.next(1);
                    return new Token(line, TOKEN_OP_WAVE, "~");
                }
            case '=':
                if (chunk.startsWith("==")) {
                    chunk.next(2);
                    return new Token(line, TOKEN_OP_EQ, "==");
                } else {
                    chunk.next(1);
                    return new Token(line, TOKEN_OP_ASSIGN, "=");
                }
            case '<':
                if (chunk.startsWith("<<")) {
                    chunk.next(2);
                    return new Token(line, TOKEN_OP_SHL, "<<");
                } else if (chunk.startsWith("<=")) {
                    chunk.next(2);
                    return new Token(line, TOKEN_OP_LE, "<=");
                } else {
                    chunk.next(1);
                    return new Token(line, TOKEN_OP_LT, "<");
                }
            case '>':
                if (chunk.startsWith(">>")) {
                    chunk.next(2);
                    return new Token(line, TOKEN_OP_SHR, ">>");
                } else if (chunk.startsWith(">=")) {
                    chunk.next(2);
                    return new Token(line, TOKEN_OP_GE, ">=");
                } else {
                    chunk.next(1);
                    return new Token(line, TOKEN_OP_GT, ">");
                }
            case '.':
                if (chunk.startsWith("...")) {
                    chunk.next(3);
                    return new Token(line, TOKEN_VARARG, "...");
                } else if (chunk.startsWith("..")) {
                    chunk.next(2);
                    return new Token(line, TOKEN_OP_CONCAT, "..");
                } else if (chunk.length() == 1 || !CharUtil.isDigit(chunk.charAt(1))) {
                    chunk.next(1);
                    return new Token(line, TOKEN_SEP_DOT, ".");
                }
            case '[':
                if (chunk.startsWith("[[") || chunk.startsWith("[=")) {
                    return new Token(line, TOKEN_STRING, scanLongString());
                } else {
                    chunk.next(1);
                    return new Token(line, TOKEN_SEP_LBRACK, "[");
                }
            case '\'':
            case '"':
                return new Token(line, TOKEN_STRING, scanShortString());
            default:
                break;
        }

        // 判断当前字符
        char c = chunk.charAt(0);

        // 处理数字字面量
        if (c == '.' || CharUtil.isDigit(c)) {
            return new Token(line, TOKEN_NUMBER, scanNumber());
        }

        // 处理标识符和关键字
        if (c == '_' || CharUtil.isLetter(c)) {
            String id = scanIdentifier();
            // 如果匹配到标识符，则取该标识符；否则取 TOKEN_IDENTIFIER。
            return new Token(line, Token.KEYWORDS.getOrDefault(id, TOKEN_IDENTIFIER), id);
            // return Token.KEYWORDS.containsKey(id) ? new Token(line, Token.KEYWORDS.get(id), id): new Token(line, TOKEN_IDENTIFIER, id);

        }
        return error("unexpected symbol near %c", c);
    }

    /**
     * 跳过空白字符
     */
    private void skipWhiteSpaces() {
        while (chunk.length() > 0) {

            // 跳过注释
            if (chunk.startsWith("--")) {
                skipComment();
            }
            // 跳过换行符
            else if (chunk.startsWith("\r\n") || chunk.startsWith("\n\r")) {
                chunk.next(2);
                line += 1;
            }
            // 结束一行
            else if (CharUtil.isNewLine(chunk.charAt(0))) {
                chunk.next(1);
                line += 1;
            }
            // 跳过空白字符
            else if(CharUtil.isWhiteSpace(chunk.charAt(0))) {
                chunk.next(1);
            } else {
                break;
            }
        }
    }

    /**
     * 跳过注释
     */
    private void skipComment() {
        // 跳过两个减号
        chunk.next(2);

        // 长注释，跳过一个长字符串。
        if (chunk.startsWith("[")) {
            if (chunk.find(RE_OPENING_LONG_BRACKET) != null) {
                scanLongString();
                return;
            }
        }

        // 短注释，跳过换行符前所有字符。
        while(chunk.length() > 0 && !CharUtil.isNewLine(chunk.charAt(0))) {
            chunk.next(1);
        }
    }

    /**
     *
     * @return
     */
    private String scanIdentifier() {
        return scan(RE_IDENTIFIER);
    }

    /**
     * 提取数字
     *
     * @return
     */
    private String scanNumber() {
        return scan(RE_NUMBER);
    }

    /**
     *
     * @param pattern
     * @return
     */
    private String scan(Pattern pattern) {
        String token = chunk.find(pattern);
        if (token == null) {
            throw new RuntimeException("unreachable!");
        }
        chunk.next(token.length());
        return token;
    }

    /**
     * 截取长字符串
     * Lua 中支持多种方式表示长字符串：
     *      a = 'alo\n123"'
     *      a = "alo\n123\""
     *      '97lo\10\04923"'
     *      a = [[alo
     *      123"]]
     *      a = [==[
     *      alo
     *      123"]==]
     *
     *
     * @return
     */
    private String scanLongString() {
        // 查找左右长方括号，如果找不到则表示存在语法错误。
        String openingLongBracket = chunk.find(RE_OPENING_LONG_BRACKET);
        if (openingLongBracket == null) {
            return error("invalid long string delimiter near '%s'", chunk.substring(0, 2));
        }

        // 截取字符串字面量，把左右长方括号去除，在 chunk 中跳过这个字符串。
        String closingLongBracket = openingLongBracket.replace("[", "]");
        int closingLongBracketIdx = chunk.indexOf(closingLongBracket);
        if (closingLongBracketIdx < 0) {
            return error("unfinished long string or comment");
        }
        String str = chunk.substring(openingLongBracket.length(), closingLongBracketIdx);
        chunk.next(closingLongBracketIdx + closingLongBracket.length());

        // 把换行符序列统一转换成 \n
        str = RE_NEW_LINE.matcher(str).replaceAll("\n");
        line += str.chars().filter(c -> c == '\n').count();

        // 把第一个换行符去除
        if (str.startsWith("\n")) {
            str = str.substring(1);
        }
        return str;
    }

    /**
     * 截取短字符串
     *
     * @return
     */
    private String scanShortString() {
        // 查找短字符串
        String str = chunk.find(RE_SHORT_STR);
        if (str != null) {
            // 在 chunk 中跳过这个字符串
            chunk.next(str.length());
            str = str.substring(1, str.length() - 1);
            // 处理转义符转义符
            if (str.indexOf('\\') >= 0) {
                line += RE_NEW_LINE.split(str).length - 1;
                str = new Escaper(str, this).escape();
            }
            return str;
        }
        return error("unfinished string");
    }

}
