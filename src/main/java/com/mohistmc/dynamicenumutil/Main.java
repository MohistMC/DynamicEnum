package com.mohistmc.dynamicenumutil;

import java.util.Arrays;

public class Main {

    public static void main(String[] a) {
        MohistEnumHelper.addEnum0(Test.class, "d", new Class[0]);
        System.out.println(Arrays.toString(Test.values()));
    }
}
