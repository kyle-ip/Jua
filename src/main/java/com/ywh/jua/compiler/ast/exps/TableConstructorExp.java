package com.ywh.jua.compiler.ast.exps;


import com.ywh.jua.compiler.ast.BaseExp;

import java.util.ArrayList;
import java.util.List;

/**
 * 表构造表达式
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class TableConstructorExp extends BaseExp {

    /**
     * 键列表
     */
    private List<BaseExp> keyExps = new ArrayList<>();

    /**
     * 值列表
     */
    private List<BaseExp> valExps = new ArrayList<>();

    public List<BaseExp> getKeyExps() {
        return keyExps;
    }

    public void setKeyExps(List<BaseExp> keyExps) {
        this.keyExps = keyExps;
    }

    public List<BaseExp> getValExps() {
        return valExps;
    }

    public void setValExps(List<BaseExp> valExps) {
        this.valExps = valExps;
    }

    public void addKey(BaseExp key) {
        keyExps.add(key);
    }

    public void addVal(BaseExp val) {
        valExps.add(val);
    }

}
