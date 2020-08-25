package com.ywh.jua.compiler;


import com.ywh.jua.chunk.Prototype;
import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.codegen.CodeGen;
import com.ywh.jua.compiler.parser.Parser;

/**
 * 编译器
 *
 * @author ywh
 * @since 2020/8/18 11:26
 */
public class Compiler {

    /**
     * 编译
     *
     * @param chunk
     * @param chunkName
     * @return
     */
    public static Prototype compile(String chunk, String chunkName) {
        Block ast = Parser.parse(chunk, chunkName);
        Prototype proto = CodeGen.genProto(ast);
        setSource(proto, chunkName);
        return proto;
    }

    /**
     * 设置源码
     *
     * @param proto
     * @param chunkName
     */
    private static void setSource(Prototype proto, String chunkName) {
        proto.setSource(chunkName);
        for (Prototype subProto : proto.getProtos()) {
            setSource(subProto, chunkName);
        }
    }

}
