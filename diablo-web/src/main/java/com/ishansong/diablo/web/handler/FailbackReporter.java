package com.ishansong.diablo.web.handler;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.cache.UpstreamCacheManager;
import com.ishansong.diablo.core.concurrent.DiabloThreadFactory;
import com.ishansong.diablo.core.model.rule.FailbackData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.DivideUpstream;
import com.ishansong.diablo.web.init.ConfigTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@DependsOn({"apolloConfigInit"})
@Component
public class FailbackReporter {

    private final Map<String, List<DivideUpstream>> FAIL_BACK_MAP = new ConcurrentHashMap<>();

    private final EmitterProcessor<FailbackData> processor = EmitterProcessor.create();

    private final int REPORT_RULE_COUNTER = 3;

    @Autowired
    private LocalCacheManager localCacheManager;

    @PostConstruct
    public void init() {

        // 异常主机优先探活
        new ScheduledThreadPoolExecutor(1, DiabloThreadFactory.create("scheduled-failback-upstream-task", false))
                .scheduleWithFixedDelay(this::execute, ConfigTime.getFailbackDelay(), ConfigTime.getFailbackDelay(), TimeUnit.MILLISECONDS);
        // 确认 时间窗口和异常数
        Flux<ArrayList<FailbackData>> flux = processor.windowTimeout(1, Duration.ofMillis(1000)).flatMap(w -> w.reduce(new ArrayList<>(), (l, e) -> {
            l.add(e);
            return l;
        }));

        flux.subscribe(l -> {
            if (CollectionUtils.isEmpty(l)) {
                return;
            }

            Map<String, List<FailbackData>> ruleMap = l.stream().collect(Collectors.groupingBy(FailbackData::getRuleId));

            ruleMap.forEach((k, v) -> {

                if (CollectionUtils.isEmpty(v)) {
                    return;
                }

                // 确认 预设值
                if (v.size() < REPORT_RULE_COUNTER) {
                    return;
                }

                try {
                    List<DivideUpstream> divideUpstreams = UpstreamCacheManager.getUpstreamsByRuleId(k);

                    List<String> hosts = v.stream().map(FailbackData::getUpstreamHost).collect(Collectors.toList());
                    List<DivideUpstream> failback = divideUpstreams.stream().filter(d -> hosts.contains(d.getUpstreamHost())).collect(Collectors.toList());

                    if (CollectionUtils.isEmpty(failback)) {
                        return;
                    }

                    // 原主机列表移除失败主机
                    // if (failback.size() < divideUpstreams.size())
                    divideUpstreams.removeAll(failback);

                    FAIL_BACK_MAP.put(k, failback);

                    log.info("FailbackReporter subscribe remove failbacks, ruleId:{}, divideUpstreamsSize:{}, failbackDataSize:{}, failback:{}", k, divideUpstreams.size(), v.size(), failback);
                } catch (Exception ex) {
                    log.error("FailbackReporter subscribe failed, ruleId:{}, failbackDatas:{}, cause:{}", k, v, Throwables.getStackTraceAsString(ex));
                }
            });
        });

        log.info("FailbackReporter scheduleWithFixedDelay, delay:{}", ConfigTime.getFailbackDelay());
    }

    void execute() {

        if (FAIL_BACK_MAP.isEmpty()) {
            return;
        }

        Set<String> updatedBackList = new HashSet<>();

        FAIL_BACK_MAP.forEach((k, v) -> {

            log.warn("FailbackReporter rule begin to reconnect,ruleId:{}",k);

            if (CollectionUtils.isEmpty(v)) {

                return;
            }
            //验证规则有效性，比如服务已经下线，或者规则已关闭就不需要继续检查了
            RuleData ruleData=this.localCacheManager.findRuleByRuleId(k);
            if(null==ruleData){
                log.warn("FailbackReporter rule is null,ruleId:{}",k);
                FAIL_BACK_MAP.remove(k);
                return;
            }
            if(!ruleData.getEnabled()){
                log.warn("FailbackReporter rule unable,ruleId:{}",k);
                FAIL_BACK_MAP.remove(k);
                return;
            }

            // 上报的主机连接重新探活
            List<DivideUpstream> healthUpstreams = UpstreamCacheManager.detect(v);
            if (CollectionUtils.isEmpty(healthUpstreams)) {

                return;
            }

            List<DivideUpstream> divideUpstreams = UpstreamCacheManager.getUpstreamsByRuleId(k);

            // divideUpstreams contains other host
            Set<String> hosts = healthUpstreams.stream().map(DivideUpstream::getUpstreamHost).collect(Collectors.toSet());
            List<DivideUpstream> upstreams = divideUpstreams.stream().filter(d -> !hosts.contains(d.getUpstreamHost())).collect(Collectors.toList());

            // 其他正常主机+上报的正常主机
            upstreams.addAll(healthUpstreams);

            if (!CollectionUtils.isEmpty(upstreams)) {
                UpstreamCacheManager.addUpstreams(k, upstreams);

                log.info("FailbackReporter execute addUpstreams, ruleId:{}, divideUpstreamsSize:{}, otherUpstreamsSize:{}, detectSize:{}, healthUpstreams:{}", k, divideUpstreams.size(), upstreams.size(), v.size(), healthUpstreams);
            } else {
                UpstreamCacheManager.addUpstreams(k, healthUpstreams);

                log.warn("FailbackReporter execute addUpstreams otherUpstream is empty, ruleId:{}, divideUpstreamsSize:{}, otherUpstreamsSize:{}, detectSize:{}, healthUpstreams:{}", k, divideUpstreams.size(), upstreams.size(), v.size(), healthUpstreams);
            }

            // 更新上报主机列表, 异常主机列表去除健康主机
            v.removeAll(healthUpstreams);

            if (CollectionUtils.isEmpty(v)) {
                updatedBackList.add(k);
            }
        });

        updatedBackList.forEach(FAIL_BACK_MAP::remove);
    }

    public void report(FailbackData data) {

        if (data == null) {
            return;
        }

        if (Strings.isNullOrEmpty(data.getRuleId()) || Strings.isNullOrEmpty(data.getUpstreamHost())) {
            return;
        }

        processor.onNext(data);
    }

}
