package com.ywh.jua.state;

/**
 * 比较操作
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
class Comparison {

    /**
     * 等于
     *
     * @param a
     * @param b
     * @return
     */
    static boolean eq(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else if (a instanceof Boolean || a instanceof String) {
            return a.equals(b);
        } else if (a instanceof Long) {
            return a.equals(b) || (b instanceof Double && b.equals(((Long) a).doubleValue()));
        } else if (a instanceof Double) {
            return a.equals(b) || (b instanceof Long && a.equals(((Long) b).doubleValue()));
        } else {
            return a == b;
        }
    }

    /**
     * 小于
     *
     * @param a
     * @param b
     * @return
     */
    static boolean lt(Object a, Object b) {
        if (a instanceof String && b instanceof String) {
            return ((String) a).compareTo((String) b) < 0;
        }
        if (a instanceof Long) {
            if (b instanceof Long) {
                return ((Long) a) < ((Long) b);
            } else if (b instanceof Double) {
                return ((Long) a).doubleValue() < ((Double) b);
            }
        }
        if (a instanceof Double) {
            if (b instanceof Double) {
                return ((Double) a) < ((Double) b);
            } else if (b instanceof Long) {
                return ((Double) a) < ((Long) b).doubleValue();
            }
        }
        throw new RuntimeException("comparison error!");
    }

    /**
     * 小于等于
     *
     * @param a
     * @param b
     * @return
     */
    static boolean le(Object a, Object b) {
        if (a instanceof String && b instanceof String) {
            return ((String) a).compareTo((String) b) <= 0;
        }
        if (a instanceof Long) {
            if (b instanceof Long) {
                return ((Long) a) <= ((Long) b);
            } else if (b instanceof Double) {
                return ((Long) a).doubleValue() <= ((Double) b);
            }
        }
        if (a instanceof Double) {
            if (b instanceof Double) {
                return ((Double) a) <= ((Double) b);
            } else if (b instanceof Long) {
                return ((Double) a) <= ((Long) b).doubleValue();
            }
        }
        throw new RuntimeException("comparison error!");
    }

}
