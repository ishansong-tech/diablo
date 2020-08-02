package com.ishansong.diablo.config.web;

import lombok.Data;

/**
 * Created by jiangmin on 2020/8/2.
 */
@Data
public class SyncCacheHttpConfig {

    private String url;

    private Integer delayTime;

    private Integer connectionTimeout;

}
