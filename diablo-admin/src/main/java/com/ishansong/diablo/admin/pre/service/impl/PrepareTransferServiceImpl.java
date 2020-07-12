package com.ishansong.diablo.admin.pre.service.impl;

import com.ishansong.diablo.admin.entity.RuleConditionDO;
import com.ishansong.diablo.admin.entity.RuleDO;
import com.ishansong.diablo.admin.entity.SelectorConditionDO;
import com.ishansong.diablo.admin.entity.SelectorDO;
import com.ishansong.diablo.admin.pre.mapper.RuleConditionMapper;
import com.ishansong.diablo.admin.pre.mapper.RuleMapper;
import com.ishansong.diablo.admin.pre.mapper.SelectorConditionMapper;
import com.ishansong.diablo.admin.pre.mapper.SelectorMapper;
import com.ishansong.diablo.admin.pre.service.PrepareTransferService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrepareTransferServiceImpl implements PrepareTransferService {

    private com.ishansong.diablo.admin.mapper.SelectorMapper selectorMapper;
    private com.ishansong.diablo.admin.mapper.SelectorConditionMapper selectorConditionMapper;
    private final com.ishansong.diablo.admin.mapper.RuleMapper ruleMapper;
    private final com.ishansong.diablo.admin.mapper.RuleConditionMapper ruleConditionMapper;

    // pre Mapper
    private SelectorMapper preSelectorMapper;
    private SelectorConditionMapper preSelectorConditionMapper;
    private final RuleMapper preRuleMapper;
    private final RuleConditionMapper preRuleConditionMapper;

    private com.ishansong.diablo.admin.pre.mapper.backup.SelectorMapper backupSelectorMapper;
    private com.ishansong.diablo.admin.pre.mapper.backup.SelectorConditionMapper backupSelectorConditionMapper;
    private final com.ishansong.diablo.admin.pre.mapper.backup.RuleMapper backupRuleMapper;
    private final com.ishansong.diablo.admin.pre.mapper.backup.RuleConditionMapper backupRuleConditionMapper;

    @Autowired(required = false)
    public PrepareTransferServiceImpl(com.ishansong.diablo.admin.mapper.SelectorMapper selectorMapper, com.ishansong.diablo.admin.mapper.SelectorConditionMapper selectorConditionMapper, com.ishansong.diablo.admin.mapper.RuleMapper ruleMapper, com.ishansong.diablo.admin.mapper.RuleConditionMapper ruleConditionMapper, SelectorMapper preSelectorMapper, SelectorConditionMapper preSelectorConditionMapper, RuleMapper preRuleMapper, RuleConditionMapper preRuleConditionMapper, com.ishansong.diablo.admin.pre.mapper.backup.SelectorMapper backupSelectorMapper, com.ishansong.diablo.admin.pre.mapper.backup.SelectorConditionMapper backupSelectorConditionMapper, com.ishansong.diablo.admin.pre.mapper.backup.RuleMapper backupRuleMapper, com.ishansong.diablo.admin.pre.mapper.backup.RuleConditionMapper backupRuleConditionMapper) {
        this.selectorMapper = selectorMapper;
        this.selectorConditionMapper = selectorConditionMapper;
        this.ruleMapper = ruleMapper;
        this.ruleConditionMapper = ruleConditionMapper;
        this.preSelectorMapper = preSelectorMapper;
        this.preSelectorConditionMapper = preSelectorConditionMapper;
        this.preRuleMapper = preRuleMapper;
        this.preRuleConditionMapper = preRuleConditionMapper;
        this.backupSelectorMapper = backupSelectorMapper;
        this.backupSelectorConditionMapper = backupSelectorConditionMapper;
        this.backupRuleMapper = backupRuleMapper;
        this.backupRuleConditionMapper = backupRuleConditionMapper;
    }

    @Override
    @Transactional(transactionManager = "preTransactionManager", isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class})
    public Map<String, Integer> transferSelector(String syncSelectorId, SelectorDO syncSelectorDO, List<SelectorConditionDO> syncSelectorConditions,
                                                 List<RuleDO> syncSelectorRules, List<String> syncRuleIds, List<RuleConditionDO> syncRuleConditions,

                                                 List<String> backupRuleIds, SelectorDO backupSelectorDO, List<SelectorConditionDO> backupSelectorConditions,
                                                 List<RuleDO> backupRules, List<RuleConditionDO> backupRuleConditions) {

        Map<String, Integer> result = new LinkedHashMap<>();

        int deleteRuleCondition = 0;
        if (CollectionUtils.isNotEmpty(syncRuleIds)) {
            deleteRuleCondition = preRuleConditionMapper.deleteBatch(syncRuleIds);
        }
        int deleteRule = preRuleMapper.deleteBySelectorId(syncSelectorId);
        int deleteSelectorCondition = preSelectorConditionMapper.deleteBySelectorId(syncSelectorId);
        int deleteSelector = preSelectorMapper.delete(syncSelectorId);

        result.put("删除规则条件", deleteRuleCondition);
        result.put("删除规则", deleteRule);
        result.put("删除路由条件", deleteSelectorCondition);
        result.put("删除路由", deleteSelector);

        // insert
        int insertSelector = preSelectorMapper.insertSelective(syncSelectorDO);
        int insertSelectorCondition = 0;
        if (CollectionUtils.isNotEmpty(syncSelectorConditions)) {
            insertSelectorCondition = preSelectorConditionMapper.insertBatch(syncSelectorConditions);
        }
        int insertRule = 0;
        if (CollectionUtils.isNotEmpty(syncSelectorRules)) {
            insertRule = preRuleMapper.insertBatch(syncSelectorRules);
        }
        int insertRuleCondition = 0;
        if (CollectionUtils.isNotEmpty(syncRuleConditions)) {
            insertRuleCondition = preRuleConditionMapper.insertBatch(syncRuleConditions);
        }

        result.put("新增路由", insertSelector);
        result.put("新增路由条件", insertSelectorCondition);
        result.put("新增规则", insertRule);
        result.put("新增规则条件", insertRuleCondition);

        result.put("------------", 0);


        // backup online selector for roll-back
//        int backupDeleteRuleCondition = 0;
//        if (CollectionUtils.isNotEmpty(backupRuleIds)) {
//            backupDeleteRuleCondition = backupRuleConditionMapper.deleteBatch(backupRuleIds);
//        }
//        int backupDeleteRule = backupRuleMapper.deleteBySelectorId(syncSelectorId);
//        int backupDeleteSelectorCondition = backupSelectorConditionMapper.deleteBySelectorId(syncSelectorId);
//        int backupDeleteSelector = backupSelectorMapper.delete(syncSelectorId);

        int backupInsertSelector = 0;
        if (backupSelectorDO != null) {
            backupInsertSelector = backupSelectorMapper.insertSelective(backupSelectorDO);
        }
        int backupInsertSelectorCondition = 0;
        if (CollectionUtils.isNotEmpty(backupSelectorConditions)) {
            backupInsertSelectorCondition = backupSelectorConditionMapper.insertBatch(backupSelectorConditions);
        }
        int backupInsertRule = 0;
        if (CollectionUtils.isNotEmpty(backupRules)) {
            backupInsertRule = backupRuleMapper.insertBatch(backupRules);
        }
        int backupInsertRuleCondition = 0;
        if (CollectionUtils.isNotEmpty(backupRuleConditions)) {
            backupInsertRuleCondition = backupRuleConditionMapper.insertBatch(backupRuleConditions);
        }

//        result.put("删除备份规则条件", backupDeleteRuleCondition);
//        result.put("删除备份规则", backupDeleteRule);
//        result.put("删除备份路由条件", backupDeleteSelectorCondition);
//        result.put("删除备份路由", backupDeleteSelector);

        result.put("新增备份路由", backupInsertSelector);
        result.put("新增备份路由条件", backupInsertSelectorCondition);
        result.put("新增备份规则", backupInsertRule);
        result.put("新增备份规则条件", backupInsertRuleCondition);

        return result;
    }


    @Override
    @Transactional(transactionManager = "onlineTransactionManager", isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public Map<String, Integer> rollbackSelector(String backupSelectorId, List<String> backupRuleIds,
                                                 SelectorDO backupSelector, List<SelectorConditionDO> backupSelectorConditions, List<RuleDO> backupRules, List<RuleConditionDO> backupRuleConditions) {

        Map<String, Integer> result = new LinkedHashMap<>();

        int deleteRuleCondition = 0;
        if (CollectionUtils.isNotEmpty(backupRuleIds)) {
            deleteRuleCondition = preRuleConditionMapper.deleteBatch(backupRuleIds);
        }
        int deleteRule = preRuleMapper.deleteBySelectorId(backupSelectorId);
        int deleteSelectorCondition = preSelectorConditionMapper.deleteBySelectorId(backupSelectorId);
        int deleteSelector = preSelectorMapper.delete(backupSelectorId);

        result.put("删除规则条件", deleteRuleCondition);
        result.put("删除规则", deleteRule);
        result.put("删除路由条件", deleteSelectorCondition);
        result.put("删除路由", deleteSelector);

        int insertSelector = 0;
        if (backupSelector != null) {
            backupSelector.setDateRollbacked(new Timestamp(System.currentTimeMillis()));
            insertSelector = preSelectorMapper.insertSelective(backupSelector);
        }
        int insertSelectorCondition = 0;
        if (CollectionUtils.isNotEmpty(backupSelectorConditions)) {
            insertSelectorCondition = preSelectorConditionMapper.insertBatch(backupSelectorConditions);
        }
        int insertRule = 0;
        if (CollectionUtils.isNotEmpty(backupRules)) {
            insertRule = preRuleMapper.insertBatch(backupRules);
        }
        int insertRuleCondition = 0;
        if (CollectionUtils.isNotEmpty(backupRuleConditions)) {
            insertRuleCondition = preRuleConditionMapper.insertBatch(backupRuleConditions);
        }

        result.put("新增路由", insertSelector);
        result.put("新增路由条件", insertSelectorCondition);
        result.put("新增规则", insertRule);
        result.put("新增规则条件", insertRuleCondition);

        return result;
    }
}
