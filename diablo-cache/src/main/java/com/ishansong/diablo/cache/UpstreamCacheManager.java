package com.ishansong.diablo.cache;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ishansong.diablo.config.DiabloConfig;
import com.ishansong.diablo.core.concurrent.DiabloThreadFactory;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.model.rule.DivideHealthDto;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.DivideUpstream;
import com.ishansong.diablo.core.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "keepAliveLogger")
@Component
public class UpstreamCacheManager {

    private static final Map<String, List<DivideUpstream>> UPSTREAM_MAP = Maps.newConcurrentMap();

    private static final ConcurrentHashMap<String, List<DivideUpstream>> SCHEDULED_MAP = new ConcurrentHashMap<>();

    private static Map<String, DivideHealthDto> DIVIDEHEALTHMAP = new HashMap<>();

    public List<DivideUpstream> findUpstreamListByRuleId(final String ruleId) {
        return UPSTREAM_MAP.get(ruleId);
    }

    static void removeByKey(final String key) {
        SCHEDULED_MAP.remove(key);
        UPSTREAM_MAP.remove(key);
    }

    private Long upstreamKeepAliveScanTime;

    @Autowired
    private DiabloConfig diabloConfig;

    private static Boolean defaultHealthStatus = true;

    private static String defaultHealthUri = "/health";

    @PostConstruct
    public void init() {

        upstreamKeepAliveScanTime=this.diabloConfig.getWeb().getKeepAliveUpstream().getScanTimeMillisecond();
        new ScheduledThreadPoolExecutor(1, DiabloThreadFactory.create("scheduled-upstream-task", false))
                .scheduleWithFixedDelay(this::scheduled, upstreamKeepAliveScanTime, upstreamKeepAliveScanTime, TimeUnit.MILLISECONDS);
    }

    private void scheduled() {
        if (!SCHEDULED_MAP.isEmpty()) {
            SCHEDULED_MAP.entrySet().parallelStream().forEach(e ->
                    UPSTREAM_MAP.put(e.getKey(), detect(e.getValue()))
            );
        }
    }

    static void submit(final RuleData ruleData) {
        execute(ruleData);
    }

    static void clear() {
        SCHEDULED_MAP.clear();
        UPSTREAM_MAP.clear();
    }

    private static void execute(final RuleData ruleData) {

        final List<DivideUpstream> upstreamList =
                GsonUtils.getInstance().fromList(ruleData.getUpstreamHandle(), DivideUpstream.class);

        if (CollectionUtils.isNotEmpty(upstreamList)) {
            //补全服务健康检查信息
            upstreamListAddHealth(ruleData, DIVIDEHEALTHMAP, upstreamList);

            //规则不开启的，过滤掉
            if (ruleData.getEnabled()) {
                SCHEDULED_MAP.put(ruleData.getId(), upstreamList);
                UPSTREAM_MAP.put(ruleData.getId(), check(upstreamList));
            } else {
                SCHEDULED_MAP.put(ruleData.getId(), Lists.newArrayList());
                UPSTREAM_MAP.put(ruleData.getId(), Lists.newArrayList());
            }
        }
    }

    private static void upstreamListAddHealth(final RuleData ruleData, final Map<String, DivideHealthDto> divideHealthMap, final List<DivideUpstream> upstreamList) {
        for(DivideUpstream divideUpstream : upstreamList){
            String serviceInfoId = ruleData.getServiceInfoId();
            if(!Strings.isNullOrEmpty(serviceInfoId)){
                DivideHealthDto divideHealthDto = divideHealthMap.get(serviceInfoId.trim());
                if(Objects.nonNull(divideHealthDto)){
                    divideUpstream.setHealthStatus(divideHealthDto.isHealthStatus());
                    divideUpstream.setHealthUri(divideHealthDto.getHealthUri());
                    divideUpstream.setServiceName(divideHealthDto.getServiceName());
                    continue;
                }
            }
            divideUpstream.setHealthStatus(defaultHealthStatus);
            divideUpstream.setHealthUri(defaultHealthUri);
        }
    }

    private static RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());

    private static ClientHttpRequestFactory getClientHttpRequestFactory() {

        int timeout = 1000;
        RequestConfig requestConfig = RequestConfig.custom()
                                                   .setConnectTimeout(timeout)
                                                   .setConnectionRequestTimeout(timeout)
                                                   .setSocketTimeout(timeout)
                                                   .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(60);

        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create()
                                                                   .setKeepAliveStrategy((response, context) -> 5 * 1000)
                                                                   .setDefaultRequestConfig(requestConfig)
                                                                   .setConnectionManager(connectionManager)
                                                                   .build();

        return new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
    }

    private static boolean healthCheck(final DivideUpstream divideUpstream) {
        boolean check = false;
        String upstreamUrl = divideUpstream.getUpstreamUrl();
        String upstreamHost = divideUpstream.getUpstreamHost();

        if(!divideUpstream.isHealthStatus()){
            return Boolean.TRUE;
        }

        if (!upstreamUrl.startsWith("http")) {
            upstreamUrl = "http://" + upstreamUrl;
        }

        final String finalUpstreamUrl = upstreamUrl + divideUpstream.getHealthUri();
        for (int i = 0, retry = 0; i <= retry; i++) {

            if (retry > 0) {
                upstreamUrl += "_retry_" + i;
                upstreamHost += "_retry_" + i;
            }

            try {
                // other status throw exception
                restTemplate.headForHeaders(new URI(finalUpstreamUrl));

                check = true;
            } catch (Exception e) {
                try {
                    Thread.sleep(90);
                } catch (InterruptedException ex) {
                    // ingore
                }
                retry = 3;

                Transaction transaction = Cat.newTransaction(Constants.TRANSACTION_TYPE_KEEP_ALIVE, upstreamUrl);
                transaction.setStatus(e.getClass().getCanonicalName());
                Cat.logEvent(Constants.TRANSACTION_TYPE_KEEP_ALIVE, upstreamHost);

                Cat.logError(e);
                transaction.complete();

                log.warn("UpstreamCacheManager healthCheck the url: {}, host: {} is fail, cause: {}", finalUpstreamUrl, upstreamHost, Throwables.getStackTraceAsString(e));
            }
        }

        return check;
    }

    public static List<DivideUpstream> detect(List<DivideUpstream> upstreamList) {

        List<DivideUpstream> list = new ArrayList<>(upstreamList.size());

        upstreamList.parallelStream().forEach(u -> {
            final boolean pass = healthCheck(u);
            if (pass) {
                if (!u.isStatus()) {
                    u.setTimestamp(System.currentTimeMillis());
                    u.setStatus(true);

                    log.info("UpstreamCacheManager detect success the url: {}, host: {} ", u.getUpstreamUrl(), u.getUpstreamHost());
                }
                list.add(u);
            } else {
                u.setStatus(false);

                log.warn("UpstreamCacheManager detect failed the url: {}, host: {} ", u.getUpstreamUrl(), u.getUpstreamHost());
            }
        });

        return list;
    }

    private static List<DivideUpstream> check(final List<DivideUpstream> upstreamList) {
        List<DivideUpstream> resultList = Lists.newArrayListWithCapacity(upstreamList.size());
        for (DivideUpstream divideUpstream : upstreamList) {
            String upstreamUrl = divideUpstream.getUpstreamUrl();
            if (Strings.isNullOrEmpty(upstreamUrl)) {
                log.warn("UpstreamCacheManager config changed check warning the divideUpstream: {}", divideUpstream);

                continue;
            }

            //final boolean pass = healthCheck(upstreamUrl, divideUpstream.getUpstreamHost());
            final boolean pass = healthCheck(divideUpstream);
            if (pass) {
                resultList.add(divideUpstream);
            } else {
                log.error("UpstreamCacheManager config changed check failed the url: {}, host: {}", upstreamUrl, divideUpstream.getUpstreamHost());
            }
        }
        return resultList;
    }

    public static List<DivideUpstream> getUpstreamsByRuleId(String ruleId) {

        return UPSTREAM_MAP.get(ruleId);
    }

    public static List<DivideUpstream> addUpstreams(String ruleId, List<DivideUpstream> upstreams) {

        return UPSTREAM_MAP.put(ruleId, upstreams);
    }

    public static void addDivideHealthMap(final Map<String, DivideHealthDto> divideHealthMap) {
        DIVIDEHEALTHMAP = divideHealthMap;
        log.warn("addDivideHealthMap divideHealthMap info:{}",divideHealthMap);
    }
}
