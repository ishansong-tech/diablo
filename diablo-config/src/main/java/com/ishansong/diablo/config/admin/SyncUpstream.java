package com.ishansong.diablo.config.admin;

import lombok.Data;

/**
 * 后台管理配置：主机同步
 */
@Data
public class SyncUpstream{
    /***
     * 是否自动从云效同步服务主机信息
     */
    private boolean autoSync=false;
    /***
     * 当主机信息有差异时，是否发钉钉报警
     */
    private boolean sendWarnMessage=false;
}