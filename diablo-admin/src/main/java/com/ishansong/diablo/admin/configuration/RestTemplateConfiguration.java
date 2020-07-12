package com.ishansong.diablo.admin.configuration;

import com.ishansong.diablo.admin.utils.RestTemplateUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate syncRestTemplate() {

        return RestTemplateUtil.getRestTemplate(
                3000, 300);
    }


}
