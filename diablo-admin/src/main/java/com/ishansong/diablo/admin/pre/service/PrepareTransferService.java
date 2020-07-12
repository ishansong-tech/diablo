package com.ishansong.diablo.admin.pre.service;

import com.ishansong.diablo.admin.entity.RuleConditionDO;
import com.ishansong.diablo.admin.entity.RuleDO;
import com.ishansong.diablo.admin.entity.SelectorConditionDO;
import com.ishansong.diablo.admin.entity.SelectorDO;

import java.util.List;
import java.util.Map;

public interface PrepareTransferService {

    Map<String, Integer> transferSelector(String syncSelectorId, SelectorDO syncSelectorDO, List<SelectorConditionDO> syncSelectorConditions,
                                          List<RuleDO> syncSelectorRules, List<String> syncRuleIds, List<RuleConditionDO> syncRuleConditions,

                                          List<String> backupRuleIds, SelectorDO backupSelectorDO, List<SelectorConditionDO> backupSelectorConditions,
                                          List<RuleDO> backupRules, List<RuleConditionDO> backupRuleConditions);

    Map<String, Integer> rollbackSelector(String backupSelectorId, List<String> backupRuleIds,
                                          SelectorDO backupSelector, List<SelectorConditionDO> backupSelectorConditions, List<RuleDO> backupRules, List<RuleConditionDO> backupRuleConditions);
}
