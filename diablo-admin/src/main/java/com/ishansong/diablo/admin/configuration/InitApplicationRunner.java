package com.ishansong.diablo.admin.configuration;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.ishansong.diablo.admin.listener.CacheService;
import com.ishansong.diablo.admin.utils.PreEnv;
import com.ishansong.diablo.admin.utils.ProdEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class InitApplicationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitApplicationRunner.class);

    private final CacheService cacheService;

    @Autowired
    public InitApplicationRunner(@Value("${apollo.cluster:}") String apolloCluster,
                                 @Value("${env:}") String env,
                                 CacheService cacheService) {
        this.cacheService = cacheService;

        PreEnv.setPre(!Strings.isNullOrEmpty(apolloCluster));
        ProdEnv.setProd(!Strings.isNullOrEmpty(env) && Objects.equal("pro",env.toLowerCase()));
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        log.info("InitApplicationRunner start:{}", new Date());
        this.cacheService.updateSelectorCache();
        this.cacheService.updatePluginCache();
        this.cacheService.updateRuleCache();
        this.cacheService.updateDubboResourceCache();
        log.info("InitApplicationRunner end:{}", new Date());
    }

}
