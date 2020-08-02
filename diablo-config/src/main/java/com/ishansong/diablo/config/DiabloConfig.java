package com.ishansong.diablo.config;

import com.ishansong.diablo.config.admin.Admin;
import com.ishansong.diablo.config.web.Web;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Configuration
@Component("diabloConfig")
@ConfigurationProperties(prefix = "diablo",ignoreInvalidFields = true)
public class DiabloConfig implements Serializable {

    /**
     * 后台管理配置
     */
    private Admin admin;

    /**
     * 网关配置
     */
    private Web web;

}
