package com.ishansong.diablo.admin.utils;

public class PreEnv {

    private static boolean pre;

    public static boolean isPre() {
        return pre;
    }

    public static void setPre(boolean pre) {
        PreEnv.pre = pre;
    }
}
