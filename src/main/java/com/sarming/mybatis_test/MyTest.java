package com.sarming.mybatis_test;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MyTest {
    @Test
    public void testDevide() {
        int a = 101;
        int b = a >> 1;
        System.out.println(b);

        int c = a / 2;
        System.out.println(c);
    }


    @Test
    public void testFinal() {
        int a = 12;
        int b = 13;
        int all = getCount(a);
        Map<String,Integer> map = Collections.unmodifiableMap(new HashMap<>());
        map.put("001",456);

        int all2 = getCount2(map);
        System.err.println(all);
        System.err.println(all2);
    }

    private int getCount2(final Map<String, Integer> map) {
        map.put("002",400);

        return map.get("002")+10;
    }

    private int getCount(final int a) {

        return a + 20;
    }


    @Test
    public void insert() {

    }
}
