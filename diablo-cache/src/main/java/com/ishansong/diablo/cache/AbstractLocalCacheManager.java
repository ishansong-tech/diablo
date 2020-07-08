package com.ishansong.diablo.cache;

import com.ishansong.diablo.core.model.dubbo.DubboResourceStream;
import com.ishansong.diablo.core.model.plugin.PluginData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractLocalCacheManager implements LocalCacheManager {

    static final ConcurrentMap<String, PluginData> PLUGIN_MAP = new ConcurrentHashMap<>();

    static final ConcurrentMap<String, List<SelectorData>> SELECTOR_MAP = new ConcurrentHashMap<>();

    static final ConcurrentMap<String, List<RuleData>> RULE_MAP = new ConcurrentHashMap<>();

    static final ConcurrentMap<String, DubboResourceStream> DUBBO_MAP = new ConcurrentHashMap<>();

    @Override
    public PluginData findPluginByName(final String pluginName) {
        return PLUGIN_MAP.get(pluginName);
    }

    @Override
    public List<SelectorData> findSelectorByPluginName(final String pluginName) {
        return SELECTOR_MAP.get(pluginName);
    }

    @Override
    public List<RuleData> findRuleBySelectorId(final String selectorId) {
        return RULE_MAP.get(selectorId);
    }

    @Override
    public RuleData findRuleByRuleId(String ruleId) {
        for(Map.Entry<String,List<RuleData>> entry :RULE_MAP.entrySet()){
            if(!CollectionUtils.isEmpty(entry.getValue())){
                Optional<RuleData> ruleDataOptional=entry.getValue().stream().filter(ruleData -> {
                    return Objects.equals(ruleData.getId(),ruleId);
                }).findFirst();
                if(ruleDataOptional.isPresent()){
                    return ruleDataOptional.get();
                }

            }
        }
        return null;
    }

    @Override
    public DubboResourceStream findDubbResource(String resourceKey) {
        return DUBBO_MAP.get(resourceKey);
    }
}
