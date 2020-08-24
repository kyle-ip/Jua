package com.ywh.jua.state;


import com.ywh.jua.number.LuaNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Lua 表
 * 采用混合数据结构，同时包含数组和哈希表两部分。
 * 如果表的键是连续的正整数，则哈希表为空，值全部存储在数组中；
 * 如果表没有被当作数组使用，则数据完全存储在哈希表中。
 *
 * @author ywh
 * @since 2020/8/19 11:26
 */
class LuaTable {

    /**
     * 元表，存放类型关联函数。
     */
    LuaTable metatable;

    /**
     * 数组
     */
    private List<Object> arr;

    /**
     * 哈希表
     */
    private Map<Object, Object> map;

    // ========== 迭代器 next 函数 ==========

    /**
     * 键表，存放表的键与下一个键的关系。
     */
    private Map<Object, Object> keys;

    private Object lastKey;

    private boolean changed;

    LuaTable(int nArr, int nRec) {
        if (nArr > 0) {
            arr = new ArrayList<>(nArr);
        }
        if (nRec > 0) {
            map = new HashMap<>(nRec);
        }
    }

    /**
     * 是否具备元字段（方法）
     *
     * @param fieldName
     * @return
     */
    boolean hasMetafield(String fieldName) {
        return metatable != null && metatable.get(fieldName) != null;
    }

    /**
     * 数组长度
     *
     * @return
     */
    int length() {
        return arr == null ? 0 : arr.size();
    }

    /**
     * 取值
     *
     * @param key
     * @return
     */
    Object get(Object key) {

        // 如果数组非空，且 key 可以转换为 Long 类型，则按数组的方式访问；否则按哈希表的方式访问。
        key = floatToInteger(key);
        if (arr != null && key instanceof Long) {
            int idx = ((Long) key).intValue();
            if (idx >= 1 && idx <= arr.size()) {
                return arr.get(idx - 1);
            }
        }
        return map != null ? map.get(key) : null;
    }

    /**
     * 设值
     *
     * @param key
     * @param val
     */
    void put(Object key, Object val) {
        // 键不允许为空，且不允许为 Double 的 NaN
        if (key == null) {
            throw new RuntimeException("table index is nil!");
        }
        if (key instanceof Double && ((Double) key).isNaN()) {
            throw new RuntimeException("table index is NaN!");
        }
        // 如果键可以转换为 Long 类型，且下标 >= 1，则按数组处理
        key = floatToInteger(key);
        if (key instanceof Long) {
            int idx = ((Long) key).intValue();
            if (idx >= 1) {
                if (arr == null) {
                    arr = new ArrayList<>();
                }
                int arrLen = arr.size();

                // 如果访问的下标在数组长度范围内，则设值
                if (idx <= arrLen) {
                    arr.set(idx - 1, val);
                    // 如果向数组放入 nil 值，则会造成“洞”；如果“洞”在数组末尾，则把末尾的“洞”全部删除
                    if (idx == arrLen && val == null) {
                        shrinkArray();
                    }
                    return;
                }

                // 如果访问的下标刚超出数组范围，则删除哈希表中的值（如果存在），并扩充数组
                if (idx == arrLen + 1) {
                    if (map != null) {
                        map.remove(key);
                    }
                    if (val != null) {
                        arr.add(val);
                        if (map != null) {
                            expandArray();
                        }

                    }
                    return;
                }
            }
        }
        // 如果键不能转换为 Long 类型，且值不为空，则加入到哈希表。
        if (val != null) {
            if (map == null) {
                map = new HashMap<>();
            }
            map.put(key, val);
        } else {
            if (map != null) {
                map.remove(key);
            }
        }
    }

    /**
     * Float 转换成 Integer
     *
     * @param key
     * @return
     */
    private Object floatToInteger(Object key) {
        if (key instanceof Double) {
            Double f = (Double) key;
            if (LuaNumber.isInteger(f)) {
                return f.longValue();
            }
        }
        return key;
    }

    /**
     * 数组缩容（清理“洞”）
     */
    private void shrinkArray() {
        for (int i = arr.size() - 1; i >= 0; i--) {
            if (arr.get(i) == null) {
                arr.remove(i);
            }
        }
    }

    /**
     * 数组扩容
     */
    private void expandArray() {
        for (int idx = arr.size() + 1; ; idx++) {
            Object val = map.remove((long) idx);
            if (val != null) {
                arr.add(val);
            } else {
                break;
            }
        }
    }

    /**
     * 取下一个键
     *
     * @param key
     * @return
     */
    Object nextKey(Object key) {

        // 如果传入 nil，表示遍历开始，需要先把所有的键收集到 keys 中。
        if (keys == null || (key == null && changed)) {
            initKeys();
            changed = false;
        }

        Object nextKey = keys.get(key);
        // 如果下一个键为空，且当前键为空，且当前键不是最后一个键，则访问错误。
        if (nextKey == null && key != null && key != lastKey) {
            throw new RuntimeException("invalid key to 'next'");
        }

        return nextKey;
    }

    /**
     * keys 初始化，把数组或哈希表所有的键收集到 keys 中。
     */
    private void initKeys() {
        if (keys == null) {
            keys = new HashMap<>();
        } else {
            keys.clear();
        }
        Object key = null;

        // 数组
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i) != null) {
                    long nextKey = i + 1;
                    keys.put(key, nextKey);
                    key = nextKey;
                }
            }
        }

        // 哈希表
        if (map != null) {
            for (Object k : map.keySet()) {
                Object v = map.get(k);
                if (v != null) {
                    keys.put(key, k);
                    key = k;
                }
            }
        }
        // 设置最后一个 key。
        lastKey = key;
    }
}
