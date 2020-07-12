package com.ishansong.diablo.admin.entity;

import com.ishansong.diablo.admin.dto.RuleConditionDTO;
import com.ishansong.diablo.core.utils.UUIDUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

@Data
public class RuleConditionDO extends BaseDO {

    private String ruleId;

    private String paramType;

    private String operator;

    private String paramName;

    private String paramValue;

    private Timestamp datePublished;

    public static RuleConditionDO buildRuleConditionDO(final RuleConditionDTO ruleConditionDTO) {
        if (ruleConditionDTO != null) {
            RuleConditionDO ruleConditionDO = new RuleConditionDO();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (StringUtils.isEmpty(ruleConditionDTO.getId())) {
                ruleConditionDO.setId(UUIDUtils.generateShortUuid());
                ruleConditionDO.setDateCreated(currentTime);
            } else {
                ruleConditionDO.setId(ruleConditionDTO.getId());
            }

            ruleConditionDO.setParamType(ruleConditionDTO.getParamType());
            ruleConditionDO.setRuleId(ruleConditionDTO.getRuleId());
            ruleConditionDO.setOperator(ruleConditionDTO.getOperator());
            ruleConditionDO.setParamName(ruleConditionDTO.getParamName());
            ruleConditionDO.setParamValue(ruleConditionDTO.getParamValue());
            ruleConditionDO.setDateUpdated(currentTime);
            return ruleConditionDO;
        }
        return null;
    }
}
