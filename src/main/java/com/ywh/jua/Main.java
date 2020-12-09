package com.ywh.jua;

import com.ywh.jua.state.LuaStateImpl;

import java.io.File;

/**
 * @author ywh
 * @since 2020/8/17 11:26
 */
public class Main {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {

        System.out.println("\n" +
            "             ,---._                              \n" +
            "           .-- -.' \\                             \n" +
            "           |    |   :                            \n" +
            "           |    |   |         ,--,               \n" +
            "           |    |   |       ,'_ /|               \n" +
            "           |    |   |  .--. |  | :    ,--.--.    \n" +
            "           |    |   |,'_ /| :  . |   /       \\   \n" +
            "           |    |   ||  ' | |  . .  .--.  .-. |  \n" +
            "       ___ |    |   ||  | ' |  | |   \\__\\/| . .  \n" +
            "     /    /\\    |   ||  | : ;  ; |   ,\" .--.; |  \n" +
            "    /  ../  `..-    ,|  |  `--'   \\ /  /  ,.  |  \n" +
            "    \\    \\         ; :  ,      .-./;  :  |..'  \\ \n" +
            "     \\    \\      ,'   `--`----'    |  ,     .-./ \n" +
            "      \"---....--'                   `--`---'     \n" +
            "            :: jua ::       (5.3.5-alpha)\n\n"
        );

        if (args.length <= 0 || !new File(args[0]).exists()) {
            throw new RuntimeException("file is not exist!");
        }

        LuaStateImpl ls = new LuaStateImpl();
        ls.openLibs();
        ls.loadFile(args[0]);
        ls.call(0, -1);
    }
}
