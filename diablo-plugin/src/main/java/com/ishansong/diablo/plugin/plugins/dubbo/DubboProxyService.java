package com.ishansong.diablo.plugin.plugins.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.ishansong.diablo.core.exception.DiabloException;
import com.ishansong.diablo.core.model.dubbo.DubboExtConfig;
import com.ishansong.diablo.core.model.dubbo.DubboResourceStream;
import com.ishansong.diablo.core.model.selector.DubboSelectorHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DubboProxyService {

    public static final String DUBBO_LOADBALANCE_ROUNDROBIN = "roundrobin";
    private final Map<String, ApplicationConfig> APPLICATION_CONFIG_MAP = new ConcurrentHashMap<>();
    private final Map<String, ConsumerConfig> CONSUMER_CONFIG_MAP = new ConcurrentHashMap<>();

    private final Map<String, RegistryConfig> REGISTRY_CONFIG_MAP = new ConcurrentHashMap<>();

    private final List<String> DUBBO_LOADBALANCE_LIST = new ArrayList<String>() {{
        // org.apache.dubbo.rpc.cluster.loadbalance.RandomLoadBalance
        add("random");

        // org.apache.dubbo.rpc.cluster.loadbalance.RoundRobinLoadBalance
        add("roundrobin");

        // org.apache.dubbo.rpc.cluster.loadbalance.LeastActiveLoadBalance
        add("leastactive");

        // org.apache.dubbo.rpc.cluster.loadbalance.ConsistentHashLoadBalance
        add("consistenthash");
    }};

    private final Splitter DUBBO_REGISTRIES_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
    private static final String ZOOKEEPER_PREFIX = "zookeeper://";

    private final int coreSize = Runtime.getRuntime().availableProcessors() * 2;

    ConcurrentHashMap<String, ReferenceConfig<GenericService>> cache = new ConcurrentHashMap<>();

    public GenericService buildGenericService(final DubboResourceStream dubboMappingResource, final DubboSelectorHandle dubboSelectorHandle) {


        String appName = dubboSelectorHandle.getAppName();
        String referenceKey = generateKey(dubboMappingResource);

        referenceKey = appName + referenceKey;
        try {

            ReferenceConfig<GenericService> referenceConfig = cache.get(referenceKey);

            if (referenceConfig == null) {
                referenceConfig = buildReferenceConfig(dubboSelectorHandle, dubboMappingResource);
                cache.putIfAbsent(referenceKey, referenceConfig);
            }

            GenericService genericService = referenceConfig.get();

            if (Objects.isNull(genericService)) {
                destroyReference(referenceKey);

                throw new DiabloException("dubbo genericService has exception");
            }

            return genericService;
        } catch (NullPointerException e) {

            destroyReference(referenceKey);

            log.error("DubboProxyService genericInvoker configCache fail, namespace={}, method={}, cause={}", dubboMappingResource.getNamespace(), dubboMappingResource.getMethod(), Throwables.getStackTraceAsString(e));

            throw new DiabloException(e.getMessage());
        }

    }

    private ReferenceConfig<GenericService> buildReferenceConfig(DubboSelectorHandle selectorHandle, DubboResourceStream resource) {

        String appName = selectorHandle.getAppName();

        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();

        reference.setGeneric(true);
        reference.setApplication(APPLICATION_CONFIG_MAP.computeIfAbsent(appName, f -> new ApplicationConfig(appName)));

        String registry = selectorHandle.getRegistry();
        reference.setRegistry(REGISTRY_CONFIG_MAP.computeIfAbsent(registry,
                f -> Optional.of(registry)
                             .map(DUBBO_REGISTRIES_SPLITTER::split)
                             .map(Lists::newArrayList)
                             .map(l -> {
                                 if (CollectionUtils.isEmpty(l)) {
                                     return null;
                                 }

                                 StringBuilder builder = new StringBuilder();
                                 if (l.size() == 1) {
                                     builder.append(ZOOKEEPER_PREFIX).append(l.get(0));
                                 } else if (l.size() > 1) {
                                     for (int i = 0; i < l.size(); i++) {
                                         if (i == 0) {
                                             builder.append(ZOOKEEPER_PREFIX).append(l.get(i)).append("?backup=");
                                         } else {
                                             builder.append(l.get(i)).append(",");
                                         }
                                     }
                                     builder.setLength(builder.length() - 1);
                                 }

                                 return new RegistryConfig(builder.toString());
                             }).get()));

        reference.setInterface(resource.getNamespace());
        Optional<DubboExtConfig> configOptional = Optional.ofNullable(resource.getDubboExtConfig());

        configOptional.map(DubboExtConfig::getTimeout).ifPresent(reference::setTimeout);
        configOptional.map(DubboExtConfig::getRetries).ifPresent(reference::setRetries);

        // 只调用指定协议的服务提供方，其它协议忽略。多个协议ID用逗号分隔
        Optional.of(selectorHandle).map(DubboSelectorHandle::getProtocol).ifPresent(s -> {
            if (!Strings.isNullOrEmpty(s)) {
                reference.setProtocol(s);
            } else {
                reference.setProtocol("dubbo");
            }
        });

        configOptional.map(DubboExtConfig::getVersion).ifPresent(reference::setVersion);

        configOptional.map(DubboExtConfig::getGroup).ifPresent(reference::setGroup);

        // random,roundrobin,leastactive，分别表示：随机，轮询，最少活跃调用  不用ruleHandler的loadBalance名称有大写
        configOptional.map(DubboExtConfig::getLoadbalance)
                      .map(lb -> DUBBO_LOADBALANCE_LIST.contains(lb) ? lb : DUBBO_LOADBALANCE_ROUNDROBIN)
                      .ifPresent(reference::setLoadbalance);

        // 默认false 是否缺省异步执行，不可靠异步，只是忽略返回值，不阻塞执行线程 TODO 测试是否需要
        configOptional.map(DubboExtConfig::getAsync).ifPresent(reference::setAsync);

        // TODO 提供者不可用状态注册问题
        // configOptional.map(DubboExtConfig::getCheck).ifPresent(reference::setCheck);
        reference.setCheck(false);

        //  TODO 头部参数获取不在接口定义 测试灰度 invoke重写
        configOptional.map(DubboExtConfig::getLayer).ifPresent(reference::setLayer);

        reference.setConsumer(CONSUMER_CONFIG_MAP.computeIfAbsent(appName, f -> {

            ConsumerConfig consumerConfig = new ConsumerConfig();
            consumerConfig.setThreadpool("cached");
            consumerConfig.setCorethreads(0);
            consumerConfig.setQueues(0);

            consumerConfig.setThreads(200);
            consumerConfig.setClient("netty4");

            return consumerConfig;
        }));

        // 线程池共享一个连接
        reference.setConnections(0);

        return reference;
    }

    private String generateKey(DubboResourceStream dubboMappingResource) {

        DubboExtConfig dubboExtConfig = dubboMappingResource.getDubboExtConfig();
        if (dubboExtConfig == null) {
            return dubboMappingResource.getNamespace();
        }
        // 一个接口重载方法初始定义应该为全异步或非异步, 不加异步标签了 dubbo确认唯一reference(interface+group+version)
        return dubboMappingResource.getNamespace() + dubboExtConfig.getGroup() + dubboExtConfig.getVersion();
    }

    private void destroyReference(String referenceKey) {

        ReferenceConfig<GenericService> referenceConfig = cache.remove(referenceKey);
        if (referenceConfig != null) {

            referenceConfig.destroy();
        }
    }

}
