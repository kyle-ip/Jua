package com.ywh.jua.stdlib;

import com.ywh.jua.api.JavaFunction;
import com.ywh.jua.api.LuaState;
import com.ywh.jua.api.LuaType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.ywh.jua.api.LuaType.LUA_TNIL;
import static com.ywh.jua.api.LuaType.LUA_TTABLE;

/**
 * @author ywh
 * @since 2020/8/26/026
 */
public class OSLib {

    private static final Map<String, JavaFunction> OS_FUNCS = new HashMap<>();

    static {
        OS_FUNCS.put("clock", OSLib::osClock);
        OS_FUNCS.put("difftime", OSLib::osDifftime);
        OS_FUNCS.put("time", OSLib::osTime);
    }

    /**
     *
     * @param ls
     * @return
     */
    public static int openOSLib(LuaState ls) {
        ls.newLib(OS_FUNCS);
        return 1;
    }

    /**
     *
     *
     * @param ls
     * @return
     */
    private static int osClock(LuaState ls) {
        ls.pushNumber(System.currentTimeMillis() / 1000.0);
        return 1;
    }

    /**
     * os.difftime (t2, t1)
     * http://www.lua.org/manual/5.3/manual.html#pdf-os.difftime
     * lua-5.3.4/src/loslib.c#os_difftime()
     *
     * @param ls
     * @return
     */
    private static int osDifftime(LuaState ls) {
        long t2 = ls.checkInteger(1);
        long t1 = ls.checkInteger(2);
        ls.pushInteger(t2 - t1);
        return 1;
    }

    /**
     * os.time ([table])
     * http://www.lua.org/manual/5.3/manual.html#pdf-os.time
     * lua-5.3.4/src/loslib.c#os_time()
     *
     * @param ls
     * @return
     * @throws ParseException
     */
    private static int osTime(LuaState ls) {
        if (ls.isNoneOrNil(1)) {
            ls.pushInteger(System.currentTimeMillis());
        } else {
            ls.checkType(1, LUA_TTABLE);
            int sec = getField(ls, "sec", 0);
            int min = getField(ls, "min", 0);
            int hour = getField(ls, "hour", 12);
            int day = getField(ls, "day", -1);
            int month = getField(ls, "month", -1);
            int year = getField(ls, "year", -1);
            SimpleDateFormat format =  new SimpleDateFormat("yyyyMMddHHmmss");
            String time = String.format("%d%d%d%d%d%d", year, month, day, hour, min, sec);
            try {
                Date date = format.parse(time);
                ls.pushInteger(date.getTime());
            } catch (Exception ex) {
                return ls.error2(ex.getMessage());
            }
        }
        return 1;
    }

    private static int getField(LuaState ls, String key, long dft) {
        LuaType t = ls.getField(-1, key);
        Long ret = ls.toIntegerX(-1);
        if (ret != null) {
            if (t != LUA_TNIL) {
                return ls.error2("field '%s' is not an integer", key);
            } else if (dft < 0) {
                return ls.error2("field '%s' missing in date table", key);
            }
            ret = dft;
        }
        ls.pop(1);
        assert ret != null;
        return ret.intValue();
    }

    private static void setField(LuaState ls, String key, int value) {
        ls.pushInteger((long) value);
        ls.setField(-2, key);
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis() / 1000.0);
    }
}
