package com.ywh.jua.compiler.codegen;

import com.ywh.jua.chunk.Prototype;
import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.exps.FuncDefExp;

/**
 * 代码生成器
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class CodeGen {

    public static Prototype genProto(Block chunk) {
        FuncDefExp fd = new FuncDefExp();
        fd.setLastLine(chunk.getLastLine());
        fd.setVararg(true);
        fd.setBlock(chunk);

        FuncInfo fi = new FuncInfo(null, fd);
        fi.addLocVar("_ENV", 0);
        ExpProcessor.processFuncDefExp(fi, fd, 0);
        return Fi2Proto.toProto(fi.subFuncs.get(0));
    }

}
