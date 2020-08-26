package com.ywh.jua.stdlib;

import com.ywh.jua.api.JavaFunction;
import com.ywh.jua.api.LuaState;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.ywh.jua.api.CmpOp.LUA_OPLT;
import static com.ywh.jua.api.LuaType.LUA_TNUMBER;

/**
 * 数学库
 *
 * @author ywh
 * @since 2020/8/26/026
 */
public class MathLib {

    private static final Map<String, JavaFunction> MATH_FUNCS = new HashMap<>();

    static {
        MATH_FUNCS.put("random", MathLib::mathRandom);
        MATH_FUNCS.put("randomseed", MathLib::mathRandomseed);
        MATH_FUNCS.put("max", MathLib::mathMax);
        MATH_FUNCS.put("min", MathLib::mathMin);
        MATH_FUNCS.put("exp", MathLib::mathExp);
        MATH_FUNCS.put("log", MathLib::mathLog);
        MATH_FUNCS.put("deg", MathLib::mathDeg);
        MATH_FUNCS.put("rad", MathLib::mathRad);
        MATH_FUNCS.put("sin", MathLib::mathSin);
        MATH_FUNCS.put("cos", MathLib::mathCos);
        MATH_FUNCS.put("tan", MathLib::mathTan);
        MATH_FUNCS.put("asin", MathLib::mathAsin);
        MATH_FUNCS.put("acos", MathLib::mathAcos);
        MATH_FUNCS.put("atan", MathLib::mathAtan);
        MATH_FUNCS.put("ceil", MathLib::mathCeil);
        MATH_FUNCS.put("floor", MathLib::mathFloor);
        MATH_FUNCS.put("abs", MathLib::mathAbs);
        MATH_FUNCS.put("sqrt", MathLib::mathSqrt);
        MATH_FUNCS.put("fmod", MathLib::mathFmod);
        MATH_FUNCS.put("modf", MathLib::mathModf);
        MATH_FUNCS.put("ult", MathLib::mathUlt);
        MATH_FUNCS.put("tointeger", MathLib::mathToInt);
        MATH_FUNCS.put("type", MathLib::mathType);
        MATH_FUNCS.put("pi", null);
        MATH_FUNCS.put("huge", null);
        MATH_FUNCS.put("maxinteger", null);
        MATH_FUNCS.put("mininteger", null);
    }

    /**
     * 启用基础库
     *
     * @param ls
     * @return
     */
    public static int openMathLib(LuaState ls) {
        ls.newLib(MATH_FUNCS);
        ls.pushNumber(Math.PI);
        ls.setField(-2, "pi");
        ls.pushNumber(Double.POSITIVE_INFINITY);
        ls.setField(-2, "huge");
        ls.pushInteger(Integer.MAX_VALUE);
        ls.setField(-2, "maxinteger");
        ls.pushInteger(Integer.MIN_VALUE);
        ls.setField(-2, "mininteger");
        return 1;
    }

    /**
     * 随机数
     * <p>
     * math.random ([m [, n]])
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.random
     * lua-5.3.4/src/lmathlib.c#math_random()
     *
     * @param ls
     * @return
     */
    private static int mathRandom(LuaState ls) {
        Random r = new Random();
        long low, up;
        switch (ls.getTop()) {
            case 0:
                ls.pushNumber(r.nextDouble());
                return 1;
            case 1:
                low = 1;
                up = ls.checkInteger(1);
                break;
            case 2:
                low = ls.checkInteger(1);
                up = ls.checkInteger(2);
                break;
            default:
                return ls.error2("wrong number of arguments");
        }
        ls.argCheck(low <= up, 1, "interval is empty");
        ls.argCheck(low >= 0 || up <= Long.MAX_VALUE + low, 1, "interval too large");
        ls.pushInteger(low + r.nextLong());
        return 1;
    }

    /**
     * math.randomseed (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.randomseed
     * lua-5.3.4/src/lmathlib.c#math_randomseed()
     *
     * @param ls
     * @return
     */
    private static int mathRandomseed(LuaState ls) {
        double x = ls.checkNumber(1);
        Random r = new Random();
        r.setSeed((int) x);
        return 0;
    }

    /**
     * math.max (x, ···)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.max
     * lua-5.3.4/src/lmathlib.c#math_max()
     *
     * @param ls
     * @return
     */
    private static int mathMax(LuaState ls) {
        int n = ls.getTop();
        int imax = 1;
        ls.argCheck(n >= 1, 1, "value expected");
        for (int i = 2; i < n; i++) {
            if (ls.compare(imax, i, LUA_OPLT)) {
                imax = i;
            }
        }
        ls.pushValue(imax);
        return 1;
    }

    /**
     * math.min (x, ···)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.min
     * lua-5.3.4/src/lmathlib.c#math_min()
     *
     * @param ls
     * @return
     */
    private static int mathMin(LuaState ls) {
        int n = ls.getTop();
        int imin = 1;
        ls.argCheck(n >= 1, 1, "value expected");
        for (int i = 2; i < n; i++) {
            if (ls.compare(i, imin, LUA_OPLT)) {
                imin = i;
            }
        }
        ls.pushValue(imin);
        return 1;
    }

    /**
     * math.exp (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.exp
     * lua-5.3.4/src/lmathlib.c#math_exp()
     *
     * @param ls
     * @return
     */
    private static int mathExp(LuaState ls) {
        double x = ls.checkNumber(1);
        ls.pushNumber(Math.exp(x));
        return 1;
    }

    /**
     * math.log (x [, base])
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.log
     * lua-5.3.4/src/lmathlib.c#math_log()
     *
     * @param ls
     * @return
     */
    private static int mathLog(LuaState ls) {
        double x = ls.checkNumber(1);
        double res;
        if (ls.isNoneOrNil(2)) {
            res = Math.log(x);
        } else {
            double base = ls.toNumber(2);
            res = Math.log(x) / Math.log(base);
        }
        ls.pushNumber(res);
        return 1;
    }

    /**
     * math.deg (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.deg
     * lua-5.3.4/src/lmathlib.c#math_deg()
     *
     * @param ls
     * @return
     */
    private static int mathDeg(LuaState ls) {
        double x = ls.checkNumber(1);
        ls.pushNumber(x * 180 / Math.PI);
        return 1;
    }

    /**
     * math.rad (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.rad
     * lua-5.3.4/src/lmathlib.c#math_rad()
     *
     * @param ls
     * @return
     */
    private static int mathRad(LuaState ls) {
        double x = ls.checkNumber(1);
        ls.pushNumber(x * Math.PI / 180);
        return 1;
    }

    /**
     * math.sin (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.sin
     * lua-5.3.4/src/lmathlib.c#math_sin()
     *
     * @param ls
     * @return
     */
    private static int mathSin(LuaState ls) {
        double x = ls.checkNumber(1);
        ls.pushNumber(Math.sin(x));
        return 1;
    }

    /**
     * math.cos (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.cos
     * lua-5.3.4/src/lmathlib.c#math_cos()
     *
     * @param ls
     * @return
     */
    private static int mathCos(LuaState ls) {
        double x = ls.checkNumber(1);
        ls.pushNumber(Math.cos(x));
        return 1;
    }

    /**
     * math.tan (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.tan
     * lua-5.3.4/src/lmathlib.c#math_tan()
     *
     * @param ls
     * @return
     */
    private static int mathTan(LuaState ls) {
        double x = ls.checkNumber(1);
        ls.pushNumber(Math.tan(x));
        return 1;
    }

    /**
     * math.asin (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.asin
     * lua-5.3.4/src/lmathlib.c#math_asin()
     *
     * @param ls
     * @return
     */
    private static int mathAsin(LuaState ls) {
        double x = ls.checkNumber(1);
        ls.pushNumber(Math.asin(x));
        return 1;
    }

    /**
     * math.acos (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.acos
     * lua-5.3.4/src/lmathlib.c#math_acos()
     *
     * @param ls
     * @return
     */
    private static int mathAcos(LuaState ls) {
        double x = ls.checkNumber(1);
        ls.pushNumber(Math.acos(x));
        return 1;
    }

    /**
     * math.atan (y [, x])
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.atan
     * lua-5.3.4/src/lmathlib.c#math_atan()
     *
     * @param ls
     * @return
     */
    private static int mathAtan(LuaState ls) {
        double x = ls.checkNumber(1);
        ls.pushNumber(Math.acos(x));
        return 1;
    }

    /**
     * math.ceil (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.ceil
     * lua-5.3.4/src/lmathlib.c#math_ceil()
     *
     * @param ls
     * @return
     */
    private static int mathCeil(LuaState ls) {
        if (ls.isInteger(1)) {
            ls.setTop(1);
        } else {
            double x = ls.checkNumber(1);
            ls.pushInteger((int) Math.ceil(x));
        }
        return 1;
    }

    /**
     * math.floor (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.floor
     * lua-5.3.4/src/lmathlib.c#math_floor()
     *
     * @param ls
     * @return
     */
    private static int mathFloor(LuaState ls) {
        if (ls.isInteger(1)) {
            ls.setTop(1);
        } else {
            double x = ls.checkNumber(1);
            ls.pushInteger((int) Math.floor(x));
        }
        return 1;
    }

    /**
     * math.fmod (x, y)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.fmod
     * lua-5.3.4/src/lmathlib.c#math_fmod()
     *
     * @param ls
     * @return
     */
    private static int mathFmod(LuaState ls) {
        if (ls.isInteger(1) && ls.isInteger(2)) {
            long d = ls.toInteger(2);
            /* special cases: -1 or 0 */
            if (d == 0 || d == -1) {
                ls.argCheck(d != 0, 2, "zero");
                /* avoid overflow with 0x80000... / -1 */
                ls.pushInteger(0);
            } else {
                ls.pushInteger(ls.toInteger(1) % d);
            }
        } else {
            double x = ls.checkNumber(1);
            double y = ls.checkNumber(2);
            ls.pushNumber(x - ((int) (x / y)) * y);
        }
        return 1;
    }

    /**
     * math.modf (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.modf
     * lua-5.3.4/src/lmathlib.c#math_modf()
     *
     * @param ls
     * @return
     */
    private static int mathModf(LuaState ls) {
        if (ls.isInteger(1)) {
            ls.setTop(1);
            ls.pushNumber(0);
        } else {
            double x = ls.checkNumber(1);
            ls.pushNumber((int) x);
            if (x == Double.POSITIVE_INFINITY) {
                ls.pushNumber(0);
            } else {
                ls.pushNumber(getDecimal(x));
            }
        }
        return 2;
    }

    /**
     * math.abs (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.abs
     * lua-5.3.4/src/lmathlib.c#math_abs()
     *
     * @param ls
     * @return
     */
    private static int mathAbs(LuaState ls) {
        if (ls.isInteger(1)) {
            long x = ls.toInteger(1);
            if (x < 0) {
                ls.pushInteger(-x);
            }
        } else {
            double x = ls.checkNumber(1);
            ls.pushNumber(Math.abs(x));
        }
        return 1;
    }

    /**
     * math.sqrt (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.sqrt
     * lua-5.3.4/src/lmathlib.c#math_sqrt()
     *
     * @param ls
     * @return
     */
    private static int mathSqrt(LuaState ls) {
        double x = ls.checkNumber(1);
        ls.pushNumber(Math.sqrt(x));
        return 1;
    }

    /**
     * math.ult (m, n)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.ult
     * lua-5.3.4/src/lmathlib.c#math_ult()
     *
     * @param ls
     * @return
     */
    private static int mathUlt(LuaState ls) {
        long m = ls.checkInteger(1);
        long n = ls.checkInteger(2);
        ls.pushBoolean(m < n);
        return 1;
    }

    /**
     * math.tointeger (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.tointeger
     * lua-5.3.4/src/lmathlib.c#math_toint()
     *
     * @param ls
     * @return
     */
    private static int mathToInt(LuaState ls) {
        Long i = ls.toIntegerX(1);
        if (i != null) {
            ls.pushInteger(i);
        } else {
            ls.checkAny(1);
            /* value is not convertible to integer */
            ls.pushNil();
        }
        return 1;
    }

    /**
     * math.type (x)
     * http://www.lua.org/manual/5.3/manual.html#pdf-math.type
     * lua-5.3.4/src/lmathlib.c#math_type()
     *
     * @param ls
     * @return
     */
    private static int mathType(LuaState ls) {
        if (ls.type(1) == LUA_TNUMBER) {
            if (ls.isInteger(1)) {
                ls.pushString("integer");
            } else {
                ls.pushString("float");
            }
        } else {
            ls.checkAny(1);
            ls.pushNil();
        }
        return 1;
    }

    /**
     * 取小数部分
     *
     * @param x
     * @return
     */
    private static double getDecimal(double x) {
        String s = String.valueOf(x);
        return Double.parseDouble(s.substring(s.indexOf(".")));
    }
}
