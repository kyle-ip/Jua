package com.ywh.jua.stdlib;

import com.ywh.jua.api.JavaFunction;
import com.ywh.jua.api.LuaState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ywh
 * @since 2020/8/26/026
 */
public class StringLib {

    private static final Pattern TAG_PATTERN = Pattern.compile("%[ #+-0]?[0-9]*(\\.[0-9]+)?[cdeEfgGioqsuxX%]");

    private static final Map<String, JavaFunction> STRING_FUNCS = new HashMap<>();

    static {
        STRING_FUNCS.put("len", StringLib::strLen);
        STRING_FUNCS.put("rep", StringLib::strRep);
        STRING_FUNCS.put("reverse", StringLib::strReverse);
        STRING_FUNCS.put("lower", StringLib::strLower);
        STRING_FUNCS.put("upper", StringLib::strUpper);
        STRING_FUNCS.put("sub", StringLib::strSub);
        STRING_FUNCS.put("byte", StringLib::strByte);
        STRING_FUNCS.put("char", StringLib::strChar);
        STRING_FUNCS.put("dump", StringLib::strDump);
        STRING_FUNCS.put("format", StringLib::strFormat);
        STRING_FUNCS.put("packsize", StringLib::strPackSize);
        STRING_FUNCS.put("pack", StringLib::strPack);
        STRING_FUNCS.put("unpack", StringLib::strUnpack);
        STRING_FUNCS.put("find", StringLib::strFind);
        STRING_FUNCS.put("match", StringLib::strMatch);
        STRING_FUNCS.put("gsub", StringLib::strGsub);
        STRING_FUNCS.put("gmatch", StringLib::strGmatch);
    }

    /**
     *
     * @param ls
     * @return
     */
    public static int openStringLib(LuaState ls) {
        ls.newLib(STRING_FUNCS);
        createMetatable(ls);
        return 1;
    }

    /**
     *
     * @param ls
     */
    private static void createMetatable(LuaState ls) {
        /* table to be metatable for strings */
        ls.createTable(0, 1);
        /* dummy string */
        ls.pushString("dummy");
        /* copy table */
        ls.pushValue(-2);
        /* set table as metatable for strings */
        ls.setMetatable(-2);
        /* pop dummy string */
        ls.pop(1);
        /* get string library */
        ls.pushValue(-2);
        /* metatable.__index = string */
        ls.setField(-2, "__index");
        /* pop metatable */
        ls.pop(1);
    }

    /**
     * string.len (s)
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.len
     * lua-5.3.4/src/lstrlib.c#str_len()
     *
     * @param ls
     * @return
     */
    private static int strLen(LuaState ls) {
        String s = ls.checkString(1);
        ls.pushInteger(s.length());
        return 1;
    }

    /**
     * string.rep (s, n [, sep])
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.rep
     * lua-5.3.4/src/lstrlib.c#str_rep()
     *
     * @param ls
     * @return
     */
    private static int strRep(LuaState ls) {
        String s = ls.checkString(1);
        long n = ls.checkInteger(2);
        String sep = ls.optString(3, "");
        if (n <= 0) {
            ls.pushString("");
        } else if (n == 1) {
            ls.pushString(s);
        } else {
            String[] arr = new String[(int) n];
            for (int i = 0; i < (int) n; i++) {
                arr[i] = s;
            }
            ls.pushString(String.join(sep, arr));
        }
        return 1;
    }

    /**
     * string.reverse (s)
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.reverse
     * lua-5.3.4/src/lstrlib.c#str_reverse()
     *
     * @param ls
     * @return
     */
    private static int strReverse(LuaState ls) {
        String s = ls.checkString(1);
        if (s.length() > 1) {
            ls.pushString(new StringBuffer(s).reverse().toString());
        }
        return 1;
    }

    /**
     * string.lower (s)
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.lower
     * lua-5.3.4/src/lstrlib.c#str_lower()
     *
     * @param ls
     * @return
     */
    private static int strLower(LuaState ls) {
        String s = ls.checkString(1);
        ls.pushString(s.toLowerCase());
        return 1;
    }

    /**
     * string.upper (s)
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.upper
     * lua-5.3.4/src/lstrlib.c#str_upper()
     *
     * @param ls
     * @return
     */
    private static int strUpper(LuaState ls) {
        String s = ls.checkString(1);
        ls.pushString(s.toUpperCase());
        return 1;
    }

    /**
     * string.sub (s, i [, j])
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.sub
     * lua-5.3.4/src/lstrlib.c#str_sub()
     *
     * @param ls
     * @return
     */
    private static int strSub(LuaState ls) {
        String s = ls.checkString(1);
        long i = posRelat(ls.checkInteger(2), s.length());
        long j = posRelat(ls.optInteger(3, -1), s.length());
        if (i < 1) {
            i = 1;
        }
        if (j > s.length()) {
            j = s.length();
        }
        if (i <= j) {
            ls.pushString(s.substring((int) i, (int) j));
        } else {
            ls.pushString("");
        }
        return 1;
    }

    /**
     * string.byte (s [, i [, j]])
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.byte
     * lua-5.3.4/src/lstrlib.c#str_byte()
     *
     * @param ls
     * @return
     */
    private static int strByte(LuaState ls) {
        String s = ls.checkString(1);
        long i = posRelat(ls.optInteger(2, 1), s.length());
        long j = posRelat(ls.optInteger(3, i), s.length());
        if (i < 1) {
            i = 1;
        }
        if (j > s.length()) {
            j = s.length();
        }
        if (i > j) {
            return 0;
        }
        long n = j - i + 1;
        ls.checkStack2((int) n, "string slice too long");
        for (int k = 0; k < n; k++) {
            ls.pushInteger(s.charAt((int) (i + k - 1)) - '0');
        }
        return (int) n;
    }

    /**
     * string.char (···)
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.char
     * lua-5.3.4/src/lstrlib.c#str_char()
     *
     * @param ls
     * @return
     */
    private static int strChar(LuaState ls) {
        int nArgs = ls.getTop();
        String[] s = new String[nArgs];
        for (int i = 1; i <= nArgs; i++) {
            long c = ls.checkInteger(i);
            ls.argCheck(c <= (long) Integer.MAX_VALUE, i, "value out of range");
            s[i - 1] = String.valueOf(c);
        }
        ls.pushString(String.join("", s));
        return 1;
    }

    /**
     * string.dump (function [, strip])
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.dump
     * lua-5.3.4/src/lstrlib.c#str_dump()
     *
     * @param ls
     * @return
     */
    private static int strDump(LuaState ls) {
        // TODO
        return 0;
    }

    /**
     * string.packsize (fmt)
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.packsize
     *
     * @param ls
     * @return
     */
    private static int strPackSize(LuaState ls) {
        String fmt = ls.checkString(1);
        if ("j".equals(fmt)) {
            ls.pushInteger(8);
        } else {
            ls.error2("todo: strPackSize!");
        }
        return 1;
    }

    /**
     * string.pack (fmt, v1, v2, ···)
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.pack
     *
     * @param ls
     * @return
     */
    private static int strPack(LuaState ls) {
        // TODO
        return 1;
    }

    /**
     * string.unpack (fmt, s [, pos])
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.unpack
     *
     * @param ls
     * @return
     */
    private static int strUnpack(LuaState ls) {
        // TODO
        return 1;
    }

    /**
     * string.format (formatstring, ···)
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.format
     *
     * @param ls
     * @return
     */
    private static int strFormat(LuaState ls) {
        String fmtStr = ls.checkString(1);
        if (fmtStr.length() <= 1 || !fmtStr.contains("%")) {
            ls.pushString(fmtStr);
            return 1;
        }
        int argIdx = 1;
        String[] arr = parseFmtStr(fmtStr);
        for (int i = 0; i < arr.length; i++) {
            String s = arr[i];
            if (s.charAt(0) == '%') {
                if ("%%".equals(s)) {
                    arr[i] = "%";
                } else {
                    argIdx += 1;
                    arr[i] = fmtArg(s, ls, argIdx);
                }
            }
        }
        ls.pushString(String.join("", arr));
        return 1;
    }

    /**
     * string.find (s, pattern [, init [, plain]])
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.find
     *
     * @param ls
     * @return
     */
    private static int strFind(LuaState ls) {
        String s = ls.checkString(1);
        String pattern = ls.checkString(2);
        long init = posRelat(ls.optInteger(3, 1), s.length());
        if (init < 1) {
            init = 1;
        } else if (init > s.length() + 1) {
            ls.pushNil();
            return 1;
        }
        boolean plain = ls.toBoolean(4);
        int start, end;
        String tail = s;
        if (init > 1) {
            tail = s.substring((int) init - 1);
        }

        if (plain) {
            start = tail.indexOf(pattern);
            end = start + pattern.length() - 1;
        } else {
            Pattern re = Pattern.compile(pattern);
            Matcher matcher = re.matcher(tail);
            if (!matcher.find()) {
                start = end = -1;
            } else {
                start = matcher.start();
                // TODO ?
                end = start + tail.length() - 1 - 1;
            }
        }
        if (start >= 0) {
            start += s.length() - tail.length() + 1;
            end += s.length() - tail.length() + 1;
        }
        if (start < 0) {
            ls.pushNil();
            return 1;
        }
        ls.pushInteger(start);
        ls.pushInteger(end);
        return 1;
    }

    /**
     * string.match (s, pattern [, init])
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.match
     *
     * @param ls
     * @return
     */
    private static int strMatch(LuaState ls) {
        String s = ls.checkString(1);
        String pattern = ls.checkString(2);
        long init = posRelat(ls.optInteger(3, 1), s.length());
        if (init < 1) {
            init = 1;
        } else if (init > s.length() + 1) {
            ls.pushNil();
            return 1;
        }
        int[] captures = match(s, pattern, (int) init);
        if (captures == null) {
            ls.pushNil();
            return 1;
        } else {
            for (int i = 0; i < captures.length; i += 2) {
                String capture = s.substring(captures[i], captures[i + 1]);
                ls.pushString(capture);
            }
            return captures.length / 2;
        }
    }

    /**
     * string.gsub (s, pattern, repl [, n])
     * http://www.lua.org/manual/5.3/manual.html#pdf-string.gsub
     *
     * @param ls
     * @return
     */
    private static int strGsub(LuaState ls) {
        String s = ls.checkString(1);
        String pattern = ls.checkString(2);
        String repl = ls.checkString(3);
        long n = ls.optInteger(4, -1);
        // TODO
        return 2;
    }

    /**
     *
     * @param ls
     * @return
     */
    private static int strGmatch(LuaState ls) {
        String s = ls.checkString(1);
        String pattern = ls.checkString(2);

        // TODO
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

    /**
     *
     * @param s
     * @param pattern
     * @param init
     * @return
     */
    private static int[] match(String s, String pattern, int init) {

        // TODO
        return null;
    }

    /**
     * @param tag
     * @param ls
     * @param argIdx
     * @return
     */
    private static String fmtArg(String tag, LuaState ls, int argIdx) {
        switch (tag.charAt(tag.length() - 1)) { // specifier
            case 'c': // character
                return String.valueOf(ls.toInteger(argIdx));
            case 'i':
                // %i -> %d
                tag = tag.substring(0, tag.length() - 1) + "d";
                return String.format(tag, ls.toInteger(argIdx));
            case 'd':
            case 'o':
                // integer, octal
                return String.format(tag, ls.toInteger(argIdx));
            case 'u':
                // unsigned integer
                // %u -> %d
                tag = tag.substring(0, tag.length() - 1) + "d";
                return String.format(tag, ls.toInteger(argIdx));
            case 'x':
            case 'X':
                // hex integer
                return String.format(tag, ls.toInteger(argIdx));
            case 'f':
                // float
                return String.format(tag, ls.toNumber(argIdx));
            case 's':
            case 'q':
                // string
                return String.format(tag, ls.toString2(argIdx));
            default:
                return null;
        }
    }

    private static String[] parseFmtStr(String fmt) {
        if ("".equals(fmt) || !fmt.contains("%")) {
            return new String[]{fmt};
        }
//        String[] parsed = new String[fmt.length() / 2];
        List<String> parsed = new ArrayList<>();
        while (true) {
            if ("".equals(fmt)) {
                break;
            }
            Matcher matcher = TAG_PATTERN.matcher(fmt);
            if (!matcher.find()) {
                parsed.add(fmt);
                break;
            }
            int start = matcher.start();
            int end = start + fmt.length() - 1;
            String head = fmt.substring(0, start);
            String tag = fmt.substring(start, end);
            String tail = fmt.substring(end);
            if (!"".equals(head)) {
                parsed.add(head);
            }
            parsed.add(tag);
            fmt = tail;
        }
        String[] ret = new String[fmt.length() / 2];
        parsed.toArray(ret);
        return ret;
    }
}