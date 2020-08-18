package com.ywh.jua.state;


import com.ywh.jua.api.ArithOp;
import com.ywh.jua.number.LuaMath;

import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;

/**
 * 算数、按位运算操作
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public class Arithmetic {

    // 运算操作分为整数和浮点数两类，都用函数式接口数组存放

    private static final LongBinaryOperator[] INTEGER_OPS = {
        // LUA_OPADD
        Long::sum,

        // LUA_OPSUB
        (a, b) -> a - b,

        // LUA_OPMUL
        (a, b) -> a * b,

        // LUA_OPMOD
        Math::floorMod,

        // LUA_OPPOW
        null,

        // LUA_OPDIV
        null,

        // LUA_OPIDIV
        Math::floorDiv,

        // LUA_OPBAND
        (a, b) -> a & b,

        // LUA_OPBOR
        (a, b) -> a | b,

        // LUA_OPBXOR
        (a, b) -> a ^ b,

        // LUA_OPSHL
        LuaMath::shiftLeft,

        // LUA_OPSHR
        LuaMath::shiftRight,

        // LUA_OPUNM
        (a, b) -> -a,

        // LUA_OPBNOT
        (a, b) -> ~a,
    };

    private static final DoubleBinaryOperator[] FLOAT_OPS = {

        // LUA_OPADD
        Double::sum,

        // LUA_OPSUB
        (a, b) -> a - b,

        // LUA_OPMUL
        (a, b) -> a * b,

        // LUA_OPMOD
        LuaMath::floorMod,

        // LUA_OPPOW
        Math::pow,

        // LUA_OPDIV
        (a, b) -> a / b,

        // LUA_OPIDIV
        LuaMath::floorDiv,

        // LUA_OPBAND
        null,

        // LUA_OPBOR
        null,

        // LUA_OPBXOR
        null,

        // LUA_OPSHL
        null,

        // LUA_OPSHR
        null,

        // LUA_OPUNM
        (a, b) -> -a,

        // LUA_OPBNOT
        null,
    };

    /**
     * 运算
     *
     * @param a
     * @param b
     * @param op
     * @return
     */
    static Object arith(Object a, Object b, ArithOp op) {

        // 通过枚举常量序数获取具体的运算函数
        LongBinaryOperator integerFunc = INTEGER_OPS[op.ordinal()];
        DoubleBinaryOperator floatFunc = FLOAT_OPS[op.ordinal()];

        // 位运算（bitwise）
        if (floatFunc == null) {
            Long x = LuaValue.toInteger(a), y = LuaValue.toInteger(b);
            if (x != null && y != null) {
                return integerFunc.applyAsLong(x, y);
            }
        }
        // 算数运算（arith）
        else { //
            if (integerFunc != null) {
                if (a instanceof Long && b instanceof Long) {
                    return integerFunc.applyAsLong((Long) a, (Long) b);
                }
            }
            Double x = LuaValue.toFloat(a), y = LuaValue.toFloat(b);
            if (x != null && y != null) {
                return floatFunc.applyAsDouble(x, y);
            }
        }
        return null;
    }

}
