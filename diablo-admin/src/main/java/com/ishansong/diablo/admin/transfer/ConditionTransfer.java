package com.ishansong.diablo.admin.transfer;

import com.ishansong.diablo.admin.dto.RuleConditionDTO;
import com.ishansong.diablo.admin.dto.SelectorConditionDTO;
import com.ishansong.diablo.admin.entity.RuleConditionDO;
import com.ishansong.diablo.admin.entity.SelectorConditionDO;
import com.ishansong.diablo.core.model.condition.ConditionData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConditionTransfer {

    ConditionTransfer INSTANCE = Mappers.getMapper(ConditionTransfer.class);

    ConditionData mapToSelectorDO(SelectorConditionDO selectorConditionDO);

    ConditionData mapToSelectorDTO(SelectorConditionDTO selectorConditionDTO);

    ConditionData mapToRuleDO(RuleConditionDO ruleConditionDO);

    ConditionData mapToRuleDTO(RuleConditionDTO ruleConditionDTO);


}
