package com.ishansong.diablo.web.init;

public class ConfigTime {

    private static long failbackDelay;

    public static long getFailbackDelay() {
        return failbackDelay;
    }

    public static void setFailbackDelay(long failbackDelay) {
        ConfigTime.failbackDelay = failbackDelay;
    }
}
