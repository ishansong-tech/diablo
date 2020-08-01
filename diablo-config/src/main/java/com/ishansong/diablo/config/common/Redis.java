package com.ishansong.diablo.config.common;

import lombok.Data;

/**
 * Created by jiangmin on 2020/8/1.
 */
@Data
public class Redis {
    private String master;
    private String nodes;
    private String pwd;
}
