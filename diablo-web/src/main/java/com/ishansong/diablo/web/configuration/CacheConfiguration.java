package com.ishansong.diablo.web.configuration;

import com.ishansong.diablo.cache.HttpLongPollSyncCacheManager;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.config.DiabloConfig;
import com.ishansong.diablo.config.web.SyncCacheHttpConfig;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfigureBefore(DiabloConfiguration.class)
public class CacheConfiguration {

    @Bean
    public LocalCacheManager localCacheManager(final DiabloConfig diabloConfig) {
        SyncCacheHttpConfig syncCacheHttpConfig=new SyncCacheHttpConfig();
        syncCacheHttpConfig.setUrl(diabloConfig.getWeb().getSync().getUrl());
        return new HttpLongPollSyncCacheManager(syncCacheHttpConfig);
    }

}
