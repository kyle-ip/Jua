package com.ywh.jua.compiler.codegen;

import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.BaseExp;
import com.ywh.jua.compiler.ast.BaseStat;
import com.ywh.jua.compiler.ast.exps.FuncCallExp;
import com.ywh.jua.compiler.ast.exps.NameExp;

import java.util.List;

import static com.ywh.jua.compiler.codegen.ExpProcessor.processExp;
import static com.ywh.jua.compiler.codegen.ExpProcessor.processTailCallExp;
import static com.ywh.jua.compiler.codegen.StatProcessor.processStat;

/**
 * 编译块处理器
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
class BlockProcessor {

    /**
     *
     * @param fi
     * @param node
     */
    static void processBlock(FuncInfo fi, Block node) {
        // 处理块中的每条语句
        for (BaseStat stat : node.getStats()) {
            processStat(fi, stat);
        }
        // 处理返回语句
        if (node.getRetExps() != null) {
            processRetStat(fi, node.getRetExps(), node.getLastLine());
        }
    }

    /**
     * 处理返回语句
     *
     * @param fi
     * @param exps
     * @param lastLine
     */
    private static void processRetStat(FuncInfo fi, List<BaseExp> exps, int lastLine) {
        int nExps = exps.size();
        if (nExps == 0) {
            fi.emitReturn(lastLine, 0, 0);
            return;
        }

        if (nExps == 1) {
            if (exps.get(0) instanceof NameExp) {
                NameExp nameExp = (NameExp) exps.get(0);
                int r = fi.slotOfLocVar(nameExp.getName());
                if (r >= 0) {
                    fi.emitReturn(lastLine, r, 1);
                    return;
                }
            }
            if (exps.get(0) instanceof FuncCallExp) {
                FuncCallExp fcExp = (FuncCallExp) exps.get(0);
                int r = fi.allocReg();
                processTailCallExp(fi, fcExp, r);
                fi.freeReg();
                fi.emitReturn(lastLine, r, -1);
                return;
            }
        }

        boolean multRet = ExpHelper.isVarargOrFuncCall(exps.get(nExps-1));
        for (int i = 0; i < nExps; i++) {
            BaseExp exp = exps.get(i);
            int r = fi.allocReg();
            if (i == nExps-1 && multRet) {
                processExp(fi, exp, r, -1);
            } else {
                processExp(fi, exp, r, 1);
            }
        }
        fi.freeRegs(nExps);

        // correct?
        int a = fi.usedRegs;
        if (multRet) {
            fi.emitReturn(lastLine, a, -1);
        } else {
            fi.emitReturn(lastLine, a, nExps);
        }
    }

}
