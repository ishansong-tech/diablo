package com.ishansong.diablo.admin.configuration;

import com.ishansong.diablo.admin.listener.http.HttpLongPollingDataChangedListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSyncConfiguration {

    @Bean
    public HttpLongPollingDataChangedListener dataChangedListener() {
        return new HttpLongPollingDataChangedListener();
    }

}
