package com.ywh.jua.number;

/**
 * Lua 运算
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public class LuaMath {

    /**
     * 整除
     *
     * @param a
     * @param b
     * @return
     */
    public static double floorDiv(double a, double b) {
        return Math.floor(a / b);
    }

    /**
     * 取模：a % b == a - ((a // b) * b)
     *
     * @param a
     * @param b
     * @return
     */
    public static double floorMod(double a, double b) {
        if (a > 0 && b == Double.POSITIVE_INFINITY || a < 0 && b == Double.NEGATIVE_INFINITY) {
            return a;
        }
        if (a > 0 && b == Double.NEGATIVE_INFINITY || a < 0 && b == Double.POSITIVE_INFINITY) {
            return b;
        }
        return a - Math.floor(a / b) * b;
    }

    /**
     * 左移
     *
     * @param a
     * @param n
     * @return
     */
    public static long shiftLeft(long a, long n) {
        return n >= 0 ? a << n : a >>> -n;
    }

    /**
     * 右移
     *
     * @param a
     * @param n
     * @return
     */
    public static long shiftRight(long a, long n) {
        return n >= 0 ? a >>> n : a << -n;
    }

}
