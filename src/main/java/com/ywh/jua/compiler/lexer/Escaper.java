package com.ywh.jua.compiler.lexer;

import java.util.regex.Pattern;

/**
 * 转义符处理
 *
 * @author ywh
 * @since 2020/8/24 11:26
 */
class Escaper {

    private static final Pattern RE_DEC_ESCAPE_SEQ = Pattern.compile("^\\\\[0-9]{1,3}");

    private static final Pattern RE_HEX_ESCAPE_SEQ = Pattern.compile("^\\\\x[0-9a-fA-F]{2}");

    private static final Pattern RE_UNICODE_ESCAPE_SEQ = Pattern.compile("^\\\\u\\{[0-9a-fA-F]+}");

    private final CharSeq rawStr;

    private final Lexer lexer;

    private final StringBuilder buf = new StringBuilder();

    Escaper(String rawStr, Lexer lexer) {
        this.rawStr = new CharSeq(rawStr);
        this.lexer = lexer;
    }

    String escape() {
        while (rawStr.length() > 0) {
            if (rawStr.charAt(0) != '\\') {
                buf.append(rawStr.nextChar());
                continue;
            }

            if (rawStr.length() == 1) {
                return lexer.error("unfinished string");
            }

            switch (rawStr.charAt(1)) {
                case 'a':
                    buf.append((char) 0x07);
                    rawStr.next(2);
                    // Bell
                    continue;
                case 'v':
                    buf.append((char) 0x0B);
                    rawStr.next(2);
                    // Vertical tab
                    continue;
                case 'b':
                    buf.append('\b');
                    rawStr.next(2);
                    continue;
                case 'f':
                    buf.append('\f');
                    rawStr.next(2);
                    continue;
                case 'n':
                    buf.append('\n');
                    rawStr.next(2);
                    continue;
                case 'r':
                    buf.append('\r');
                    rawStr.next(2);
                    continue;
                case 't':
                    buf.append('\t');
                    rawStr.next(2);
                    continue;
                case '"':
                    buf.append('"');
                    rawStr.next(2);
                    continue;
                case '\'':
                    buf.append('\'');
                    rawStr.next(2);
                    continue;
                case '\\':
                    buf.append('\\');
                    rawStr.next(2);
                    continue;
                case '\n':
                    buf.append('\n');
                    rawStr.next(2);
                    continue;
                // \ddd
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    escapeDecSeq();
                    continue;
                // \xXX
                case 'x':
                    escapeHexSeq();
                    continue;
                // \ u{XXX}
                case 'u':
                    escapeUnicodeSeq();
                    continue;
                // \ u{XXX}
                case 'z':
                    rawStr.next(2);
                    skipWhitespaces();
                    continue;
                default:
                    break;
            }
            reportInvalidEscapeSeq();
        }

        return buf.toString();
    }

    private void reportInvalidEscapeSeq() {
        lexer.error("invalid escape sequence near '\\%c'", rawStr.charAt(1));
    }

    /**
     * 提取表示数字的字符序列（\ddd），转换为整数。
     */
    private void escapeDecSeq() {
        String seq = rawStr.find(RE_DEC_ESCAPE_SEQ);
        if (seq == null) {
            reportInvalidEscapeSeq();
        }

        try {
            assert seq != null;
            int d = Integer.parseInt(seq.substring(1));
            if (d <= 0xFF) {
                buf.append((char) d);
                rawStr.next(seq.length());
                return;
            }
        } catch (NumberFormatException ignored) {}
        lexer.error("decimal escape too large near '%s'", seq);
    }

    /**
     * 提取表示十六进制整数的字符序列（\xXX），转换为整数（超过 OxFF 则报错）。
     */
    private void escapeHexSeq() {
        String seq = rawStr.find(RE_HEX_ESCAPE_SEQ);
        if (seq == null) {
            reportInvalidEscapeSeq();
        }

        assert seq != null;
        int d = Integer.parseInt(seq.substring(2), 16);
        buf.append((char) d);
        rawStr.next(seq.length());
    }

    /**
     * 提取表示 Unicode 的字符序列，转换为整数（\ u{XXX}）
     */
    private void escapeUnicodeSeq() {
        String seq = rawStr.find(RE_UNICODE_ESCAPE_SEQ);
        if (seq == null) {
            reportInvalidEscapeSeq();
        }

        try {
            assert seq != null;
            int d = Integer.parseInt(seq.substring(3, seq.length() - 1), 16);
            if (d <= 0x10FFFF) {
                buf.appendCodePoint(d);
                rawStr.next(seq.length());
                return;
            }
        } catch (NumberFormatException ignored) {}

        lexer.error("UTF-8 value too large near '%s'", seq);
    }

    /**
     * 跳过空白字符
     */
    private void skipWhitespaces() {
        while (rawStr.length() > 0 && CharUtil.isWhiteSpace(rawStr.charAt(0))) {
            rawStr.next(1);
        }
    }

}
