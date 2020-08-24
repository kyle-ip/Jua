# Jua

[![Java](https://img.shields.io/badge/language-Java-green.svg)]()
[![lua](https://img.shields.io/badge/language-lua-blue.svg)]()

A simple Lua written in Java.

## Introduction

Jua is a toy Lua 5.3 implementation written in Java. 

The goals of this project is learning:

- Data Structure
- The Principles of Register-based Virtual Machine
- Compilers
- Lua grammar & Standard Library

## Features

- Virtual Machine: (Instructions & Runner)
- Compiler: Lexer, AST & Code Generator
- Standard Library (Work in Progress)

### Instruction Set (completed)

| No. | Symbol | Opcode   | Desc |
| ------ | ----------- | ----------- | ----------- |
| 0    | OP_MOVE     | 0x00       |        |
| 1   | OP_LOADK    | 0x01       |        |
| 2  | OP_LOADKX   | 0x02       |        |
| 3 | OP_LOADBOOL | 0x03       |        |
| 4 | OP_LOADNIL  | 0x04       |        |
| 5 | OP_GETUPVAL | 0x05       |        |
| 6 | OP_GETTABUP | 0x06       |        |
| 7 | OP_GETTABLE | 0x07       |        |
| 8 | OP_SETTABUP | 0x08       |        |
| 9 | OP_SETUPVAL | 0x09       |        |
| 10 | OP_SETTABLE | 0x0A       |        |
| 11 | OP_NEWTABLE | 0x0B       |        |
| 12   | OP_SELF     | 0x0C       |        |
| 13    | OP_ADD      | 0x0D       |        |
| 14    | OP_SUB      | 0x0E       |        |
| 15    | OP_MUL      | 0x0F       |        |
| 16    | OP_MOD      | 0x10       |        |
| 17    | OP_POW      | 0x11       |        |
| 18    | OP_DIV      | 0x12       |        |
| 19   | OP_IDIV     | 0x13       |        |
| 20   | OP_BAND     | 0x14       |        |
| 21    | OP_BOR      | 0x15       |        |
| 22   | OP_BXOR     | 0x16       |        |
| 23    | OP_SHL      | 0x17       |        |
| 24    | OP_SHR      | 0x18       |        |
| 25    | OP_UNM      | 0x19       |        |
| 26   | OP_BNOT     | 0x1A       |        |
| 27   | OP_NOT      | 0x1B       |        |
| 28    | OP_LEN      | 0x1C       |        |
| 29 | OP_CONCAT   | 0x1D       |        |
| 30    | OP_JMP      | 0x1E       |        |
| 31     | OP_EQ       | 0x1F       |        |
| 32     | OP_LT       | 0x20       |        |
| 33     | OP_LE       | 0x21       |        |
| 34   | OP_TEST     | 0x22       |        |
| 35 | OP_TESTSET  | 0x23       |        |
| 36   | OP_CALL     | 0x24       |        |
| 37 | OP_TAILCALL | 0x25       |        |
| 38 | OP_RETURN   | 0x26       |        |
| 39 | OP_FORLOOP  | 0x27       |        |
| 40 | OP_FORPREP  | 0x28       |        |
| 41 | OP_TFORCALL | 0x29       |        |
| 42 | OP_TFORLOOP | 0x2A       |        |
| 43 | OP_SETLIST  | 0x2B       |        |
| 44 | OP_CLOSURE  | 0x2C       |        |
| 45 | OP_VARARG   | 0x2D       |        |
| 46 | OP_EXTRAARG | 0x2E       |        |

### Standard Library

...

## Usage

JDK 1.8+, Maven 3+ Required.

```shell
mvn test
mvn clean install
```

## Links

- [lua.go](https://github.com/zxh0/lua.go)
- [Lua 5.3 Reference Manual](http://www.lua.org/manual/5.3/manual.html)
- [Lua 5.3 Test suites](http://www.lua.org/tests/lua-5.3.4-tests.tar.gz)
- ...

