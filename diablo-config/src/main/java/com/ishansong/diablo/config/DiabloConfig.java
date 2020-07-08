package com.ishansong.diablo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component("diabloConfig")
@ConfigurationProperties(prefix = "diablo",
        ignoreInvalidFields = true)
public class DiabloConfig implements Serializable {

    @Data
    public static class SyncCacheHttpConfig {

        private String url;

        private Integer delayTime;

        private Integer connectionTimeout;

    }

    @Data
    public static class NettyConfig {

        private int serverSocketSndBufSize = 1024;
        private int serverSocketRcvBufSize = 1024;
        private int serverSelectorThreads = DEFAULT_EVENT_LOOP_THREADS *2;
        private int serverBossThreads=DEFAULT_EVENT_LOOP_THREADS;
        private int serverSocketBacklog=1000;
        private static final int DEFAULT_EVENT_LOOP_THREADS = Runtime.getRuntime().availableProcessors();

    }

}
