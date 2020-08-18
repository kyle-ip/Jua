package com.ywh.jua.number;

/**
 * Lua 数值处理
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public class LuaNumber {

    /**
     * 判断浮点数是整数
     *
     * @param f
     * @return
     */
    public static boolean isInteger(double f) {
        return f % 1 == 0;
//        return f == (long) f;
    }

    /**
     * 字符串转整数
     * TODO
     *
     * @param str
     * @return
     */
    public static Long parseInteger(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 字符串转浮点数
     * TODO
     *
     * @param str
     * @return
     */
    public static Double parseFloat(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
