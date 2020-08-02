package com.ishansong.diablo.config.web;

import com.ishansong.diablo.config.common.Redis;
import lombok.Data;

/**
 * Created by jiangmin on 2020/8/1.
 */
@Data
public class Web {
    private KeepAliveUpstream keepAliveUpstream;
    private Disruptor disruptor;
    private AccessLog accessLog;
    private Redis redis;
    private Plugin plugin;
    private Sync sync;
}
