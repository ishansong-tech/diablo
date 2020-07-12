package com.ishansong.diablo.admin.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component("diabloConfig")
@ConfigurationProperties(prefix = "diablo",
        ignoreInvalidFields = true)
public class DiabloAdminConfig implements Serializable {

    @Data
    public static class AdminSyncUpstreamConfig {

        /***
         * 是否自动从云效同步服务主机信息
         */
        @Value("${diablo.adminSyncUpstream.autoSync:false}")
        private boolean autoSync=false;

        /***
         * 当主机信息有差异时，是否发钉钉报警
         */
        @Value("${diablo.adminSyncUpstream.isSendWarnMesseage:false}")
        private boolean isSendWarnMesseage=false;

    }

}
