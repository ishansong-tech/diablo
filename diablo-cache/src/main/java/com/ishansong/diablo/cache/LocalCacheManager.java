package com.ishansong.diablo.cache;

import com.ishansong.diablo.config.DiabloConfig;
import com.ishansong.diablo.core.model.dubbo.DubboResourceData;
import com.ishansong.diablo.core.model.dubbo.DubboResourceStream;
import com.ishansong.diablo.core.model.plugin.PluginData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;

import java.util.List;

public interface LocalCacheManager {

    DiabloConfig.SyncCacheHttpConfig httpConfig();

    PluginData findPluginByName(String pluginName);

    List<SelectorData> findSelectorByPluginName(String pluginName);

    List<RuleData> findRuleBySelectorId(String selectorId);

    RuleData findRuleByRuleId(String ruleId);

    DubboResourceStream findDubbResource(String resourceKey);

    void flushAllPlugin(final List<PluginData> pluginDataList);

    void flushAllSelector(final List<SelectorData> selectorDataList);

    void flushAllRule(final List<RuleData> ruleDataList);

    void flushAllDubboMapping(List<DubboResourceData> data);
}
