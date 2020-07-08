package com.ishansong.diablo.cache;

import com.google.common.base.Strings;
import com.ishansong.diablo.config.DiabloConfig;
import com.ishansong.diablo.core.enums.PluginEnum;
import com.ishansong.diablo.core.model.dubbo.DubboResourceData;
import com.ishansong.diablo.core.model.dubbo.DubboResourceStream;
import com.ishansong.diablo.core.model.dubbo.ParamMetasData;
import com.ishansong.diablo.core.model.plugin.PluginData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class HttpLongPollSyncCacheManager extends AbstractLocalCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLongPollSyncCacheManager.class);

    private DiabloConfig.SyncCacheHttpConfig httpConfig;

    public HttpLongPollSyncCacheManager(DiabloConfig.SyncCacheHttpConfig httpConfig) {
        this.httpConfig = httpConfig;
    }

    private DubboResourceStream mapToDubboResource(DubboResourceData dubboResourceData) {
        if ( dubboResourceData == null ) {
            return null;
        }

        DubboResourceStream dubboResourceStream = new DubboResourceStream();

        dubboResourceStream.setParamObjectType( dubboResourceData.getObjectType() );
        dubboResourceStream.setServiceName( dubboResourceData.getServiceName() );
        dubboResourceStream.setNamespace( dubboResourceData.getNamespace() );
        dubboResourceStream.setMethod( dubboResourceData.getMethod() );
        dubboResourceStream.setDubboExtConfig( dubboResourceData.getDubboExtConfig() );
        dubboResourceStream.setApiConfig( dubboResourceData.getApiConfig() );

        return dubboResourceStream;
    }

    public void flushAllPlugin(final List<PluginData> pluginDataList) {
        if (CollectionUtils.isEmpty(pluginDataList)) {
            LOGGER.info("clear all plugin cache, old cache:{}", PLUGIN_MAP);
            PLUGIN_MAP.clear();
        } else {
            PLUGIN_MAP.clear();
            //configPlugin(pluginDataList);
            pluginDataList.forEach(pluginData -> PLUGIN_MAP.put(pluginData.getName(), pluginData));
        }
    }

    public void flushAllSelector(final List<SelectorData> selectorDataList) {
        if (CollectionUtils.isEmpty(selectorDataList)) {
            LOGGER.info("clear all selector cache, old cache:{}", SELECTOR_MAP);
            SELECTOR_MAP.clear();
        } else {
            Map<String, List<SelectorData>> pluginNameToSelectors = selectorDataList.stream()
                                                                                    .filter(Objects::nonNull)
                                                                                    .collect(Collectors.groupingBy(SelectorData::getPluginName,
                                                                                            Collectors.toCollection(ArrayList::new)));

            pluginNameToSelectors.keySet().forEach(pluginName -> {
                List<SelectorData> sorted = pluginNameToSelectors.get(pluginName).stream()
                                                                 .sorted(Comparator.comparing(SelectorData::getSort)).collect(Collectors.toList());
                pluginNameToSelectors.put(pluginName, sorted);
            });
            SELECTOR_MAP.clear();
            SELECTOR_MAP.putAll(pluginNameToSelectors);
        }
    }

    public void flushAllRule(final List<RuleData> ruleDataList) {
        if (CollectionUtils.isEmpty(ruleDataList)) {
            LOGGER.info("clear all rule cache, old cache:{}", RULE_MAP);
            RULE_MAP.clear();
        } else {

            // 确认 清除 备份替换
            // UpstreamCacheManager.clear();
            ruleDataList.forEach(rule -> {
                if (PluginEnum.DIVIDE.getName().equals(rule.getPluginName())) {
                    UpstreamCacheManager.submit(rule);
                }
            });

            // group by selectorId, then sort by sort value
            Map<String, List<RuleData>> selectorToRules = ruleDataList.stream()
                                                                      .collect(Collectors.groupingBy(RuleData::getSelectorId));
            selectorToRules.keySet().forEach(selectorId -> {
                List<RuleData> sorted = selectorToRules.get(selectorId).stream()
                                                       .sorted(Comparator.comparing(RuleData::getSort)).collect(Collectors.toList());
                selectorToRules.put(selectorId, sorted);
            });
            RULE_MAP.clear();
            RULE_MAP.putAll(selectorToRules);
        }
    }

    @Override
    public void flushAllDubboMapping(List<DubboResourceData> data) {

        if (CollectionUtils.isEmpty(data)) {
            LOGGER.info("dubbo resource data is empty");

            return;
        }

        Map<String, DubboResourceStream> resourceMap = data.stream()
                                                           .filter(d -> d.getEnabled() != null && d.getEnabled())
                                                           .collect(Collectors.toMap(DubboResourceData::getKey, v -> {

                                                               DubboResourceStream resource = this.mapToDubboResource(v);

                                                               List<ParamMetasData> paramMetasData = v.getParamMetas().stream().sorted(Comparator.comparing(ParamMetasData::getKeySort)).collect(Collectors.toList());

                                                               LinkedHashMap<String, String> paramMetasMap = new LinkedHashMap<>();
                                                               paramMetasData.forEach(
                                                                       p -> {
                                                                            if(!Strings.isNullOrEmpty(p.getKey())){
                                                                                paramMetasMap.put(p.getKey(), p.getType());
                                                                            }
                                                                       }

                                                               );

                                                               resource.setParamMetas(paramMetasMap);
                                                               return resource;
                                                           }));

        // DUBBO_MAP.clear();
        DUBBO_MAP.putAll(resourceMap);

        Set<DubboResourceData> offResource = data.stream().filter(d -> d.getEnabled() == null || !d.getEnabled()).collect(Collectors.toSet());
        offResource.forEach(d -> {
            if (DUBBO_MAP.get(d.getKey()) != null) {
                DUBBO_MAP.remove(d.getKey());
            }
        });

    }

    @Override
    public DiabloConfig.SyncCacheHttpConfig httpConfig() {
        return this.httpConfig;
    }
}
