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

    private List<Object> arr;

    private Map<Object, Object> map;

    LuaTable(int nArr, int nRec) {
        if (nArr > 0) {
            arr = new ArrayList<>(nArr);
        }
        if (nRec > 0) {
            map = new HashMap<>(nRec);
        }
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

    private Object floatToInteger(Object key) {
        if (key instanceof Double) {
            Double f = (Double) key;
            if (LuaNumber.isInteger(f)) {
                return f.longValue();
            }
        }
        return key;
    }

    private void shrinkArray() {
        for (int i = arr.size() - 1; i >= 0; i--) {
            if (arr.get(i) == null) {
                arr.remove(i);
            }
        }
    }

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

}
