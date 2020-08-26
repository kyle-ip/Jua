package com.ywh.jua.compiler.codegen;

import com.ywh.jua.chunk.LocVar;
import com.ywh.jua.chunk.Prototype;
import com.ywh.jua.chunk.Upvalue;

import java.util.List;

/**
 * 函数原型转换器
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
class Fi2Proto {

    /**
     * 转换函数内部结构为函数原型
     *
     * @param fi
     * @return
     */
    static Prototype toProto(FuncInfo fi) {
        Prototype proto = new Prototype();
        proto.setLineDefined(fi.line);
        proto.setLastLineDefined(fi.lastLine);
        proto.setNumParams((byte) fi.numParams);
        proto.setMaxStackSize((byte) fi.maxRegs);
        proto.setCode(fi.insts.stream().mapToInt(Integer::intValue).toArray());
        proto.setConstants(getConstants(fi));
        proto.setUpvalues(getUpvalues(fi));
        proto.setProtos(toProtos(fi.subFuncs));
        proto.setLineInfo(fi.lineNums.stream().mapToInt(Integer::intValue).toArray());
        proto.setLocVars(getLocVars(fi));
        proto.setUpvalueNames(getUpvalueNames(fi));

        if (fi.line == 0) {
            proto.setLastLineDefined(0);
        }
        if (proto.getMaxStackSize() < 2) {
            // TODO
            proto.setMaxStackSize((byte) 2);
        }
        if (fi.isVararg) {
            // TODO
            proto.setIsVararg((byte) 1);
        }

        return proto;
    }

    private static Prototype[] toProtos(List<FuncInfo> fis) {
        return fis.stream().map(Fi2Proto::toProto).toArray(Prototype[]::new);
    }

    private static Object[] getConstants(FuncInfo fi) {
        Object[] consts = new Object[fi.constants.size()];
        fi.constants.forEach((c, idx) -> consts[idx] = c);
        return consts;
    }

    private static LocVar[] getLocVars(FuncInfo fi) {
        return fi.locVars.stream()
                .map(locVarInfo -> {
                    LocVar var = new LocVar();
                    var.setVarName(locVarInfo.name);
                    var.setStartPC(locVarInfo.startPC);
                    var.setEndPC(locVarInfo.endPC);
                    return var;
                })
                .toArray(LocVar[]::new);
    }

    private static Upvalue[] getUpvalues(FuncInfo fi) {
        Upvalue[] upvals = new Upvalue[fi.upvalues.size()];

        for (FuncInfo.UpvalInfo uvInfo : fi.upvalues.values()) {
            Upvalue upval = new Upvalue();
            upvals[uvInfo.index] = upval;
            // instack
            if (uvInfo.locVarSlot >= 0) {
                upval.setInstack((byte) 1);
                upval.setIdx((byte) uvInfo.locVarSlot);
            } else {
                upval.setInstack((byte) 0);
                upval.setIdx((byte) uvInfo.upvalIndex);
            }
        }

        return upvals;
    }

    private static String[] getUpvalueNames(FuncInfo fi) {
        String[] names = new String[fi.upvalues.size()];
        fi.upvalues.forEach((name, uvInfo) -> names[uvInfo.index] = name);
        return names;
    }

}
