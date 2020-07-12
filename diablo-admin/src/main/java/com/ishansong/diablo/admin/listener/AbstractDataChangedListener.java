package com.ishansong.diablo.admin.listener;

import com.ishansong.diablo.admin.service.DubboResourceService;
import com.ishansong.diablo.admin.service.PluginService;
import com.ishansong.diablo.admin.service.RuleService;
import com.ishansong.diablo.admin.service.SelectorService;
import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.enums.DataEventTypeEnum;
import com.ishansong.diablo.core.model.ConfigData;
import com.ishansong.diablo.core.model.dubbo.DubboResourceData;
import com.ishansong.diablo.core.model.plugin.PluginData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;

import javax.annotation.Resource;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractDataChangedListener implements DataChangedListener {

    private static final Logger logger = getLogger(AbstractDataChangedListener.class);

    @Resource
    protected CacheService cacheService;

    @Resource
    private PluginService pluginService;

    @Resource
    private RuleService ruleService;

    @Resource
    private SelectorService selectorService;

    @Resource
    private DubboResourceService dubboResourceService;

    public ConfigData<?> fetchConfig(ConfigGroupEnum groupKey) {
        ConfigDataCache config = this.cacheService.getConfigDataCacheService().get(groupKey.name());
        switch (groupKey) {
            case PLUGIN:
                return new ConfigData<>(config.getMd5(), config.getLastModifyTime(), pluginService.listAll());
            case RULE:
                return new ConfigData<>(config.getMd5(), config.getLastModifyTime(), ruleService.listAll());
            case SELECTOR:
                return new ConfigData<>(config.getMd5(), config.getLastModifyTime(), selectorService.listAll());
            case DUBBO_MAPPING:
                return new ConfigData<>(config.getMd5(), config.getLastModifyTime(), cacheService.loadDubboResourceCache()); //cache get()
            default:
                throw new IllegalStateException("Unexpected groupKey: " + groupKey);
        }
    }


    @Override
    public void onPluginChanged(List<PluginData> changed, DataEventTypeEnum eventType, Long durationStart) {
        if (CollectionUtils.isEmpty(changed)) {
            return;
        }
        this.cacheService.updatePluginCache();
        this.afterPluginChanged(changed, eventType, durationStart);
    }

    protected void afterPluginChanged(List<PluginData> changed, DataEventTypeEnum eventType, Long durationStart) {
    }

    @Override
    public void onRuleChanged(List<RuleData> changed, DataEventTypeEnum eventType, Long durationStart) {
        this.cacheService.updateRuleCache();
        this.afterRuleChanged(changed, eventType, durationStart);
    }

    protected void afterRuleChanged(List<RuleData> changed, DataEventTypeEnum eventType, Long durationStart) {
    }

    @Override
    public void onSelectorChanged(List<SelectorData> changed, DataEventTypeEnum eventType, Long durationStart) {
        this.cacheService.updateSelectorCache();
        this.afterSelectorChanged(changed, eventType, durationStart);
    }

    protected void afterSelectorChanged(List<SelectorData> changed, DataEventTypeEnum eventType, Long durationStart) {
    }

    @Override
    public void onDubboResourceChanged(List<DubboResourceData> changed, DataEventTypeEnum eventType, Long durationStart) {
        this.cacheService.updateDubboResourceCache();

        this.afterDubboResourceChanged(changed, eventType, durationStart);
    }

    protected void afterDubboResourceChanged(List<DubboResourceData> changed, DataEventTypeEnum eventType, Long durationStart) {
    }
}
