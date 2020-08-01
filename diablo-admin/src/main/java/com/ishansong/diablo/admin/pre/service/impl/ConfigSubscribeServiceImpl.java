package com.ishansong.diablo.admin.pre.service.impl;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.ishansong.diablo.admin.listener.DataChangedEvent;
import com.ishansong.diablo.admin.pre.service.ConfigSubscribeService;
import com.ishansong.diablo.config.DiabloConfig;
import com.ishansong.diablo.core.concurrent.DiabloThreadFactory;
import com.ishansong.diablo.core.constant.AdminConstants;
import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.enums.DataEventTypeEnum;
import com.ishansong.diablo.core.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ConfigSubscribeServiceImpl implements ConfigSubscribeService {

    private CuratorFramework client;

    private ExecutorService executor = Executors.newFixedThreadPool(1, DiabloThreadFactory.create("zookeper_listenable", true));

    private final static String ZK_PATH = "/config/gateway/router";

    @Autowired(required = false)
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public ConfigSubscribeServiceImpl(DiabloConfig diabloConfig) {

        try {
            String zookeeperHost=diabloConfig.getAdmin().getZookeeper().getHost();
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                                                                             .connectString(zookeeperHost)
                                                                             .retryPolicy(new RetryNTimes(1, 1000))
                                                                             .connectionTimeoutMs(5000).sessionTimeoutMs(5000).namespace("middleware");

            client = builder.build();

            client.start();

            boolean connected = client.blockUntilConnected(5000, TimeUnit.MILLISECONDS);
            if (!connected) {
                log.warn("ConfigSubscribeService client not connected");
            }

            if (client.checkExists().forPath(ZK_PATH) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZK_PATH);
            }

            final NodeCache cache = new NodeCache(client, ZK_PATH, false);
            cache.getListenable().addListener(() -> {

                String message = Optional.ofNullable(cache.getCurrentData()).map(ChildData::getData).map(String::new).orElse("");
                String path = Optional.ofNullable(cache.getCurrentData()).map(ChildData::getPath).orElse("");

                if (!Strings.isNullOrEmpty(message)) {

                    Map<String, Object> map = GsonUtils.getInstance().toObjectMap(message);

                    Long timestamp = MapUtils.getLong(map, AdminConstants.NOTICE_MESSAGE_TIMESTAMP);

                    eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.RULE, DataEventTypeEnum.UPDATE, timestamp, Collections.emptyList()));

                    eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.SELECTOR, DataEventTypeEnum.UPDATE, timestamp, Collections.emptyList()));

                    log.info("ConfigSubscribeService notification eventPath: {}, eventData: {}", path, message);
                }
            }, executor);

            cache.start();
        } catch (Exception e) {

            log.error("ConfigSubscribeService client failed, cause: {}", Throwables.getStackTraceAsString(e));
        }

    }

    @Override
    public void notifyChanged(String message) {

        try {
            if (client.checkExists().forPath(ZK_PATH) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZK_PATH);
            }

            client.setData().inBackground().forPath(ZK_PATH, message.getBytes());
        } catch (Exception e) {
            log.error("ConfigSubscribeService write data failed message: {}, cause: {}", message, Throwables.getStackTraceAsString(e));
        }
    }

}
