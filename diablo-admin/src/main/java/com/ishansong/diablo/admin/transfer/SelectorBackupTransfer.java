package com.ishansong.diablo.admin.transfer;

import com.ishansong.diablo.admin.entity.RuleConditionDO;
import com.ishansong.diablo.admin.entity.RuleDO;
import com.ishansong.diablo.admin.entity.SelectorConditionDO;
import com.ishansong.diablo.admin.entity.SelectorDO;
import com.ishansong.diablo.admin.vo.SelectorBackupVO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SelectorBackupTransfer {

    SelectorBackupTransfer INSTANCE = Mappers.getMapper(SelectorBackupTransfer.class);

    @Mappings({
            @Mapping(source = "dateCreated", target = "dateCreated", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "dateUpdated", target = "dateUpdated", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "datePublished", target = "datePublished", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "dateRollbacked", target = "dateRollbacked", dateFormat = "yyyy-MM-dd HH:mm:ss")
    })
    SelectorBackupVO.SelectorBackup mapToSelectorBackup(SelectorDO selectorDO);

    @Mappings({
            @Mapping(source = "dateCreated", target = "dateCreated", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "dateUpdated", target = "dateUpdated", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "datePublished", target = "datePublished", dateFormat = "yyyy-MM-dd HH:mm:ss"),
    })
    @Named("selectorCondition")
    SelectorBackupVO.SelectorConditionBackup mapToSelectorBackupCondition(SelectorConditionDO selectorCondition);

    @IterableMapping(qualifiedByName = "selectorCondition")
    List<SelectorBackupVO.SelectorConditionBackup> mapToSelectorBackupCondition(List<SelectorConditionDO> selectorConditions);

    @Mappings({
            @Mapping(source = "dateCreated", target = "dateCreated", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "dateUpdated", target = "dateUpdated", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "datePublished", target = "datePublished", dateFormat = "yyyy-MM-dd HH:mm:ss"),
    })
    @Named("rule")
    SelectorBackupVO.RuleBackup mapToRuleBackup(RuleDO rule);

    @IterableMapping(qualifiedByName = "rule")
    List<SelectorBackupVO.RuleBackup> mapToRuleBackup(List<RuleDO> rule);

    @Mappings({
            @Mapping(source = "dateCreated", target = "dateCreated", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "dateUpdated", target = "dateUpdated", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "datePublished", target = "datePublished", dateFormat = "yyyy-MM-dd HH:mm:ss"),
    })
    @Named("ruleCondition")
    SelectorBackupVO.RuleBackup mapToRuleConditionBackup(RuleConditionDO ruleCondition);

    @IterableMapping(qualifiedByName = "ruleCondition")
    List<SelectorBackupVO.RuleConditionBackup> mapToRuleConditionBackup(List<RuleConditionDO> rule);
}
