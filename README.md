# Jua
![Jua](https://ywh-oss.oss-cn-shenzhen.aliyuncs.com/Jua-2.png) 


[![Java](https://img.shields.io/badge/language-Java-green.svg)]()
[![lua](https://img.shields.io/badge/language-lua-blue.svg)]()

A simplified Lua written in Java.

## Introduction

Jua is a simplified Lua (5.3) implementation written in Java. The purpose of building this project is to learn:

- Data Structure
- Register-based Virtual Machine & Instruction Set Design
- Compilers
- Lua grammar & Standard Libraries

## Features

- Virtual Machine: (Instruction Set & Runner)
- Compiler: Lexer, AST Parser & Code Generator
- Standard Libraries
- Metaprogramming

## ToDoList

- [ ] Label & Goto statement
- [ ] Userdata
- [ ] Coroutines Support
- [ ] Garbage Collector
- [x] Standard Libraries: 
    - [x] [basic library](#Basic)
    - [x] [mathematical functions](#Math)
    - [x] [string manipulation](#String)
    - [x] [package library](#Modules)
    - [ ] input and output
    - [ ] basic UTF-8 support
    - [ ] debug facilities
    - [ ] table manipulation
    - [ ] operating system facilities
    - [ ] coroutine library
- [ ] REPL
- [ ] Other interesting features

<!--

### Instruction Set (completed)

| No. | Symbol | Opcode   | Mode | Desc |
| ------ | ----------- | ----------- | ----------- | ----------- |
| 0    | OP_MOVE     | 0x00       | iABC   | `R(A) := R(B)` |
| 1   | OP_LOADK    | 0x01       | iABx   | `R(A) := Kst(Bx)` |
| 2  | OP_LOADKX   | 0x02       | iABx   | `R(A) := Kst(extra arg)` |
| 3 | OP_LOADBOOL | 0x03       | iABC   | `R(A) := (bool)B; if (C) pc++` |
| 4 | OP_LOADNIL  | 0x04       | iABC   | `R(A), R(A+1), ..., R(A+B) := nil` |
| 5 | OP_GETUPVAL | 0x05       | iABC   | `R(A) := UpValue[B]` |
| 6 | OP_GETTABUP | 0x06       | iABC   | `R(A) := UpValue[B][RK(C)]` |
| 7 | OP_GETTABLE | 0x07       | iABC   | `R(A) := R(B)[RK(C)]` |
| 8 | OP_SETTABUP | 0x08       | iABC   | `UpValue[A][RK(B)] := RK(C)` |
| 9 | OP_SETUPVAL | 0x09       | iABC   | `UpValue[B] := R(A)` |
| 10 | OP_SETTABLE | 0x0A       | iABC   | `R(A)[RK(B)] := RK(C)` |
| 11 | OP_NEWTABLE | 0x0B       | iABC   | `R(A) := {} (size = B,C)` |
| 12   | OP_SELF     | 0x0C       | iABC   | `R(A+1) := R(B); R(A) := R(B)[RK(C)]` |
| 13    | OP_ADD      | 0x0D       | iABC   | `R(A) := RK(B) + RK(C)` |
| 14    | OP_SUB      | 0x0E       | iABC   | `R(A) := RK(B) - RK(C)` |
| 15    | OP_MUL      | 0x0F       | iABC   | `R(A) := RK(B) * RK(C)` |
| 16    | OP_MOD      | 0x10       | iABC   | `R(A) := RK(B) % RK(C)` |
| 17    | OP_POW      | 0x11       | iABC   | `R(A) := RK(B) ^ RK(C)` |
| 18    | OP_DIV      | 0x12       | iABC   | `R(A) := RK(B) / RK(C)` |
| 19   | OP_IDIV     | 0x13       | iABC   | `R(A) := RK(B) // RK(C)` |
| 20   | OP_BAND     | 0x14       | iABC   | `R(A) := RK(B) & RK(C)` |
| 21    | OP_BOR      | 0x15       | iABC   | `R(A) := RK(B)` |
| 22   | OP_BXOR     | 0x16       | iABC   | `R(A) := RK(B) ~ RK(C)` |
| 23    | OP_SHL      | 0x17       | iABC   | `R(A) := RK(B) << RK(C)` |
| 24    | OP_SHR      | 0x18       | iABC   | `R(A) := RK(B) >> RK(C)` |
| 25    | OP_UNM      | 0x19       | iABC   | `R(A) := -R(B)` |
| 26   | OP_BNOT     | 0x1A       | iABC   | `R(A) := ~R(B)` |
| 27   | OP_NOT      | 0x1B       | iABC   | `R(A) := not R(B)` |
| 28    | OP_LEN      | 0x1C       | iABC   | `R(A) := length of R(B)` |
| 29 | OP_CONCAT   | 0x1D       | iABC   | `R(A) := R(B).. ... ..R(C)` |
| 30    | OP_JMP      | 0x1E       | iAsBx  | `pc+=sBx; if (A) close all upvalues >= R(A - 1)` |
| 31     | OP_EQ       | 0x1F       | iABC   | `if ((RK(B) == RK(C)) ~= A) then pc++` |
| 32     | OP_LT       | 0x20       | iABC   | `if ((RK(B) <  RK(C)) ~= A) then pc++` |
| 33     | OP_LE       | 0x21       | iABC   | `if ((RK(B) <= RK(C)) ~= A) then pc++` |
| 34   | OP_TEST     | 0x22       | iABC   | `if not (R(A) <=> C) then pc++` |
| 35 | OP_TESTSET  | 0x23       | iABC   | `if (R(B) <=> C) then R(A) := R(B) else pc++` |
| 36   | OP_CALL     | 0x24       | iABC   | `R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))` |
| 37 | OP_TAILCALL | 0x25       | iABC   | `return R(A)(R(A+1), ... ,R(A+B-1))` |
| 38 | OP_RETURN   | 0x26       | iABC   | `return R(A), ... ,R(A+B-2)` |
| 39 | OP_FORLOOP  | 0x27       | iABC   | `R(A)+=R(A+2); if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }` |
| 40 | OP_FORPREP  | 0x28       | iAsBx  | `R(A)-=R(A+2); pc+=sBx` |
| 41 | OP_TFORCALL | 0x29       | iABC   | `R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2));` |
| 42 | OP_TFORLOOP | 0x2A       | iAsBx  | `if R(A+1) ~= nil then { R(A)=R(A+1); pc += sBx }` |
| 43 | OP_SETLIST  | 0x2B       | iABC   | `R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B` |
| 44 | OP_CLOSURE  | 0x2C       | iABx   | `R(A) := closure(KPROTO[Bx])` |
| 45 | OP_VARARG   | 0x2D       | iABC   | `R(A), R(A+1), ..., R(A+B-2) = vararg` |
| 46 | OP_EXTRAARG | 0x2E       |    | `extra (larger) argument for previous opcode` |

-->

## Standard Libraries

[Lua 5.3 Reference Manual](http://www.lua.org/manual/5.3/manual.html)

### <span id="Basic">Basic</span>

| No.  | Function     | Synopsis                                    |
| ---- | ------------ | ------------------------------------------- |
| 0    | print        | print (···)                                 |
| 1    | assert       | assert (v [, message])                      |
| 2    | error        | error (message [, level])                   |
| 3    | select       | select (index, ···)                         |
| 4    | ipairs       | ipairs (t)                                  |
| 5    | pairs        | pairs (t)                                   |
| 6    | next         | next (table [, index])                      |
| 7    | load         | load (chunk [, chunkname [, mode [, env]]]) |
| 8    | loadfile     | loadfile ([filename [, mode [, env]]])      |
| 9    | dofile       | dofile ([filename])                         |
| 10   | pcall        | pcall (f [, arg1, ···])                     |
| 11   | xpcall       | xpcall (f, msgh [, arg1, ···])              |
| 12   | getmetatable | getmetatable (object)                       |
| 13   | setmetatable | setmetatable (table, metatable)             |
| 14   | rawequal     | rawequal (v1, v2)                           |
| 15   | rawlen       | rawlen (v)                                  |
| 16   | rawget       | rawget (table, index)                       |
| 17   | rawset       | rawset (table, index, value)                |
| 18   | type         | type (v)                                    |
| 19   | tostring     | tostring (v)                                |
| 20   | tonumber     | tonumber (e [, base])                       |

### <span id="Math">Math</span>

| No.  | Function   | Synopsis                                          |
| ---- | ---------- | ------------------------------------------------- |
| 0    | random     | math.random ([m [, n]])                           |
| 1    | randomseed | math.randomseed (x)                               |
| 2    | max        | math.max (x, ···)                                 |
| 3    | min        | math.min (x, ···)                                 |
| 4    | exp        | math.exp (x)                                      |
| 5    | log        | math.log (x [, base])                             |
| 6    | deg        | math.deg (x)                                      |
| 7    | rad        | math.rad (x)                                      |
| 8    | sin        | math.sin (x)                                      |
| 9    | cos        | math.cos (x)                                      |
| 10   | tan        | math.tan (x)                                      |
| 11   | asin       | math.asin (x)                                     |
| 12   | acos       | math.acos (x)                                     |
| 13   | atan       | math.atan (y [, x])                               |
| 14   | ceil       | math.ceil (x)                                     |
| 15   | floor      | math.floor (x)                                    |
| 16   | abs        | math.abs (x)                                      |
| 17   | sqrt       | math.sqrt (x)                                     |
| 18   | fmod       | math.abs (x)                                      |
| 19   | modf       | math.modf (x)                                     |
| 20   | ult        | math.ult (m, n)                                   |
| 21   | tointeger  | math.tointeger (x)                                |
| 22   | type       | math.type (x)                                     |
| 23   | pi         | The value of *π*.                                 |
| 24   | huge       | A value larger than any other numeric value.      |
| 25   | maxinteger | An integer with the maximum value for an integer. |
| 26   | mininteger | An integer with the minimum value for an integer. |

### <span id="String">String</span>

| No.  | Function | Synopsis                  |
| ---- | -------- | ------------------------- |
| 0    | len      | string.len (s)            |
| 1    | rep      | string.rep (s, n [, sep]) |
| 2    | lower    | string.lower (s)          |
| 3    | upper    | string.upper (s)          |
|      |          |                           |

...

### <span id="Modules">Modules</span>

| No.  | Function   | Synopsis                                        |
| ---- | ---------- | ----------------------------------------------- |
| 0    | require    | require (modname)                               |
| 1    | searchers  | package.searchers                               |
| 2    | searchpath | package.searchpath (name, path [, sep [, rep]]) |
|      |            |                                                 |

...

## Example

### Dependencies

- JDK 1.8+
- Maven 3+ 

### Build

```shell
mvn clean package
```

### Script

sum.lua
```lua
function sum()
    local s = 0
    for i = 1, 100 do
        if i % 2 == 0 then
            s = s + math.sqrt(i)
        end
    end
    return s
end
print(sum())
```

### Run

```shell
$ java -jar jua-5.3.5-alpha.jar sum.lua

             ,---._
           .-- -.' \
           |    |   :
           |    |   |         ,--,
           |    |   |       ,'_ /|
           |    |   |  .--. |  | :    ,--.--.
           |    |   |,'_ /| :  . |   /       \
           |    |   ||  ' | |  . .  .--.  .-. |
       ___ |    |   ||  | ' |  | |   \__\/| . .
     /    /\    |   ||  | : ;  ; |   ," .--.; |
    /  ../  `..-    ,|  |  `--'   \ /  /  ,.  |
    \    \         ; :  ,      .-./;  :  |..'  \
     \    \      ,'   `--`----'    |  ,     .-./
      "---....--'                   `--`---'
    :: jua ::                      (5.3.5-alpha)

338.048
```

## Links

- [lua.go](https://github.com/zxh0/lua.go)
- [Lua 5.3 Reference Manual](http://www.lua.org/manual/5.3/manual.html)
- [Lua 5.3 Test suites](http://www.lua.org/tests/lua-5.3.4-tests.tar.gz)
- ...

## License

See LICENSE file.
