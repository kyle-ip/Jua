package com.ywh.jua.stdlib;

import com.ywh.jua.api.JavaFunction;
import com.ywh.jua.api.LuaState;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.ywh.jua.api.LuaState.LUA_REGISTRYINDEX;
import static com.ywh.jua.api.LuaType.LUA_TNIL;
import static com.ywh.jua.api.LuaType.LUA_TTABLE;
import static com.ywh.jua.api.ThreadStatus.LUA_OK;
import static com.ywh.jua.vm.Instructions.luaUpvalueIndex;

/**
 * 模块库
 *
 * @author ywh
 * @since 26/08/2020
 */
public class PackageLib {

    private static final String LUA_LOADED_TABLE = "_LOADED";

    private static final String LUA_PRELOAD_TABLE = "_PRELOAD";

    private static final String LUA_DIR_SEP = File.pathSeparator;

    private static final String LUA_PATH_SEP = ";";

    private static final String LUA_PATH_MARK = "?";

    private static final String LUA_EXEC_DIR = "!";

    private static final String LUA_IG_MARK = "-";

    private static final Map<String, JavaFunction> PACKAGE_FUNCS = new HashMap<>();

    private static final Map<String, JavaFunction> LL_FUNCS = new HashMap<>();

    static {
        PACKAGE_FUNCS.put("searchpath", PackageLib::pkgSearchPath);
        PACKAGE_FUNCS.put("preload", null);
        PACKAGE_FUNCS.put("cpath", null);
        PACKAGE_FUNCS.put("path", null);
        PACKAGE_FUNCS.put("searchers", null);
        PACKAGE_FUNCS.put("loaded", null);

        LL_FUNCS.put("require", PackageLib::pkgRequire);
    }

    /**
     * 启用模块库
     * 把 package 包以及里面的函数和变量准备好，并把 require() 函数注册到全局表中。
     *
     * @param ls
     * @return
     */
    public static int openPackageLib(LuaState ls) {
        /* create 'package' table */
        ls.newLib(PACKAGE_FUNCS);
        createSearchersTable(ls);
        /* set paths */
        String baseAbsPath = String.format("%s%s?.lua", new File("").getAbsolutePath(), File.separator);
        ls.pushString(String.format("%s;./?.lua;./?/init.lua", baseAbsPath));
        ls.setField(-2, "path");
        /* store config information */
        ls.pushString(String.format("%s\n%s\n%s\n%s\n%s\n", LUA_DIR_SEP, LUA_PATH_SEP, LUA_PATH_MARK, LUA_EXEC_DIR, LUA_IG_MARK));
        ls.setField(-2, "config");
        /* set field 'loaded' */
        ls.getSubTable(LUA_REGISTRYINDEX, LUA_LOADED_TABLE);
        ls.setField(-2, "loaded");
        /* set field 'preload' */
        ls.getSubTable(LUA_REGISTRYINDEX, LUA_PRELOAD_TABLE);
        ls.setField(-2, "preload");
        ls.pushGlobalTable();
        /* set 'package' as upvalue for next lib */
        ls.pushValue(-2);
        /* open lib into global table */
        ls.setFuncs(LL_FUNCS, 1);
        /* pop global table */
        ls.pop(1);
        /* return 'package' table */
        return 1;
    }

    /**
     * package.searchpath (name, path [, sep [, rep]])
     * http://www.lua.org/manual/5.3/manual.html#pdf-package.searchpath
     * loadlib.c#ll_searchpath
     *
     * @param ls
     * @return
     */
    private static int pkgSearchPath(LuaState ls) {
        String name = ls.checkString(1);
        String path = ls.checkString(2);
        String sep = ls.optString(3, ".");
        String rep = ls.optString(4, LUA_DIR_SEP);
        String fileName = searchPath(name, path, sep, rep);
        if (fileName != null) {
            ls.pushString(fileName);
            return 1;
        } else {
            ls.pushNil();
            ls.pushString(String.format("\n\tno file '%s'", name));
            return 2;
        }
    }

    /**
     * 初始化 package.searchers 表
     * 其中存放 preload 和 lua 两个搜索器，并且把 package 表设置成这两个搜索器的 Upvalue。
     *
     * @param ls
     */
    private static void createSearchersTable(LuaState ls) {
        ls.createTable(2, 0);
        ls.pushValue(-2);
        JavaFunction[] searchers = new JavaFunction[]{PackageLib::preloadSearcher, PackageLib::luaSearcher};
        /* create 'searchers' table */
        ls.createTable(searchers.length, 0);
        for (int i = 0; i < searchers.length; i++) {
            /* set 'package' as upvalue for all searchers */
            ls.pushValue(-2);
            ls.pushJavaClosure(searchers[i], 1);
            ls.rawSetI(-2, i + 1);
        }
        /* put it in field 'searchers' */
        ls.setField(-2, "searchers");
    }

    /**
     * require (modname)
     * http://www.lua.org/manual/5.3/manual.html#pdf-require
     *
     * @param ls
     * @return
     */
    private static int pkgRequire(LuaState ls) {
        String name = ls.checkString(1);
        /* LOADED table will be at index 2 */
        ls.setTop(1);
        ls.getField(LUA_REGISTRYINDEX, LUA_LOADED_TABLE);
        /* LOADED[name] */
        ls.getField(2, name);
        /* is it there? */
        if (ls.toBoolean(-1)) {
            /* package is already loaded */
            return 1;
        }
        /* else must load package */
        /* remove 'getfield' result */
        ls.pop(1);
        findLoader(ls, name);
        /* pass name as argument to module loader */
        ls.pushString(name);
        /* name is 1st argument (before search data) */
        ls.insert(-2);
        /* run loader to load module */
        ls.call(2, 1);
        /* non-nil return? */
        if (!ls.isNil(-1)) {
            /* LOADED[name] = returned value */
            ls.setField(2, name);
        }
        /* module set no value? */
        if (ls.getField(2, name) == LUA_TNIL) {
            /* use true as result */
            ls.pushBoolean(true);
            /* extra copy to be returned */
            ls.pushValue(-1);
            /* LOADED[name] = true */
            ls.setField(2, name);
        }
        return 1;
    }

    /**
     * preload 搜索器
     * 搜索器通过 Upvalue 能够访问到 package.preload 表，然后在里面查找加载器。
     *
     * @param ls
     * @return
     */
    private static int preloadSearcher(LuaState ls) {
        String name = ls.checkString(1);
        ls.getField(LUA_REGISTRYINDEX, "_PRELOAD");
        /* not found? */
        if (ls.getField(-1, name) == LUA_TNIL) {
            ls.pushString("\n\tno field package.preload['" + name + "']");
        }
        return 1;
    }

    /**
     * lua 搜索器
     *
     * @param ls
     * @return
     */
    private static int luaSearcher(LuaState ls) {
        String name = ls.checkString(1);
        ls.getField(luaUpvalueIndex(1), "path");
        String path = ls.toStringX(-1);
        if (path == null) {
            ls.error2("'package.path' must be a string");
        }
        String fileName = searchPath(name, path, ".", LUA_DIR_SEP);
        if (fileName == null) {
            ls.pushString(String.format("\n\tno file '%s'", name));
            return 1;
        }
        /* module loaded successfully? */
        if (ls.loadFile(fileName) == LUA_OK) {
            /* will be 2nd argument to module */
            ls.pushString(fileName);
            /* return open function and file name */
            return 2;
        } else {
            return ls.error2("error loading module '%s' from file '%s':\n\t%s", ls.checkString(1), fileName,
                ls.checkString(-1));
        }
    }

    /**
     * @param name
     * @param path
     * @param sep
     * @param dirSep
     * @return
     */
    private static String searchPath(String name, String path, String sep, String dirSep) {
        // . => /
        if (!"".equals(sep)) {
            name = name.replace(sep, dirSep);
        }
        for (String fileName : path.split(LUA_PATH_SEP)) {
            // ? => name
            fileName = fileName.replace(LUA_PATH_MARK, name);
            if (new File(fileName).exists()) {
                return fileName;
            }
        }
        return null;
    }

    /**
     * 搜索加载器
     *
     * @param ls
     * @param name
     */
    private static void findLoader(LuaState ls, String name) {
        /* push 'package.searchers' to index 3 in the stack */
        if (ls.getField(luaUpvalueIndex(1), "searchers") != LUA_TTABLE) {
            ls.error2("'package.searchers' must be a table");
        }
        /* to build error message */
        String errMsg = String.format("module '%s' not found:", name);
        /*  iterate over available searchers to find a loader */
        for (int i = 1; ; i++) {
            /* no more searchers? */
            if (ls.rawGetI(3, i) == LUA_TNIL) {
                /* remove nil */
                ls.pop(1);
                /* create error message */
                ls.error2(errMsg);
            }
            ls.pushString(name);
            /* call it */
            ls.call(1, 2);
            /* did it find a loader? */
            if (ls.isFunction(-2)) {
                /* module loader found */
                return;
            }
            /* searcher returned error message? */
            else if (ls.isString(-2)) {
                /* remove extra return */
                ls.pop(1);
                /* concatenate error message */
                errMsg += ls.checkString(-1);
            } else {
                /* remove both returns */
                ls.pop(2);
            }
        }
    }
}
