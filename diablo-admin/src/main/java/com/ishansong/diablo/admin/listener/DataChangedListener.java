package com.ishansong.diablo.admin.listener;


import com.ishansong.diablo.core.enums.DataEventTypeEnum;
import com.ishansong.diablo.core.model.dubbo.DubboResourceData;
import com.ishansong.diablo.core.model.plugin.PluginData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;

import java.util.List;

public interface DataChangedListener {

    default void onPluginChanged(List<PluginData> changed, DataEventTypeEnum eventType, Long durationStart) {
    }

    default void onSelectorChanged(List<SelectorData> changed, DataEventTypeEnum eventType, Long durationStart) {
    }

    default void onRuleChanged(List<RuleData> changed, DataEventTypeEnum eventType, Long durationStart) {
    }

    default void onDubboResourceChanged(List<DubboResourceData> changed, DataEventTypeEnum eventType, Long durationStart) {
    }


}
