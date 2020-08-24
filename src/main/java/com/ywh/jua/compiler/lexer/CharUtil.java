package com.ywh.jua.compiler.lexer;

/**
 * 字符工具
 *
 * @author ywh
 * @since 2020/8/24 11:26
 */
class CharUtil {

    /**
     * 空白字符
     *
     * @param c
     * @return
     */
    static boolean isWhiteSpace(char c) {
        switch (c) {
            case '\t':
            case '\n':
            case 0x0B: // \v
            case '\f':
            case '\r':
            case ' ':
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * 换行符
     *
     * @param c
     * @return
     */
    static boolean isNewLine(char c) {
        return c == '\r' || c == '\n';
    }

    /**
     * 数字
     *
     * @param c
     * @return
     */
    static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * 字母
     *
     * @param c
     * @return
     */
    static boolean isLetter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

}
