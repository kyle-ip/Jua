package com.ywh.jua.compiler.parser;

import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.Exp;
import com.ywh.jua.compiler.ast.Stat;
import com.ywh.jua.compiler.ast.exps.*;
import com.ywh.jua.compiler.ast.stats.*;
import com.ywh.jua.compiler.lexer.Lexer;
import com.ywh.jua.compiler.lexer.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.ywh.jua.compiler.lexer.TokenKind.*;
import static com.ywh.jua.compiler.parser.BlockParser.parseBlock;
import static com.ywh.jua.compiler.parser.ExpParser.*;
import static com.ywh.jua.compiler.parser.PrefixExpParser.parsePrefixExp;

/**
 * 语句解析器
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
class StatParser {


    /**
     * 解析表达式
     *
     * stat ::=  ‘;’
     *     | break
     *     | ‘::’ Name ‘::’
     *     | goto Name
     *     | do block end
     *     | while exp do block end
     *     | repeat block until exp
     *     | if exp then block {elseif exp then block} [else block] end
     *     | for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
     *     | for namelist in explist do block end
     *     | function funcname funcbody
     *     | local function Name funcbody
     *     | local namelist [‘=’ explist]
     *     | varlist ‘=’ explist
     *     | functioncall
     *
     * @param lexer
     * @return
     */
    static Stat parseStat(Lexer lexer) {
        switch (lexer.LookAhead()) {
            case TOKEN_SEP_SEMI:
                return parseEmptyStat(lexer);
            case TOKEN_KW_BREAK:
                return parseBreakStat(lexer);
            case TOKEN_SEP_LABEL:
                return parseLabelStat(lexer);
            case TOKEN_KW_GOTO:
                return parseGotoStat(lexer);
            case TOKEN_KW_DO:
                return parseDoStat(lexer);
            case TOKEN_KW_WHILE:
                return parseWhileStat(lexer);
            case TOKEN_KW_REPEAT:
                return parseRepeatStat(lexer);
            case TOKEN_KW_IF:
                return parseIfStat(lexer);
            case TOKEN_KW_FOR:
                return parseForStat(lexer);
            case TOKEN_KW_FUNCTION:
                return parseFuncDefStat(lexer);
            case TOKEN_KW_LOCAL:
                return parseLocalAssignOrFuncDefStat(lexer);
            default:
                return parseAssignOrFuncCallStat(lexer);
        }
    }

    /**
     * 解析空语句
     *
     * ;
     *
     * @param lexer
     * @return
     */
    private static EmptyStat parseEmptyStat(Lexer lexer) {
        // 跳过分号。
        lexer.nextTokenOfKind(TOKEN_SEP_SEMI);
        return EmptyStat.INSTANCE;
    }

    /**
     * 解析 break 语句
     *
     * break
     *
     * @param lexer
     * @return
     */
    private static BreakStat parseBreakStat(Lexer lexer) {
        // 跳过关键字，记录行号。
        lexer.nextTokenOfKind(TOKEN_KW_BREAK);
        return new BreakStat(lexer.line());
    }

    /**
     * 解析标签语句
     *
     * ‘::’ Name ‘::’
     *
     * @param lexer
     * @return
     */
    private static LabelStat parseLabelStat(Lexer lexer) {
        // 跳过分隔符，记录标签名。
        lexer.nextTokenOfKind(TOKEN_SEP_LABEL);          // ::
        String name = lexer.nextIdentifier().getValue(); // name
        lexer.nextTokenOfKind(TOKEN_SEP_LABEL);          // ::
        return new LabelStat(name);
    }

    /**
     * 解析 goto 语句
     *
     * goto Name
     *
     * @param lexer
     * @return
     */
    private static GotoStat parseGotoStat(Lexer lexer) {
        // 跳过关键字，记录标签名。
        lexer.nextTokenOfKind(TOKEN_KW_GOTO);
        String name = lexer.nextIdentifier().getValue();
        return new GotoStat(name);
    }

    /**
     * 解析 do ... end 语句
     *
     * do block end
     *
     * @param lexer
     * @return
     */
    private static DoStat parseDoStat(Lexer lexer) {
        // 跳过关键字 do，解析块，跳过关键字 end。
        lexer.nextTokenOfKind(TOKEN_KW_DO);
        Block block = parseBlock(lexer);
        lexer.nextTokenOfKind(TOKEN_KW_END);
        return new DoStat(block);
    }

    /**
     * 解析 while ... do ... end 语句
     *
     * while exp do block end
     *
     * @param lexer
     * @return
     */
    private static WhileStat parseWhileStat(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_KW_WHILE);
        Exp exp = parseExp(lexer);
        lexer.nextTokenOfKind(TOKEN_KW_DO);
        Block block = parseBlock(lexer);
        lexer.nextTokenOfKind(TOKEN_KW_END);
        return new WhileStat(exp, block);
    }

    /**
     * 解析 repeat ... until 语句
     *
     * repeat block until exp
     *
     * @param lexer
     * @return
     */
    private static RepeatStat parseRepeatStat(Lexer lexer) {

        // repeat
        lexer.nextTokenOfKind(TOKEN_KW_REPEAT);

        // block
        Block block = parseBlock(lexer);

        // until
        lexer.nextTokenOfKind(TOKEN_KW_UNTIL);

        // exp
        Exp exp = parseExp(lexer);
        return new RepeatStat(block, exp);
    }

    /**
     * 解析 if ... elseif ... else ... 语句
     *
     * if exp then block {elseif exp then block} [else block] end
     *
     * @param lexer
     * @return
     */
    private static IfStat parseIfStat(Lexer lexer) {
        List<Exp> exps = new ArrayList<>();
        List<Block> blocks = new ArrayList<>();

        // if exp then block
        lexer.nextTokenOfKind(TOKEN_KW_IF);
        exps.add(parseExp(lexer));
        lexer.nextTokenOfKind(TOKEN_KW_THEN);
        blocks.add(parseBlock(lexer));

        // 前瞻，如果后续有一或多次出现关键字 elseif，则依次跳过关键字、提取表达式、解析块。
        // elseif
        while (lexer.LookAhead() == TOKEN_KW_ELSEIF) {
            lexer.nextToken();
            // exp
            exps.add(parseExp(lexer));
            // then
            lexer.nextTokenOfKind(TOKEN_KW_THEN);
            // block
            blocks.add(parseBlock(lexer));
        }

        // 前瞻，如果后续有关键字 else，则依次跳过关键字、提取 true 表达式、解析块。
        // else
        if (lexer.LookAhead() == TOKEN_KW_ELSE) {
            lexer.nextToken();
            exps.add(new TrueExp(lexer.line()));
            // block
            blocks.add(parseBlock(lexer));
        }

        // end
        lexer.nextTokenOfKind(TOKEN_KW_END);
        return new IfStat(exps, blocks);
    }

    /**
     * 解析 for 语句
     *
     * for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
     * for namelist in explist do block end
     *
     * @param lexer
     * @return
     */
    private static Stat parseForStat(Lexer lexer) {
        // 跳过 for 关键字并取行号。
        int lineOfFor = lexer.nextTokenOfKind(TOKEN_KW_FOR).getLine();
        String name = lexer.nextIdentifier().getValue();
        // 前瞻，判断下一个 token。
        // 是等号则按数值 for 循环解析；否则按通用 for 循环解析。
        if (lexer.LookAhead() == TOKEN_OP_ASSIGN) {
            return finishForNumStat(lexer, name, lineOfFor);
        } else {
            return finishForInStat(lexer, name);
        }
    }

    /**
     * 解析数值 for 语句
     *
     * for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
     *
     * @param lexer
     * @param name
     * @param lineOfFor
     * @return
     */
    private static ForNumStat finishForNumStat(Lexer lexer, String name, int lineOfFor) {
        ForNumStat stat = new ForNumStat();
        // for
        stat.setLineOfFor(lineOfFor);
        // name
        stat.setVarName(name);

        // 关键字 for 和标识符已经读取，直接从等号开始解析。
        // =
        lexer.nextTokenOfKind(TOKEN_OP_ASSIGN);
        // exp
        stat.setInitExp(parseExp(lexer));
        // ,
        lexer.nextTokenOfKind(TOKEN_SEP_COMMA);
        // exp
        stat.setLimitExp(parseExp(lexer));
        // , exp
        if (lexer.LookAhead() == TOKEN_SEP_COMMA) {
            lexer.nextToken();
            stat.setStepExp(parseExp(lexer));
        } else {
            // 步长补上缺省值 1
            stat.setStepExp(new IntegerExp(lexer.line(), 1));
        }
        // do
        lexer.nextTokenOfKind(TOKEN_KW_DO);
        stat.setLineOfDo(lexer.line());
        // block
        stat.setBlock(parseBlock(lexer));
        // end
        lexer.nextTokenOfKind(TOKEN_KW_END);
        return stat;
    }

    /**
     * 解析通用 for 语句
     *
     * for namelist in explist do block end
     * namelist ::= Name {‘,’ Name}
     * explist ::= exp {‘,’ exp}
     *
     * @param lexer
     * @param name0
     * @return
     */
    private static ForInStat finishForInStat(Lexer lexer, String name0) {
        ForInStat stat = new ForInStat();

        // 关键字 for 和第一个标识符已经读取，继续解析标识符列表。
        // namelist
        stat.setNameList(finishNameList(lexer, name0));
        // in
        lexer.nextTokenOfKind(TOKEN_KW_IN);
        // explist
        stat.setExpList(parseExpList(lexer));
        // do
        lexer.nextTokenOfKind(TOKEN_KW_DO);
        stat.setLineOfDo(lexer.line());
        // block
        stat.setBlock(parseBlock(lexer));
        // end
        lexer.nextTokenOfKind(TOKEN_KW_END);
        return stat;
    }

    /**
     * 整合第一个标识符和剩余的标识符列表
     *
     * namelist ::= Name {‘,’ Name}
     *
     * @param lexer
     * @param name0
     * @return
     */
    private static List<String> finishNameList(Lexer lexer, String name0) {
        List<String> names = new ArrayList<>();
        names.add(name0);
        while (lexer.LookAhead() == TOKEN_SEP_COMMA) {
            lexer.nextToken();                            // ,
            names.add(lexer.nextIdentifier().getValue()); // Name
        }
        return names;
    }

    /**
     * 解析局部变量声明和函数定义语句
     *
     * local function Name funcbody
     * local namelist [‘=’ explist]
     *
     * @param lexer
     * @return
     */
    private static Stat parseLocalAssignOrFuncDefStat(Lexer lexer) {

        // local
        lexer.nextTokenOfKind(TOKEN_KW_LOCAL);

        // 局部函数定义
        if (lexer.LookAhead() == TOKEN_KW_FUNCTION) {
            return finishLocalFuncDefStat(lexer);
        }

        // 局部变量声明
        else {
            return finishLocalVarDeclStat(lexer);
        }
    }


    /**
     * http://www.lua.org/manual/5.3/manual.html#3.4.11
     *
     *     function f() end          =>  f = function() end
     *     function t.a.b.c.f() end  =>  t.a.b.c.f = function() end
     *     function t.a.b.c:f() end  =>  t.a.b.c.f = function(self) end
     *     local function f() end    =>  local f; f = function() end
     *
     *     The statement `local function f () body end`
     *     translates to `local f; f = function () body end`
     *     not to `local f = function () body end`
     *     (This only makes a difference when the body of the function contains references to f.)
     *
     * local function Name funcbody
     *
     * @param lexer
     * @return
     */
    private static LocalFuncDefStat finishLocalFuncDefStat(Lexer lexer) {
        // function
        lexer.nextTokenOfKind(TOKEN_KW_FUNCTION);
        // name
        String name = lexer.nextIdentifier().getValue();
        // funcbody
        FuncDefExp fdExp = parseFuncDefExp(lexer);
        return new LocalFuncDefStat(name, fdExp);
    }

    // local namelist [‘=’ explist]
    private static LocalVarDeclStat finishLocalVarDeclStat(Lexer lexer) {
        String name0 = lexer.nextIdentifier().getValue();     // local Name
        List<String> nameList = finishNameList(lexer, name0); // { , Name }
        List<Exp> expList = null;
        if (lexer.LookAhead() == TOKEN_OP_ASSIGN) {
            lexer.nextToken();                                // ==
            expList = parseExpList(lexer);                    // explist
        }
        int lastLine = lexer.line();
        return new LocalVarDeclStat(lastLine, nameList, expList);
    }


    /**
     * 解析赋值和函数调用语句
     * 赋值语句和函数调用语句都以前缀表达式（任意长）开始，
     * 所以需要有前瞻无数个 token 的能力才能区分这两种语句，或者借助回溯来解析。
     * 由于函数调用既可以是语句，也可以是前缀表达式，但一定不是 var 表达式。
     * 因此可以解析一个前缀表达式，看它是否函数调用。
     * 如果是则解析出来的是一条函数调用语句，否则解析出来的必须是 var 表达式，继续解析剩余赋值语句即可。
     *
     * varlist ‘=’ explist
     * functioncall
     *
     * @param lexer
     * @return
     */
    private static Stat parseAssignOrFuncCallStat(Lexer lexer) {

        // 解析前缀表达式。
        Exp prefixExp = parsePrefixExp(lexer);
        if (prefixExp instanceof FuncCallExp) {
            return new FuncCallStat((FuncCallExp) prefixExp);
        } else {
            return parseAssignStat(lexer, prefixExp);
        }
    }

    /**
     * 解析赋值语句
     *
     * varlist ‘=’ explist |
     *
     * @param lexer
     * @param var0
     * @return
     */
    private static AssignStat parseAssignStat(Lexer lexer, Exp var0) {
        // varlist
        List<Exp> varList = finishVarList(lexer, var0);
        // =
        lexer.nextTokenOfKind(TOKEN_OP_ASSIGN);
        // explist
        List<Exp> expList = parseExpList(lexer);
        int lastLine = lexer.line();
        return new AssignStat(lastLine, varList, expList);
    }

    /**
     * 解析赋值参数列表
     *
     * varlist ::= var {‘,’ var}
     *
     * @param lexer
     * @param var0
     * @return
     */
    private static List<Exp> finishVarList(Lexer lexer, Exp var0) {
        List<Exp> vars = new ArrayList<>();
        // var
        vars.add(checkVar(lexer, var0));
        while (lexer.LookAhead() == TOKEN_SEP_COMMA) {
            // ,
            lexer.nextToken();
            // var
            Exp exp = parsePrefixExp(lexer);
            vars.add(checkVar(lexer, exp));
        }
        return vars;
    }

    /**
     * 校验 var 表达式
     *
     * var ::=  Name | prefixexp ‘[’ exp ‘]’ | prefixexp ‘.’ Name
     *
     * @param lexer
     * @param exp
     * @return
     */
    private static Exp checkVar(Lexer lexer, Exp exp) {

        // t.k 或 t["k"]
        if (exp instanceof NameExp || exp instanceof TableAccessExp) {
            return exp;
        }
        lexer.nextTokenOfKind(null); // trigger error
        throw new RuntimeException("unreachable!");
    }

    /**
     * 解析函数体
     *
     * function funcname funcbody
     * funcname ::= Name {‘.’ Name} [‘:’ Name]
     * funcbody ::= ‘(’ [parlist] ‘)’ block end
     * parlist ::= namelist [‘,’ ‘...’] | ‘...’
     * namelist ::= Name {‘,’ Name}
     *
     * @param lexer
     * @return
     */
    private static AssignStat parseFuncDefStat(Lexer lexer) {

        // function
        lexer.nextTokenOfKind(TOKEN_KW_FUNCTION);

        // funcname
        Map<Exp, Boolean> map = parseFuncName(lexer);
        Exp fnExp = map.keySet().iterator().next();
        boolean hasColon = map.values().iterator().next();

        // funcbody
        FuncDefExp fdExp = parseFuncDefExp(lexer);

        // insert self
        if (hasColon) {
            if (fdExp.getParList() == null) {
                fdExp.setParList(new ArrayList<>());
            }
            fdExp.getParList().add(0, "self");
        }

        return new AssignStat(fdExp.getLastLine(), Collections.singletonList(fnExp), Collections.singletonList(fdExp));
    }

    /**
     * 解析函数名
     *
     * funcname ::= Name {‘.’ Name} [‘:’ Name]
     *
     * @param lexer
     * @return
     */
    private static Map<Exp, Boolean> parseFuncName(Lexer lexer) {

        // 取标识符为名称表达式
        Token id = lexer.nextIdentifier();
        Exp exp = new NameExp(id.getLine(), id.getValue());
        boolean hasColon = false;


        // .（t.k <=> t["k"]）
        while (lexer.LookAhead() == TOKEN_SEP_DOT) {
            lexer.nextToken();
            id = lexer.nextIdentifier();
            Exp idx = new StringExp(id);
            exp = new TableAccessExp(id.getLine(), exp, idx);
        }

        // :（类方法语法糖，去除后函数名为一串记录访问表达式，去掉记录访问表达式后是一串表索引访问表达式）
        if (lexer.LookAhead() == TOKEN_SEP_COLON) {
            lexer.nextToken();
            id = lexer.nextIdentifier();
            Exp idx = new StringExp(id);
            exp = new TableAccessExp(id.getLine(), exp, idx);
            hasColon = true;
        }

        // workaround: return multiple values
        return Collections.singletonMap(exp, hasColon);
    }

}
