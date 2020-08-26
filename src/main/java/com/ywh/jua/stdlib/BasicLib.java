package com.ywh.jua.stdlib;


import com.ywh.jua.api.JavaFunction;
import com.ywh.jua.api.LuaState;
import com.ywh.jua.api.LuaType;
import com.ywh.jua.api.ThreadStatus;

import java.util.HashMap;
import java.util.Map;

import static com.ywh.jua.api.LuaState.LUA_MULTRET;
import static com.ywh.jua.api.LuaType.*;
import static com.ywh.jua.api.ThreadStatus.LUA_OK;
import static com.ywh.jua.constant.MetaConstant.METATABLE;
import static com.ywh.jua.constant.MetaConstant.PAIRS;
import static com.ywh.jua.constant.TokenConstant.LEN;

/**
 * 基础库
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public class BasicLib {


    /**
     * 基础库函数
     */
    private static final Map<String, JavaFunction> BASE_FUNCS = new HashMap<>();

    static {
        BASE_FUNCS.put("print", BasicLib::basePrint);
        BASE_FUNCS.put("assert", BasicLib::baseAssert);
        BASE_FUNCS.put("error", BasicLib::baseError);
        BASE_FUNCS.put("select", BasicLib::baseSelect);
        BASE_FUNCS.put("ipairs", BasicLib::baseIpairs);
        BASE_FUNCS.put("pairs", BasicLib::basePairs);
        BASE_FUNCS.put("next", BasicLib::baseNext);
        BASE_FUNCS.put("load", BasicLib::baseLoad);
        BASE_FUNCS.put("loadfile", BasicLib::baseLoadFile);
        BASE_FUNCS.put("dofile", BasicLib::baseDoFile);
        BASE_FUNCS.put("pcall", BasicLib::basePcall);
        BASE_FUNCS.put("xpcall", BasicLib::baseXpcall);
        BASE_FUNCS.put("getmetatable", BasicLib::baseGetMetatable);
        BASE_FUNCS.put("setmetatable", BasicLib::baseSetMetatable);
        BASE_FUNCS.put("rawequal", BasicLib::baseRawEqual);
        BASE_FUNCS.put("rawlen", BasicLib::baseRawLen);
        BASE_FUNCS.put("rawget", BasicLib::baseRawGet);
        BASE_FUNCS.put("rawset", BasicLib::baseRawSet);
        BASE_FUNCS.put("type", BasicLib::baseType);
        BASE_FUNCS.put("tostring", BasicLib::baseToString);
        BASE_FUNCS.put("tonumber", BasicLib::baseToNumber);
        /* placeholders */
        BASE_FUNCS.put("_G", null);
        BASE_FUNCS.put("_VERSION", null);
    }

    /**
     * 启用基础库
     *
     * @param ls
     * @return
     */
    public static int openBaseLib(LuaState ls) {
        /* open lib into global table */
        ls.pushGlobalTable();
        ls.setFuncs(BASE_FUNCS, 0);
        /* set global _G */
        ls.pushValue(-1);
        ls.setField(-2, "_G");
        /* set global _VERSION */
        // TODO
        ls.pushString("Lua 5.3");
        ls.setField(-2, "_VERSION");
        return 1;
    }

    /**
     * print (···)
     * http://www.lua.org/manual/5.3/manual.html#pdf-print
     * lua-5.3.4/src/lbaselib.c#luaB_print()
     *
     * @param ls
     * @return
     */
    private static int basePrint(LuaState ls) {
        /* number of arguments */
        int n = ls.getTop();
        ls.getGlobal("tostring");
        for (int i = 1; i <= n; i++) {
            /* function to be called */
            ls.pushValue(-1);
            /* value to print */
            ls.pushValue(i);
            ls.call(1, 1);
            /* get result */
            String s = ls.toString(-1);
            if (s == null) {
                return ls.error2("'tostring' must return a string to 'print'");
            }
            if (i > 1) {
                System.out.print("\t");
            }
            System.out.print(s);
            /* pop result */
            ls.pop(1);
        }
        System.out.println();
        return 0;
    }

    /**
     * assert (v [, message])
     * http://www.lua.org/manual/5.3/manual.html#pdf-assert
     * lua-5.3.4/src/lbaselib.c#luaB_assert()
     *
     * @param ls
     * @return
     */
    private static int baseAssert(LuaState ls) {
        /* condition is true? */
        if (ls.toBoolean(1)) {
            /* return all arguments */
            return ls.getTop();
        }
        /* error */
        else {
            /* there must be a condition */
            ls.checkAny(1);
            /* remove it */
            ls.remove(1);
            /* default message */
            ls.pushString("assertion failed!");
            /* leave only message (default if no other one) */
            ls.setTop(1);
            /* call 'error' */
            return baseError(ls);
        }
    }


    /**
     * error (message [, level])
     * http://www.lua.org/manual/5.3/manual.html#pdf-error
     * lua-5.3.4/src/lbaselib.c#luaB_error()
     *
     * @param ls
     * @return
     */
    private static int baseError(LuaState ls) {
        long level = ls.optInteger(2, 1);
        ls.setTop(1);
        if (ls.type(1) == LUA_TSTRING && level > 0) {
            // ls.where(level) /* add extra information */
            // ls.pushValue(1)
            // ls.concat(2)
        }
        return ls.error();
    }


    /**
     * 接收一个固定参数 index 和人一个可选参数，如果 index 是数字，该函数返回从 index 开始的所有可选参数。
     * <p>
     * select (index, ···)
     * http://www.lua.org/manual/5.3/manual.html#pdf-select
     * lua-5.3.4/src/lbaselib.c#luaB_select()
     * <p>
     * select(1, "a", "b", "c")     =>  a b c
     * select(2, "a", "b", "c")     =>  b c
     * select("#", "a", "b", "c")   =>  3
     * select(-1, "a", "b", "c")    =>  c
     *
     * @param ls
     * @return
     */
    private static int baseSelect(LuaState ls) {
        long n = ls.getTop();
        if (ls.type(1) == LUA_TSTRING && LEN.equals(ls.checkString(1))) {
            ls.pushInteger(n - 1);
            return 1;
        } else {
            long i = ls.checkInteger(1);
            if (i < 0) {
                i = n + i;
            } else if (i > n) {
                i = n;
            }
            ls.argCheck(1 <= i, 1, "index out of range");
            return (int) (n - i);
        }
    }


    /**
     * ipairs (t)
     * http://www.lua.org/manual/5.3/manual.html#pdf-ipairs
     * lua-5.3.4/src/lbaselib.c#luaB_ipairs()
     *
     * @param ls
     * @return
     */
    private static int baseIpairs(LuaState ls) {
        ls.checkAny(1);
        /* iteration function */
        ls.pushJavaFunction(BasicLib::iPairsAux);
        /* state */
        ls.pushValue(1);
        /* initial value */
        ls.pushInteger(0);
        return 3;
    }

    /**
     * @param ls
     * @return
     */
    private static int iPairsAux(LuaState ls) {
        long i = ls.checkInteger(2) + 1;
        ls.pushInteger(i);
        return ls.getI(1, i) == LUA_TNIL ? 1 : 2;
    }


    /**
     * pairs (t)
     * http://www.lua.org/manual/5.3/manual.html#pdf-pairs
     * lua-5.3.4/src/lbaselib.c#luaB_pairs()
     *
     * @param ls
     * @return
     */
    private static int basePairs(LuaState ls) {
        ls.checkAny(1);
        /* no metamethod? */
        if (ls.getMetafield(1, PAIRS) == LUA_TNIL) {
            /* will return generator, */
            ls.pushJavaFunction(BasicLib::baseNext);
            /* state, */
            ls.pushValue(1);
            ls.pushNil();
        } else {
            /* argument 'self' to metamethod */
            ls.pushValue(1);
            /* get 3 values from metamethod */
            ls.call(1, 3);
        }
        return 3;
    }

    /**
     * next (table [, index])
     * http://www.lua.org/manual/5.3/manual.html#pdf-next
     * lua-5.3.4/src/lbaselib.c#luaB_next()
     *
     * @param ls
     * @return
     */
    private static int baseNext(LuaState ls) {
        ls.checkType(1, LUA_TTABLE);
        /* create a 2nd argument if there isn't one */
        ls.setTop(2);
        if (ls.next(1)) {
            return 2;
        } else {
            ls.pushNil();
            return 1;
        }
    }

    /**
     * load (chunk [, chunkname [, mode [, env]]])
     * http://www.lua.org/manual/5.3/manual.html#pdf-load
     * lua-5.3.4/src/lbaselib.c#luaB_load()
     *
     * @param ls
     * @return
     */
    private static int baseLoad(LuaState ls) {
        String chunk = ls.toString(1);
        String mode = ls.optString(3, "bt");
        /* 'env' index or 0 if no 'env' */
        int env = !ls.isNone(4) ? 4 : 0;
        /* loading a string? */
        if (chunk != null) {
            String chunkname = ls.optString(2, chunk);
            ThreadStatus status = ls.load(chunk.getBytes(), chunkname, mode);
            return loadAux(ls, status, env);
        }
        /* loading from a reader function */
        else {
            // TODO
            throw new RuntimeException("loading from a reader function");
        }
    }

    /**
     * lua-5.3.4/src/lbaselib.c#load_aux()
     *
     * @param ls
     * @param status
     * @param envIdx
     * @return
     */
    private static int loadAux(LuaState ls, ThreadStatus status, int envIdx) {
        if (status == LUA_OK) {
            /* 'env' parameter? */
            if (envIdx != 0) {
                throw new RuntimeException("todo!");
            }
            return 1;
        }
        /* error (message is on top of the stack) */
        else {
            ls.pushNil();
            /* put before error message */
            ls.insert(-2);
            /* return nil plus error message */
            return 2;
        }
    }

    /**
     * loadfile ([filename [, mode [, env]]])
     * http://www.lua.org/manual/5.3/manual.html#pdf-loadfile
     * lua-5.3.4/src/lbaselib.c#luaB_loadfile()
     *
     * @param ls
     * @return
     */
    private static int baseLoadFile(LuaState ls) {
        String fname = ls.optString(1, "");
        String mode = ls.optString(1, "bt");
        /* 'env' index or 0 if no 'env' */
        int env = !ls.isNone(3) ? 3 : 0;
        ThreadStatus status = ls.loadFileX(fname, mode);
        return loadAux(ls, status, env);
    }

    /**
     * dofile ([filename])
     * http://www.lua.org/manual/5.3/manual.html#pdf-dofile
     * lua-5.3.4/src/lbaselib.c#luaB_dofile()
     *
     * @param ls
     * @return
     */
    private static int baseDoFile(LuaState ls) {
        String fname = ls.optString(1, "bt");
        ls.setTop(1);
        if (ls.loadFile(fname) != LUA_OK) {
            return ls.error();
        }
        ls.call(0, LUA_MULTRET);
        return ls.getTop() - 1;
    }

    /**
     * pcall (f [, arg1, ···])
     * http://www.lua.org/manual/5.3/manual.html#pdf-pcall
     *
     * @param ls
     * @return
     */
    private static int basePcall(LuaState ls) {
        int nArgs = ls.getTop() - 1;
        ThreadStatus status = ls.pCall(nArgs, -1, 0);
        ls.pushBoolean(status == LUA_OK);
        ls.insert(1);
        return ls.getTop();
    }


    /**
     * xpcall (f, msgh [, arg1, ···])
     * http://www.lua.org/manual/5.3/manual.html#pdf-xpcall
     *
     * @param ls
     * @return
     */
    private static int baseXpcall(LuaState ls) {
        throw new RuntimeException("todo!");
    }


    /**
     * getmetatable (object)
     * http://www.lua.org/manual/5.3/manual.html#pdf-getmetatable
     * lua-5.3.4/src/lbaselib.c#luaB_getmetatable()
     *
     * @param ls
     * @return
     */
    private static int baseGetMetatable(LuaState ls) {
        ls.checkAny(1);
        if (!ls.getMetatable(1)) {
            ls.pushNil();
            /* no metatable */
            return 1;
        }
        ls.getMetafield(1, "__metatable");
        /* returns either __metatable field (if present) or metatable */
        return 1;

    }

    /**
     * setmetatable (table, metatable)
     * http://www.lua.org/manual/5.3/manual.html#pdf-setmetatable
     * lua-5.3.4/src/lbaselib.c#luaB_setmetatable()
     *
     * @param ls
     * @return
     */
    private static int baseSetMetatable(LuaState ls) {
        LuaType t = ls.type(2);
        ls.checkType(1, LUA_TTABLE);
        ls.argCheck(t == LUA_TNIL || t == LUA_TTABLE, 2,
            "nil or table expected");
        if (ls.getMetafield(1, METATABLE) != LUA_TNIL) {
            return ls.error2("cannot change a protected metatable");
        }
        ls.setTop(2);
        ls.setMetatable(1);
        return 1;
    }

    /**
     * rawequal (v1, v2)
     * http://www.lua.org/manual/5.3/manual.html#pdf-rawequal
     * lua-5.3.4/src/lbaselib.c#luaB_rawequal()
     *
     * @param ls
     * @return
     */
    private static int baseRawEqual(LuaState ls) {
        ls.checkAny(1);
        ls.checkAny(2);
        ls.pushBoolean(ls.rawEqual(1, 2));
        return 1;
    }

    //
    //
    //

    /**
     * rawlen (v)
     * http://www.lua.org/manual/5.3/manual.html#pdf-rawlen
     * lua-5.3.4/src/lbaselib.c#luaB_rawlen()
     *
     * @param ls
     * @return
     */
    private static int baseRawLen(LuaState ls) {
        LuaType t = ls.type(1);
        ls.argCheck(t == LUA_TTABLE || t == LUA_TSTRING, 1, "table or string expected");
        ls.pushInteger(ls.rawLen(1));
        return 1;
    }

    /**
     * rawget (table, index)
     * http://www.lua.org/manual/5.3/manual.html#pdf-rawget
     * lua-5.3.4/src/lbaselib.c#luaB_rawget()
     *
     * @param ls
     * @return
     */
    private static int baseRawGet(LuaState ls) {
        ls.checkType(1, LUA_TTABLE);
        ls.checkAny(2);
        ls.setTop(2);
        ls.rawGet(1);
        return 1;
    }

    /**
     * rawset (table, index, value)
     * http://www.lua.org/manual/5.3/manual.html#pdf-rawset
     * lua-5.3.4/src/lbaselib.c#luaB_rawset()
     *
     * @param ls
     * @return
     */
    private static int baseRawSet(LuaState ls) {
        ls.checkType(1, LUA_TTABLE);
        ls.checkAny(2);
        ls.checkAny(3);
        ls.setTop(3);
        ls.rawSet(1);
        return 1;
    }

    /**
     * type (v)
     * http://www.lua.org/manual/5.3/manual.html#pdf-type
     * lua-5.3.4/src/lbaselib.c#luaB_type()
     *
     * @param ls
     * @return
     */
    private static int baseType(LuaState ls) {
        LuaType t = ls.type(1);
        ls.argCheck(t != LUA_TNONE, 1, "value expected");
        ls.pushString(ls.typeName(t));
        return 1;
    }


    /**
     * tostring (v)
     * http://www.lua.org/manual/5.3/manual.html#pdf-tostring
     * lua-5.3.4/src/lbaselib.c#luaB_tostring()
     *
     * @param ls
     * @return
     */
    private static int baseToString(LuaState ls) {
        ls.checkAny(1);
        ls.toString2(1);
        return 1;
    }


    /**
     * tonumber (e [, base])
     * http://www.lua.org/manual/5.3/manual.html#pdf-tonumber
     * lua-5.3.4/src/lbaselib.c#luaB_tonumber()
     *
     * @param ls
     * @return
     */
    private static int baseToNumber(LuaState ls) {
        /* standard conversion? */
        if (ls.isNoneOrNil(2)) {
            ls.checkAny(1);
            /* already a number? */
            if (ls.type(1) == LUA_TNUMBER) {
                /* yes; return it */
                ls.setTop(1);
                return 1;
            } else {
                String s = ls.toString(1);
                if (s != null) {
                    if (ls.stringToNumber(s)) {
                        /* successful conversion to number */
                        return 1;
                    }
                    /* else not a number */
                }
            }
        } else {
            /* no numbers as strings */
            ls.checkType(1, LUA_TSTRING);
            String s = ls.toString(1).trim();
            int base = (int) ls.checkInteger(2);
            ls.argCheck(2 <= base && base <= 36, 2, "base out of range");
            try {
                long n = Long.parseLong(s, base);
                ls.pushInteger(n);
                return 1;
            } catch (NumberFormatException e) {
                /* else not a number */
            }
        } /* else not a number */
        ls.pushNil();
        return 1;
    }

}
