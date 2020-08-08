package com.ishansong.diablo.web.init;

import com.google.common.base.Strings;
import com.ishansong.diablo.config.DiabloConfig;
import io.netty.util.ResourceLeakDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Slf4j
@Component
public class WebResourceConfigInit {

    @Autowired
    private DiabloConfig diabloConfig;

    @Autowired
    public WebResourceConfigInit() {

        ConfigTime.setFailbackDelay(1000);
    }

    @PostConstruct
    public void init() {

        resourceLeakDetection();
    }

    public void resourceLeakDetection() {
        String leakDetectionLevel = this.diabloConfig.getWeb().getLeakDetection().getLevel();

        if (Strings.isNullOrEmpty(leakDetectionLevel)) {
            return;
        }

        if (Objects.equals(leakDetectionLevel, ResourceLeakDetector.Level.ADVANCED.name())) {

            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);

            log.info("WebResourceConfigInit ADVANCED leakDetectionLevel:{}, advancedName:{}", leakDetectionLevel, ResourceLeakDetector.Level.ADVANCED.name());
        } else if (Objects.equals(leakDetectionLevel, ResourceLeakDetector.Level.SIMPLE.name())) {
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.SIMPLE);

            log.info("WebResourceConfigInit SIMPLE leakDetectionLevel:{}, advancedName:{}", leakDetectionLevel, ResourceLeakDetector.Level.SIMPLE.name());
        }
    }
}
