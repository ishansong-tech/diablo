package com.ishansong.diablo.admin.utils;

public class ProdEnv {

    private static boolean prod;

    public static boolean isProd() {
        return prod;
    }

    public static void setProd(boolean prod) {
        ProdEnv.prod = prod;
    }
}
