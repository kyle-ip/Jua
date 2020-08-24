package com.ywh.jua.compiler.lexer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lua 源码字符序列
 *
 * @author ywh
 * @since 2020/8/24 11:26
 */
class CharSeq implements CharSequence {

    /**
     * 源码字符串
     */
    private String str;

    /**
     * 偏移量
     */
    private int pos;

    CharSeq(String str) {
        this.str = str;
    }

    // TODO rename
    char nextChar() {
        return str.charAt(pos++);
    }

    /**
     * 跳过 n 个字符
     *
     * @param n
     */
    void next(int n) {
        pos += n;
    }

    /**
     * 从 pos 开始，以 prefix 开头
     *
     * @param prefix
     * @return
     */
    boolean startsWith(String prefix) {
        return str.startsWith(prefix, pos);
    }

    /**
     * 从 pos 开始，s 在 str 首次出现首字母位置
     *
     * @param s
     * @return
     */
    int indexOf(String s) {
        return str.indexOf(s, pos) - pos;
    }

    /**
     *
     * @param beginIndex
     * @param endIndex
     * @return
     */
    String substring(int beginIndex, int endIndex) {
        return str.substring(beginIndex + pos, endIndex + pos);
    }

    /**
     *
     * @param pattern
     * @return
     */
    String find(Pattern pattern) {
        Matcher matcher = pattern.matcher(this);
        return matcher.find()? matcher.group(0) : null;
    }

    /**
     * 剩余未访问字符个数
     *
     * @return
     */
    @Override
    public int length() {
        return str.length() - pos;
    }

    /**
     *
     * @param index
     * @return
     */
    @Override
    public char charAt(int index) {
        return str.charAt(index + pos);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return str.subSequence(start + pos, end + pos);
    }

}
