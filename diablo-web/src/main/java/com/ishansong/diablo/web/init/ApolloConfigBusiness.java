package com.ishansong.diablo.web.init;

import com.ctrip.framework.apollo.spring.annotation.ApolloJsonValue;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.ishansong.diablo.cache.UpstreamCacheManager;
import com.ishansong.diablo.core.model.rule.DivideHealthDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@EnableApolloConfig("business")
@Configuration
@Slf4j
public class ApolloConfigBusiness {

    @Value("${diablo.token.dubbo.timeout:1000}")
    private Integer tokenDubboTimeout;


    /**
     * {
     *     "134256709": {"healthStatus":true,"healthUri":"/busGateway/health","serviceName":"xxx-sys"}
     * }
     * @param divideHealthConfig
     */

    @ApolloJsonValue("${diablo.divide.health.config}")
    public void setDivideHealthConfig(Map<String, DivideHealthDto> divideHealthConfig) {
        UpstreamCacheManager.addDivideHealthMap(divideHealthConfig);
    }
}
