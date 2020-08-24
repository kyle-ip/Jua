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

    // private static final Pattern reSpaces = Pattern.compile("^\\s+");

    private static final Pattern reNewLine = Pattern.compile("\r\n|\n\r|\n|\r");

    private static final Pattern reIdentifier = Pattern.compile("^[_\\d\\w]+");

    private static final Pattern reNumber = Pattern.compile("^0[xX][0-9a-fA-F]*(\\.[0-9a-fA-F]*)?([pP][+\\-]?[0-9]+)?|^[0-9]*(\\.[0-9]*)?([eE][+\\-]?[0-9]+)?");

    private static final Pattern reShortStr = Pattern.compile("(?s)(^'(\\\\\\\\|\\\\'|\\\\\\n|\\\\z\\s*|[^'\\n])*')|(^\"(\\\\\\\\|\\\\\"|\\\\\\n|\\\\z\\s*|[^\"\\n])*\")");

    private static final Pattern reOpeningLongBracket = Pattern.compile("^\\[=*\\[");

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

    // to support lookahead
	private Token cachedNextToken;

	private int lineBackup;

    public Lexer(String chunk, String chunkName) {
        this.chunk = new CharSeq(chunk);
        this.chunkName = chunkName;
        this.line = 1;
    }

    public int line() {
    	return cachedNextToken != null ? lineBackup : line;
    }

    <T> T error(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        msg = String.format("%s:%d: %s", chunkName, line(), msg);
        throw new RuntimeException(msg);
    }

    public TokenKind LookAhead() {
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

    public Token nextToken() {
        if (cachedNextToken != null) {
            Token token = cachedNextToken;
            cachedNextToken = null;
            return token;
        }

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

        char c = chunk.charAt(0);
        if (c == '.' || CharUtil.isDigit(c)) {
            return new Token(line, TOKEN_NUMBER, scanNumber());
        }
        if (c == '_' || CharUtil.isLetter(c)) {
            String id = scanIdentifier();
            return Token.KEYWORDS.containsKey(id) ? new Token(line, Token.KEYWORDS.get(id), id): new Token(line, TOKEN_IDENTIFIER, id);
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
            if (chunk.find(reOpeningLongBracket) != null) {
                scanLongString();
                return;
            }
        }

        // 短注释，跳过换行符前所有字符。
        while(chunk.length() > 0 && !CharUtil.isNewLine(chunk.charAt(0))) {
            chunk.next(1);
        }
    }

    private String scanIdentifier() {
        return scan(reIdentifier);
    }

    private String scanNumber() {
        return scan(reNumber);
    }

    private String scan(Pattern pattern) {
        String token = chunk.find(pattern);
        if (token == null) {
            throw new RuntimeException("unreachable!");
        }
        chunk.next(token.length());
        return token;
    }

    private String scanLongString() {
        String openingLongBracket = chunk.find(reOpeningLongBracket);
        if (openingLongBracket == null) {
            return error("invalid long string delimiter near '%s'", chunk.substring(0, 2));
        }

        String closingLongBracket = openingLongBracket.replace("[", "]");
        int closingLongBracketIdx = chunk.indexOf(closingLongBracket);
        if (closingLongBracketIdx < 0) {
            return error("unfinished long string or comment");
        }

        String str = chunk.substring(openingLongBracket.length(), closingLongBracketIdx);
        chunk.next(closingLongBracketIdx + closingLongBracket.length());

        str = reNewLine.matcher(str).replaceAll("\n");
        line += str.chars().filter(c -> c == '\n').count();
        if (str.startsWith("\n")) {
            str = str.substring(1);
        }

        return str;
    }

    private String scanShortString() {
        String str = chunk.find(reShortStr);
        if (str != null) {
            chunk.next(str.length());
            str = str.substring(1, str.length() - 1);
            if (str.indexOf('\\') >= 0) {
                line += reNewLine.split(str).length - 1;
                str = new Escaper(str, this).escape();
            }
            return str;
        }
        return error("unfinished string");
    }

}
