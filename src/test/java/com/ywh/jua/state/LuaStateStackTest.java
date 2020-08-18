package com.ywh.jua.state;

import com.ywh.jua.api.LuaState;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author ywh
 * @since 2020/8/18 11:26
 */
public class LuaStateStackTest {

    private LuaState ls;

    private String lsToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= ls.getTop(); i++) {
            sb.append(ls.toInteger(i));
        }
        return sb.toString();
    }

    @Before
    public void initLuaState() {
        ls = new LuaStateImpl();
        for (int i = 1; i < 10; i++) {
            ls.pushInteger(i);
        }
        assertEquals("123456789", lsToString());
    }

    @Test
    public void stack() {

        //       +--------------+
        //       ↓              |
        // 1, 2, 3, 4, 5, 6, 7, 8, 9
        ls.copy(8, 3);
        assertEquals("128456789", lsToString());

        //             +--------------+
        //             |              ↓
        // 1, 2, 8, 4, 5, 6, 7, 8, 9,
        ls.pushValue(5);
        assertEquals("1284567895", lsToString());

        // +---------------------------+
        // ↓                           |
        // 1, 2, 8, 4, 5, 6, 7, 8, 9, [5]
        ls.replace(1);
        assertEquals("528456789", lsToString());

        //   -----------------------+
        //   ↓                      |
        // 5, 2, 8, 4, 5, 6, 7, 8, [9]
        ls.insert(2);
        assertEquals("592845678", lsToString());

        //          +--------------+
        //          ↓  →  →  →  →  |
        // 5, 9, 2, 8, 4, 5, 6, 7, 8
        ls.rotate(5, 1);
        assertEquals("592884567", lsToString());

        //                      ↑  ↑
        //                      |  |
        // 5, 9, 2, 8, 8, 4, 5, 6, 7
        ls.pop(2);
        assertEquals("5928845", lsToString());

        //            -┐  ↑  ↑
        //             |  |  |
        // 5, 9, 2, 8, 8, 4, 5
        ls.setTop(5);
        assertEquals("59288", lsToString());
    }

}
