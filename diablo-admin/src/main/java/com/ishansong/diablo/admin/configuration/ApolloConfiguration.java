package com.ishansong.diablo.admin.configuration;

import com.ishansong.diablo.admin.config.DiabloAdminConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApolloConfiguration {

    @Bean
    public DiabloAdminConfig.AdminSyncUpstreamConfig adminSyncUpstream() {
        return new DiabloAdminConfig.AdminSyncUpstreamConfig();
    }
}
