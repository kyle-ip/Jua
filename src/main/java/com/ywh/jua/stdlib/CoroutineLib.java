package com.ywh.jua.stdlib;

import com.ywh.jua.api.JavaFunction;
import com.ywh.jua.api.LuaState;
import com.ywh.jua.api.ThreadStatus;
import com.ywh.jua.state.LuaStateImpl;

import java.util.HashMap;
import java.util.Map;

import static com.ywh.jua.api.LuaType.LUA_TFUNCTION;
import static com.ywh.jua.api.ThreadStatus.LUA_OK;
import static com.ywh.jua.api.ThreadStatus.LUA_YIELD;

/**
 * 协程库
 * Lua 通过协程库对协作（非抢占）式多任务执行提供支持。
 * 协程与线程相似，用作自己得调用栈、程序计数器、局部变量等，是独立的执行单位。
 *
 * Java 等语言的线程由调度器（OS）根据时间片和优先级进行调度；
 * Lua 等语言的协程（在 Lua 中类型为 thread，非抢占式线程）则需要相互协作来完成工作。
 *
 * 协程有四个状态：运行（running）、挂起（suspended）、正常（normal）、死亡（dead）；
 * 脚本是在一个协程内执行的，这个协程称为主线程，任何时刻只能有一个协程处于运行状态。
 * 通过 running() 函数可以获取运行的协程，通过 status() 函数可以获取任意协程状态。
 *
 * 协程的状态迁移如图所示：
 *                                    resume
 *                     resume         other
 *               +--------------+   +--------> normal
 *     new       |              ↓   |
 * O ------> suspended         running
 *               ↑              |   |
 *               +--------------+   +--------> dead --------> O
 *                     yield           return
 *
 * 使用 resume() 和 yield() 函数进行协程状态迁移：
 *      co = coroutine.create(function()
 *           print(coroutine.status(co))                     --> running
 *           coroutine.resume(coroutine.create(function()
 *               print(coroutine.status(co))
 *           end))
 *      end)
 *      print(coroutine.status(co))                          --> suspended
 *      coroutine.resume(co)
 *      print(coroutine.status(co))                          --> dead
 *
 * 如果协程首次开始运行，参数会传递给创建该协程时提供的函数，否则参数会作为 yield() 调用的返回值返回。
 *      co = coroutine.create(function(a, b, c))
 *           print(a, b, c)
 *           while true do
 *               print(coroutine.yield())
 *           end
 *      end)
 *      coroutine.resume(co, 1, 2, 3)                         --> 1 2 3
 *      coroutine.resume(co, 4, 5, 6)                         --> 4 5 6
 *      coroutine.resume(co, 7, 8, 9)                         --> 7 8 9
 *
 * resume() 调用也有返回值，如果被恢复的协程调用了 yield()，则 resume() 返回 true 和 yield() 接收到的参数（成功），
 * 或返回 false 和一个错误信息（失败）。
 *      co = coroutine.create(function()
 *           for k, v in pairs({"a", "b", "c"}) do
 *               coroutine.yield(k, v)
 *           end
 *           return "d", 4
 *      end)
 *      print(coroutine.resume(co))                            --> true 1 a
 *      print(coroutine.resume(co))                            --> true 2 b
 *      print(coroutine.resume(co))                            --> true 3 c
 *      print(coroutine.resume(co))                            --> true d 4
 *      print(coroutine.resume(co))                            --> false "cannot resume dead coroutine"
 * @author ywh
 * @since 2020/8/27/027
 */
public class CoroutineLib {

    private static final Map<String, JavaFunction> COROUTINE_FUNCS = new HashMap<>();

    static {
        COROUTINE_FUNCS.put("create", CoroutineLib::coCreate);
        COROUTINE_FUNCS.put("resume", CoroutineLib::coResume);
        COROUTINE_FUNCS.put("yield", CoroutineLib::coYield);
        COROUTINE_FUNCS.put("status", CoroutineLib::coStatus);
        COROUTINE_FUNCS.put("isyieldable", CoroutineLib::coYieldable);
        COROUTINE_FUNCS.put("running", CoroutineLib::coRunning);
        COROUTINE_FUNCS.put("wrap", CoroutineLib::coWrap);
    }

    /**
     * 启用协程库
     *
     * @param ls
     * @return
     */
    public static int openBaseLib(LuaState ls) {
        ls.newLib(COROUTINE_FUNCS);
        return 1;
    }

    /**
     * coroutine.create (f)
     * http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.create
     * lua-5.3.4/src/lcorolib.c#luaB_cocreate()
     *
     * @param ls
     * @return
     */
    private static int coCreate(LuaStateImpl ls) {
        ls.checkType(1, LUA_TFUNCTION);
        LuaStateImpl ls2 = ls.newThread();
        ls.pushValue(1);
        ls.xMove(ls2, 1);
        return 1;
    }

    /**
     * coroutine.resume (co [, val1, ···])
     * http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.resume
     * lua-5.3.4/src/lcorolib.c#luaB_coresume()
     *
     * @param ls
     * @return
     */
    private static int coResume(LuaStateImpl ls) {
        LuaStateImpl co = ls.toThread(1);
        ls.argCheck(co != null, 1, "thread expected");
        int r = auxResume(ls, co, ls.getTop() - 1);
        if (r < 0) {
            ls.pushBoolean(false);
            ls.insert(-2);
            return 2;
        } else {
            ls.pushBoolean(true);
            ls.insert(-(r + 1));
            return r + 1;
        }
    }

    /**
     * coroutine.yield (···)
     * http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.yield
     * lua-5.3.4/src/lcorolib.c#luaB_yield()
     *
     * @param ls
     * @return
     */
    private static int coYield(LuaStateImpl ls) {
        return ls.yield(ls.getTop());
    }

    /**
     * coroutine.status (co)
     * http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.status
     * lua-5.3.4/src/lcorolib.c#luaB_costatus()
     *
     * @param ls
     * @return
     */
    private static int coStatus(LuaStateImpl ls) {
        LuaStateImpl co = ls.toThread(1);
        ls.argCheck(co != null, 1, "thread expected");
        if (ls == co) {
            ls.pushString("running");
        } else {
            assert(co != null);
            switch (co.status()) {
                case LUA_YIELD:
                    ls.pushString("suspended");
                    break;
                case LUA_OK:
                    if (co.getStack()) {
                        ls.pushString("normal");
                    } else if (co.getTop() == 0) {
                        ls.pushString("dead");
                    } else {
                        ls.pushString("suspended");
                    }
                    break;
                default:
                    ls.pushString("dead");
                    break;
            }
        }
        return 1;
    }

    /**
     * coroutine.isyieldable ()
     * http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.isyieldable
     *
     * @param ls
     * @return
     */
    private static int coYieldable(LuaStateImpl ls) {
        ls.pushBoolean(ls.isYieldAble());
        return 1;
    }

    /**
     * coroutine.running ()
     * http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.running
     *
     * @param ls
     * @return
     */
    private static int coRunning(LuaStateImpl ls) {
        boolean isMain = ls.pushThread();
        ls.pushBoolean(isMain);
        return 2;
    }

    /**
     * coroutine.wrap (f)
     * http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.wrap
     *
     * @param ls
     * @return
     */
    private static int coWrap(LuaStateImpl ls) {
        // TODO
        return 0;
    }

    /**
     *
     * @param ls
     * @param co
     * @param narg
     * @return
     */
    private static int auxResume(LuaStateImpl ls, LuaStateImpl co, int narg) {
        if (ls.checkStack(narg)) {
            ls.pushString("too many arguments to resume");
            /* error flag */
            return -1;
        }
        if (co.status() == LUA_OK && co.getTop() == 0) {
            ls.pushString("cannot resume dead coroutine");
            /* error flag */
            return -1;
        }
        ls.xMove(co, narg);
        ThreadStatus status = co.resume(ls, narg);
        if (status == LUA_OK || status == LUA_YIELD) {
            int nres = co.getTop();
            if (!ls.checkStack(nres + 1)) {
                co.pop(nres);
                ls.pushString("too many results to resume");
                return -1;
            }
            co.xMove(ls, nres);
            return nres;
        } else {
            co.xMove(ls, 1);
            return -1;
        }
    }
}
