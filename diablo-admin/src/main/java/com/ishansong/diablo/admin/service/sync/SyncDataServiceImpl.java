package com.ishansong.diablo.admin.service.sync;

import com.ishansong.diablo.admin.listener.DataChangedEvent;
import com.ishansong.diablo.admin.service.*;
import com.ishansong.diablo.admin.transfer.PluginTransfer;
import com.ishansong.diablo.admin.vo.PluginVO;
import com.ishansong.diablo.admin.service.*;
import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.enums.DataEventTypeEnum;
import com.ishansong.diablo.core.model.dubbo.DubboResourceData;
import com.ishansong.diablo.core.model.plugin.PluginData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service("syncDataService")
public class SyncDataServiceImpl implements SyncDataService {

    private final PluginService pluginService;

    private final SelectorService selectorService;

    private final RuleService ruleService;

    private final DubboResourceService dubboResourceService;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public SyncDataServiceImpl(
            final PluginService pluginService,
            final SelectorService selectorService,
            final RuleService ruleService,
            final DubboResourceService dubboResourceService,
            final ApplicationEventPublisher eventPublisher) {
        this.pluginService = pluginService;
        this.selectorService = selectorService;
        this.ruleService = ruleService;
        this.dubboResourceService = dubboResourceService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean syncAll(DataEventTypeEnum type) {

        List<PluginData> pluginDataList = pluginService.listAll();
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.PLUGIN,
                type,
                pluginDataList));

        List<SelectorData> selectorDataList = selectorService.listAll();
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.SELECTOR,
                type,
                selectorDataList));

        List<RuleData> ruleDataList = ruleService.listAll();
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.RULE,
                type,
                ruleDataList));

        List<DubboResourceData> dubboResources = dubboResourceService.listAll();
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.DUBBO_MAPPING, type, dubboResources));

        return true;
    }

    @Override
    public boolean syncPluginData(String pluginId) {
        PluginVO pluginVO = pluginService.findById(pluginId);
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.PLUGIN, DataEventTypeEnum.UPDATE,
                Collections.singletonList(PluginTransfer.INSTANCE.mapDataTOVO(pluginVO))));
        List<SelectorData> selectorDataList = selectorService.findByPluginId(pluginId);
        if (CollectionUtils.isNotEmpty(selectorDataList)) {
            eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.SELECTOR,
                    DataEventTypeEnum.UPDATE,
                    selectorDataList));
            for (SelectorData selectData : selectorDataList) {
                List<RuleData> ruleDataList = ruleService.findBySelectorId(selectData.getId());
                eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.RULE,
                        DataEventTypeEnum.UPDATE,
                        ruleDataList));
            }
        }
        return true;
    }
}
