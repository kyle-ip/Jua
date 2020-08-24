package com.ywh.jua;

import com.ywh.jua.api.LuaState;
import com.ywh.jua.api.LuaType;
import com.ywh.jua.chunk.LocVar;
import com.ywh.jua.chunk.Prototype;
import com.ywh.jua.chunk.Upvalue;
import com.ywh.jua.state.LuaStateImpl;
import com.ywh.jua.vm.OpCode;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.ywh.jua.vm.Instruction.*;
import static com.ywh.jua.vm.OpArgMask.*;

/**
 * @author ywh
 * @since 2020/8/17 11:26
 */
public class Main {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String fileName = "G:\\demo\\jua\\src\\test\\resources\\vector2.luac";
        byte[] data = Files.readAllBytes(Paths.get(fileName));
//        Prototype proto = BinaryChunk.undump(data);
//        list(proto);

//        LuaState ls = new LuaStateImpl();
//        ls.load(data, fileName, "b");
//        ls.call(0, 0);

        LuaState ls = new LuaStateImpl();
        ls.register("print", Main::print);
        ls.register("getmetatable", Main::getMetatable);
        ls.register("setmetatable", Main::setMetatable);
        ls.load(data, fileName, "b");
        ls.call(0, 0);
    }

    private static int print(LuaState ls) {
        int nArgs = ls.getTop();
        for (int i = 1; i <= nArgs; i++) {
            if (ls.isBoolean(i)) {
                System.out.print(ls.toBoolean(i));
            } else if (ls.isString(i)) {
                System.out.print(ls.toString(i));
            } else {
                System.out.print(ls.typeName(ls.type(i)));
            }
            if (i < nArgs) {
                System.out.print("\t");
            }
        }
        System.out.println();
        return 0;
    }



    private static int getMetatable(LuaState ls) {
        if (!ls.getMetatable(1)) {
            ls.pushNil();
        }
        return 1;
    }

    private static int setMetatable(LuaState ls) {
        ls.setMetatable(1);
        return 1;
    }

//    /**
//     * 从函数原型解析指令
//     *
//     * @param proto
//     */
//    private static void luaMain(Prototype proto) {
//        LuaVM vm = new LuaStateImpl(proto);
//        vm.setTop(proto.getMaxStackSize());
//        for (;;) {
//            // 取程序计数器、下一条指令
//            int pc = vm.getPC(), i = vm.fetch();
//            OpCode opCode = Instruction.getOpCode(i);
//            if (opCode != OpCode.RETURN && opCode.getAction() != null) {
//                opCode.getAction().execute(i, vm);
//                // 打印 PC 和指令名称
//                System.out.printf("[%02d] %-8s ", pc + 1, opCode.name());
//                // 打印栈
//                printStack(vm);
//            } else {
//                break;
//            }
//        }
//    }

    /**
     * 打印 Lua 栈
     *
     * @param ls
     */
    private static void printStack(LuaState ls) {
        for (int i = 1; i <= ls.getTop(); i++) {
            LuaType t = ls.type(i);
            switch (t) {
                case LUA_TBOOLEAN:
                    System.out.printf("[%b]", ls.toBoolean(i));
                    break;
                case LUA_TNUMBER:
                    if (ls.isInteger(i)) {
                        System.out.printf("[%d]", ls.toInteger(i));
                    } else {
                        System.out.printf("[%f]", ls.toNumber(i));
                    }
                    break;
                case LUA_TSTRING:
                    System.out.printf("[\"%s\"]", ls.toString(i));
                    break;
                default: // other values
                    System.out.printf("[%s]", ls.typeName(t));
                    break;
            }
        }
        System.out.println();
    }

    /**
     * 打印函数原型信息
     *
     * @param f
     */
    private static void list(Prototype f) {
        printHeader(f);
        printCode(f);
        printDetail(f);
        for (Prototype p : f.getProtos()) {
            list(p);
        }
        System.out.println();
    }

    /**
     * 打印函数原型头部
     *
     * @param f
     */
    private static void printHeader(Prototype f) {
        String funcType = f.getLineDefined() > 0 ? "function" : "main";
        String varargFlag = f.getIsVararg() > 0 ? "+" : "";
        System.out.printf("\n%s <%s:%d,%d> (%d instructions)\n", funcType, f.getSource(), f.getLineDefined(),
            f.getLastLineDefined(), f.getCode().length);
        System.out.printf("%d%s params, %d slots, %d upvalues, ", f.getNumParams(), varargFlag, f.getMaxStackSize(),
            f.getUpvalues().length);
        System.out.printf("%d locals, %d constants, %d functions\n", f.getLocVars().length, f.getConstants().length,
            f.getProtos().length);
    }

    /**
     * 打印函数原型指令表
     *
     * @param f
     */
    private static void printCode(Prototype f) {
        int[] code = f.getCode();
        int[] lineInfo = f.getLineInfo();

        // 遍历指令表
        for (int i = 0; i < code.length; i++) {
            String line = lineInfo.length > 0 ? String.valueOf(lineInfo[i]) : "-";
            System.out.printf("\t%d\t[%s]\t%-8s \t", i + 1, line, getOpCode(code[i]));

            // 输出指令表操作数
            printOperands(code[i]);
            System.out.println();
        }
    }

    /**
     * 打印操作数
     *
     * @param i
     */
    private static void printOperands(int i) {
        OpCode opCode = getOpCode(i);

        // 根据编码模式判断分支
        //      判断操作数类型
        switch (opCode.getOpMode()) {
            case iABC:
                // 先取操作数 A。
                System.out.printf("%d", getA(i));

                // 操作数 B 或 C 在某些指令可能未被使用，所以不一定会打印；而且最高位是 1 表示常量表索引，按负数输出。
                if (opCode.getArgBMode() != OpArgN) {
                    int b = getB(i);
                    System.out.printf(" %d", b > 0xFF ? -1 - (b & 0xFF) : b);
                }
                if (opCode.getArgCMode() != OpArgN) {
                    int c = getC(i);
                    System.out.printf(" %d", c > 0xFF ? -1 - (c & 0xFF) : c);
                }
                break;
            case iABx:
                // 先取操作数 A。
                System.out.printf("%d", getA(i));
                // 再取操作数 Bx，如果表示常量表索引，则按负数输出。
                int bx = getBx(i);
                if (opCode.getArgBMode() == OpArgK) {
                    System.out.printf(" %d", -1 - bx);
                } else if (opCode.getArgBMode() == OpArgU) {
                    System.out.printf(" %d", bx);
                }
                break;
            case iAsBx:
                // 打印操作数 A 和 sBx。
                System.out.printf("%d %d", getA(i), getSBx(i));
                break;
            case iAx:
                // 打印操作数 Ax。
                System.out.printf("%d", -1 - getAx(i));
                break;
            default:
                break;
        }
    }

    /**
     * 打印函数原型常量表，局部变量表，Upvalue 表
     *
     * @param f
     */
    private static void printDetail(Prototype f) {
        System.out.printf("constants (%d):\n", f.getConstants().length);
        int i = 1;
        for (Object k : f.getConstants()) {
            System.out.printf("\t%d\t%s\n", i++, constantToString(k));
        }

        i = 0;
        System.out.printf("locals (%d):\n", f.getLocVars().length);
        for (LocVar locVar : f.getLocVars()) {
            System.out.printf("\t%d\t%s\t%d\t%d\n", i++,
                locVar.getVarName(), locVar.getStartPC() + 1, locVar.getEndPC() + 1);
        }

        i = 0;
        System.out.printf("upvalues (%d):\n", f.getUpvalues().length);
        for (Upvalue upval : f.getUpvalues()) {
            String name = f.getUpvalueNames().length > 0 ? f.getUpvalueNames()[i] : "-";
            System.out.printf("\t%d\t%s\t%d\t%d\n", i++,
                name, upval.getInstack(), upval.getIdx());
        }
    }

    /**
     * 常量转字符串
     *
     * @param k
     * @return
     */
    private static String constantToString(Object k) {
        if (k == null) {
            return "nil";
        } else if (k instanceof String) {
            return "\"" + k + "\"";
        } else {
            return k.toString();
        }
    }

}
