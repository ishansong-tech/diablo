package com.ishansong.diablo.admin.listener;

import java.io.Serializable;

public class ConfigDataCache implements Serializable {

    protected String group;

    private volatile String md5;

    private volatile long lastModifyTime;

    public ConfigDataCache(){

    }

    ConfigDataCache(String group, String md5, long lastModifyTime) {
        this.group = group;
        this.md5 = md5;
        this.lastModifyTime = lastModifyTime;
    }

    protected synchronized void update(String md5, long lastModifyTime) {
        this.md5 = md5;
        this.lastModifyTime = lastModifyTime;
    }

    public String getGroup() {
        return group;
    }

    public String getMd5() {
        return md5;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }
}
