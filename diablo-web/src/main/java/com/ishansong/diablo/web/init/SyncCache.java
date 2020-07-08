package com.ishansong.diablo.web.init;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.SerialPolicy;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.config.DiabloConfig;
import com.ishansong.diablo.core.constant.HttpConstants;
import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.exception.DiabloException;
import com.ishansong.diablo.core.model.ConfigData;
import com.ishansong.diablo.core.model.dubbo.DubboResourceData;
import com.ishansong.diablo.core.model.plugin.PluginData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import com.ishansong.diablo.web.concurrent.DiabloThreadFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SyncCache implements CommandLineRunner, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncCache.class);

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    private static final ConcurrentMap<ConfigGroupEnum, ConfigData> GROUP_CACHE = new ConcurrentHashMap<>();

    private static final Gson GSON = new Gson();

    private Duration connectionTimeout = Duration.ofSeconds(10);

    private RestTemplate httpClient;

    private ExecutorService executor;

    private DiabloConfig.SyncCacheHttpConfig httpConfig;

    private List<String> serverList;

    @CreateCache(expire = 24 * 60 * 60, name = "dubboResource:", cacheType = CacheType.REMOTE, serialPolicy = SerialPolicy.KRYO)
    private Cache<String, List<DubboResourceData>> cacheDubboResourceData;

    @Autowired
    private LocalCacheManager localCacheManager;

    @Override
    public void run(final String... args) {

        long start = System.currentTimeMillis();

        this.httpConfig = this.localCacheManager.httpConfig();
        serverList = Splitter.on(",").splitToList(this.httpConfig.getUrl());

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) this.connectionTimeout.toMillis());
        factory.setReadTimeout((int) HttpConstants.CLIENT_POLLING_READ_TIMEOUT);
        this.httpClient = new RestTemplate(factory);

        if (RUNNING.compareAndSet(false, true)) {

            this.fetchGroupConfig(ConfigGroupEnum.values());

            this.executor = new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(),
                    DiabloThreadFactory.create("http-long-polling", true));

            this.executor.execute(new HttpLongPollingTask());
        } else {
            LOGGER.info("SyncCache http long polling was started, executor=[{}]", executor);
        }

        LOGGER.info("SyncCache run http long polling was started, serverList: {}, elasedTime: {}", serverList, System.currentTimeMillis() - start);
    }

    @Override
    public void destroy() {
        RUNNING.set(false);
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private void fetchGroupConfig(final ConfigGroupEnum... groups) throws DiabloException {
        StringBuilder params = new StringBuilder();
        for (ConfigGroupEnum groupKey : groups) {
            params.append("groupKeys")
                  .append("=")
                  .append(groupKey.name())
                  .append("&");
        }

        DiabloException ex = null;
        for (String server : serverList) {
            String url = server + "/configs/fetch?" + StringUtils.removeEnd(params.toString(), "&");

            try {
                String json = this.httpClient.getForObject(url, String.class);
                updateCacheWithJson(json);

                LOGGER.info("configs fetch request configs: [{}]", url);
                return;
            } catch (Exception e) {
                LOGGER.warn("configs fetch request configs fail, server:[{}]", server);
                ex = new DiabloException("Init cache error, serverList:" + httpConfig.getUrl(), e);
                // try next server, if have another one.
            }
        }

        if (ex != null) {
            throw ex;
        }
    }

    private void updateCacheWithJson(final String json) {

        JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);
        JsonObject data = jsonObject.getAsJsonObject("data");

        // plugin
        JsonObject configData = data.getAsJsonObject(ConfigGroupEnum.PLUGIN.name());
        if (configData != null) {
            ConfigData<PluginData> result = GSON.fromJson(configData, new TypeToken<ConfigData<PluginData>>() {
            }.getType());
            GROUP_CACHE.put(ConfigGroupEnum.PLUGIN, result);
            this.localCacheManager.flushAllPlugin(result.getData());
        }

        // rule
        configData = data.getAsJsonObject(ConfigGroupEnum.RULE.name());
        if (configData != null) {
            ConfigData<RuleData> result = GSON.fromJson(configData, new TypeToken<ConfigData<RuleData>>() {
            }.getType());
            GROUP_CACHE.put(ConfigGroupEnum.RULE, result);
            this.localCacheManager.flushAllRule(result.getData());
        }

        // selector
        configData = data.getAsJsonObject(ConfigGroupEnum.SELECTOR.name());
        if (configData != null) {
            ConfigData<SelectorData> result = GSON.fromJson(configData, new TypeToken<ConfigData<SelectorData>>() {
            }.getType());
            GROUP_CACHE.put(ConfigGroupEnum.SELECTOR, result);
            this.localCacheManager.flushAllSelector(result.getData());
        }

        configData = data.getAsJsonObject(ConfigGroupEnum.DUBBO_MAPPING.name());
        if (configData != null) {

            ConfigData<DubboResourceData> result = GSON.fromJson(configData, new TypeToken<ConfigData<DubboResourceData>>() {
            }.getType());

            GROUP_CACHE.put(ConfigGroupEnum.DUBBO_MAPPING, result);

            List<DubboResourceData> dubboResourceData = cacheDubboResourceData.get(ConfigGroupEnum.DUBBO_MAPPING.name());
            if (!CollectionUtils.isEmpty(dubboResourceData)) {

                this.localCacheManager.flushAllDubboMapping(dubboResourceData);
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void doLongPolling() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>(16);
        for (ConfigGroupEnum group : ConfigGroupEnum.values()) {
            ConfigData<?> cacheConfig = GROUP_CACHE.get(group);
            String value = String.join(",", cacheConfig.getMd5(), String.valueOf(cacheConfig.getLastModifyTime()));
            params.put(group.name(), Lists.newArrayList(value));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity httpEntity = new HttpEntity(params, headers);
        for (String server : serverList) {
            String listenerUrl = server + "/configs/listener";
            try {
                String json = this.httpClient.postForEntity(listenerUrl, httpEntity, String.class).getBody();
                JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);
                int code = jsonObject.get("code").getAsInt();
                if (code == 200) {
                    LOGGER.info("postForEntity listener result, listenerUrl:{}, params:{}, json:{}", listenerUrl, params, json);

                    JsonArray groupJson = jsonObject.get("data").getAsJsonArray();
                    // fetch group configuration async.
                    ConfigGroupEnum[] changedGroups = GSON.fromJson(groupJson, ConfigGroupEnum[].class);
                    if (ArrayUtils.isNotEmpty(changedGroups)) {
                        executor.execute(() -> fetchGroupConfig(changedGroups));
                    }
                } else {
                    LOGGER.warn("configs listener waring result: [{}]", json);
                }
                break;
            } catch (RestClientException e) {
                LOGGER.error("listener configs fail, can not connection this server:[{}]", listenerUrl);

                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    class HttpLongPollingTask implements Runnable {
        @Override
        public void run() {
            while (RUNNING.get()) {
                try {
                    doLongPolling();
                } catch (Exception e) {
                    LOGGER.error("HttpLongPollingTask doLongPolling run, cause:{}", Throwables.getStackTraceAsString(e));
                }
            }
            LOGGER.warn("Stop http long polling.");
        }
    }


}
