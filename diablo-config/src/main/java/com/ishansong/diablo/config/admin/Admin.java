package com.ishansong.diablo.config.admin;

import com.ishansong.diablo.config.common.Apollo;
import com.ishansong.diablo.config.common.Redis;
import com.ishansong.diablo.config.common.Zookeeper;
import lombok.Data;

@Data
public class Admin{
    private SyncUpstream syncUpstream;
    private Apollo apollo;
    private String env;
    private String domain;
    private Zookeeper zookeeper;
    private Redis redis;
}
