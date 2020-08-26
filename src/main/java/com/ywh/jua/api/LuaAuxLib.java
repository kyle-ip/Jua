package com.ywh.jua.api;

import java.util.Map;

/**
 * 辅助 API（增强版方法）
 *
 * @author ywh
 * @since 2020/8/17 11:26
 */
public interface LuaAuxLib {

    //type FuncReg map[string]GoFunction

    /* Error-report functions */

    /**
     * 输出指定错误类型
     *
     * @param fmt
     * @param a
     * @return
     */
    int error2(String fmt, Object... a);

    /**
     *
     * @param arg
     * @param extraMsg
     * @return
     */
    int argError(int arg, String extraMsg);

    /* Argument check functions */

    /**
     * 确保栈还有足够的剩余空间，必要时扩容，扩容失败则报错。
     *
     * @param sz
     * @param msg
     */
    void checkStack2(int sz, String msg);

    /**
     * 通用的参数检查
     *
     * @param cond
     * @param arg
     * @param extraMsg
     */
    void argCheck(boolean cond, int arg, String extraMsg);

    /**
     * 确保某个参数一定存在
     *
     * @param arg
     */
    void checkAny(int arg);

    /**
     *
     * @param arg
     * @param t
     */
    void checkType(int arg, LuaType t);

    /**
     * 确保参数为 integer
     *
     * @param arg
     * @return
     */
    long checkInteger(int arg);

    /**
     * 确保参数为 number
     *
     * @param arg
     * @return
     */
    double checkNumber(int arg);

    /**
     * 确保参数为字符串
     *
     * @param arg
     * @return
     */
    String checkString(int arg);

    /**
     *
     * @param arg
     * @param d
     * @return
     */
    long optInteger(int arg, long d);

    /**
     *
     * @param arg
     * @param d
     * @return
     */
    double optNumber(int arg, double d);

    /**
     *
     * @param arg
     * @param d
     * @return
     */
    String optString(int arg, String d);

    /* Load functions */

    /**
     *
     * @param filename
     * @return
     */
    boolean doFile(String filename);

    /**
     * 加载并使用保护模式执行字符串
     *
     * @param str
     * @return
     */
    boolean doString(String str);

    /**
     * 加载文件
     *
     * @param filename
     * @return
     */
    ThreadStatus loadFile(String filename);

    /**
     * 以默认模式加载文件
     *
     * @param filename
     * @param mode
     * @return
     */
    ThreadStatus loadFileX(String filename, String mode);

    /**
     * 加载字符串
     *
     * @param s
     * @return
     */
    ThreadStatus loadString(String s);

    /* Other functions */

    /**
     *
     * @param idx
     * @return
     */
    String typeName2(int idx);

    /**
     *
     * @param idx
     * @return
     */
    String toString2(int idx);

    /**
     * 求长度
     * 如果获取到的长度不是整数（调用 __len 元方法），则抛出错误。
     *
     * @param idx
     * @return
     */
    long len2(int idx);

    /**
     * 检查指定索引处的表某个字段是否表，是则把子表推入栈顶并返回 true，否则创建一个空表赋值给字段并返回 false。
     *
     * @param idx
     * @param fname
     * @return
     */
    boolean getSubTable(int idx, String fname);

    /**
     *
     * @param obj
     * @param e
     * @return
     */
    LuaType getMetafield(int obj, String e);

    /**
     *
     * @param obj
     * @param e
     * @return
     */
    boolean callMeta(int obj, String e);

    /**
     * 启用标准库
     */
    void openLibs();

    /**
     * 开启标准库
     *
     * @param modname
     * @param openf
     * @param glb
     */
    void requireF(String modname, JavaFunction openf, boolean glb);

    /**
     *
     * @param l
     */
    void newLib(Map<String, JavaFunction> l);

    /**
     *
     * @param l
     */
    void newLibTable(Map<String, JavaFunction> l);

    /**
     * 注册函数到全局变量表
     *
     * @param l
     * @param nup
     */
    void setFuncs(Map<String, JavaFunction> l, int nup);

}
