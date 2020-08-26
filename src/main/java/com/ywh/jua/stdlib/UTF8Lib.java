package com.ywh.jua.stdlib;

import com.ywh.jua.api.JavaFunction;
import com.ywh.jua.api.LuaState;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ywh
 * @since 2020/8/26/026
 */
public class UTF8Lib {

    private static final Map<String, JavaFunction> UTF8_FUNCS = new HashMap<>();

    static {
        UTF8_FUNCS.put("len", UTF8Lib::utf8Len);
        UTF8_FUNCS.put("offset", null);
        UTF8_FUNCS.put("codepoint", null);
        UTF8_FUNCS.put("char", null);
        UTF8_FUNCS.put("codes", null);
        UTF8_FUNCS.put("charpattern", null);
    }

    /**
     * utf8.len (s [, i [, j]])
     * http://www.lua.org/manual/5.3/manual.html#pdf-utf8.len
     * lua-5.3.4/src/lutf8lib.c#utflen()
     *
     * @param ls
     * @return
     */
    private static int utf8Len(LuaState ls) {
        String s = ls.checkString(1);
        long sLen = s.length();
        long i = posRelat(ls.optInteger(2, 1), sLen);
        long j = posRelat(ls.optInteger(3, -1), sLen);
        ls.argCheck(1 <= i && i <= sLen + 1, 2, "initial position out of string");
        ls.argCheck(j <= sLen, 3, "final position out of string");
        if (i > j) {
            ls.pushInteger(0);
        } else {
            ls.pushInteger(j - i + 2);
        }
        return 1;
    }

    /**
     * translate a relative string position: negative means back from end
     *
     * @param pos
     * @param len
     * @return
     */
    private static long posRelat(long pos, long len) {
        if (pos >= 0) {
            return pos;
        } else if (-pos > len) {
            return 0;
        } else {
            return len + pos + 1;
        }
    }
}
