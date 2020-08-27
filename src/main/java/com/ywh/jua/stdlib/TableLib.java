package com.ywh.jua.stdlib;

import com.ywh.jua.api.JavaFunction;
import com.ywh.jua.api.LuaState;
import com.ywh.jua.api.LuaType;
import com.ywh.jua.state.LuaStateImpl;

import java.util.HashMap;
import java.util.Map;

import static com.ywh.jua.api.LuaType.LUA_TTABLE;

/**
 * 字符串库
 *
 * @author ywh
 * @since 27/08/2020
 */
public class TableLib {

    /**
     * read
     */
    private static final int TAB_R = 1;

    /**
     * write
     */
    private static final int TAB_W = 2;

    /**
     * length
     */
    private static final int TAB_L = 4;

    /**
     * read/write
     */
    private static final int TAB_RW = (TAB_R | TAB_W);

    private static final Map<String, JavaFunction> TABLE_FUNCS = new HashMap<>();

    static {
        TABLE_FUNCS.put("move", null);
        TABLE_FUNCS.put("insert", null);
        TABLE_FUNCS.put("remove", null);
        TABLE_FUNCS.put("sort", null);
        TABLE_FUNCS.put("concat", null);
        TABLE_FUNCS.put("pack", null);
        TABLE_FUNCS.put("unpack", null);
    }
}
