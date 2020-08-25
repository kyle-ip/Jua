package com.ywh.jua.compiler.ast.exps;


import com.ywh.jua.compiler.ast.Exp;

import java.util.List;

/**
 * 表构造表达式
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class TableConstructorExp extends Exp {

    /**
     * 键列表
     */
    private List<Exp> keyExps;

    /**
     * 值列表
     */
    private List<Exp> valExps;

    public List<Exp> getKeyExps() {
        return keyExps;
    }

    public void setKeyExps(List<Exp> keyExps) {
        this.keyExps = keyExps;
    }

    public List<Exp> getValExps() {
        return valExps;
    }

    public void setValExps(List<Exp> valExps) {
        this.valExps = valExps;
    }

    public void addKey(Exp key) {
        keyExps.add(key);
    }

    public void addVal(Exp val) {
        valExps.add(val);
    }

}
