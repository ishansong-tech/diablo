package com.ishansong.diablo.web.init;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.google.common.base.Strings;
import io.netty.util.ResourceLeakDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Slf4j
@Component
public class ApolloConfigInit {

    @ApolloConfig
    private Config applicationConfig;

    @Autowired
    public ApolloConfigInit(@Value("${diablo.failback.delay:200}") long diabloFailbackDelay) {

        ConfigTime.setFailbackDelay(diabloFailbackDelay);
    }

    @PostConstruct
    public void init() {

        resourceLeakDetection();
    }

    private final static String leakDetectionKey = "diablo.leakDetection.level";

    @ApolloConfigChangeListener
    public void leakDetection(ConfigChangeEvent event) {

        if (event.isChanged(leakDetectionKey)) {
            resourceLeakDetection();
        }
    }

    public void resourceLeakDetection() {
        String leakDetectionLevel = applicationConfig.getProperty(leakDetectionKey, "");

        if (Strings.isNullOrEmpty(leakDetectionLevel)) {
            return;
        }

        if (Objects.equals(leakDetectionLevel, ResourceLeakDetector.Level.ADVANCED.name())) {

            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);

            log.info("ApolloConfigInit apollo change listener ADVANCED leakDetectionLevel:{}, advancedName:{}", leakDetectionLevel, ResourceLeakDetector.Level.ADVANCED.name());
        } else if (Objects.equals(leakDetectionLevel, ResourceLeakDetector.Level.SIMPLE.name())) {
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.SIMPLE);

            log.info("ApolloConfigInit apollo change listener SIMPLE leakDetectionLevel:{}, advancedName:{}", leakDetectionLevel, ResourceLeakDetector.Level.SIMPLE.name());
        }
    }
}
