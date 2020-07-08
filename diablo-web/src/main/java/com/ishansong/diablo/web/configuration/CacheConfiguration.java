package com.ishansong.diablo.web.configuration;

import com.ishansong.diablo.cache.HttpLongPollSyncCacheManager;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.config.DiabloConfig;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfigureBefore(DiabloConfiguration.class)
public class CacheConfiguration {

    @Configuration
    @ConditionalOnMissingBean(LocalCacheManager.class)
    @ConditionalOnProperty(name = "diablo.sync.strategy", havingValue = "http", matchIfMissing = true)
    static class HttpCacheManager {

        @ConfigurationProperties(prefix = "diablo.sync.http")
        @Bean
        public DiabloConfig.SyncCacheHttpConfig httpConfig() {
            return new DiabloConfig.SyncCacheHttpConfig();
        }

        @Bean
        public LocalCacheManager localCacheManager(final DiabloConfig.SyncCacheHttpConfig httpConfig) {
            return new HttpLongPollSyncCacheManager(httpConfig);
        }

    }
}
