package com.ishansong.diablo.admin.vo;

import com.ishansong.diablo.admin.entity.RuleConditionDO;
import com.ishansong.diablo.core.enums.OperatorEnum;
import com.ishansong.diablo.core.enums.ParamTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleConditionVO implements Serializable {

    private String id;

    private String ruleId;

    private String paramType;

    private String paramTypeName;

    private String operator;

    private String operatorName;

    private String paramName;

    private String paramValue;

    private String dateCreated;

    private String dateUpdated;

    public static RuleConditionVO buildRuleConditionVO(final RuleConditionDO ruleConditionDO) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ParamTypeEnum paramTypeEnum = ParamTypeEnum.getParamTypeEnumByName(ruleConditionDO.getParamType());
        OperatorEnum operatorEnum = OperatorEnum.getOperatorEnumByAlias(ruleConditionDO.getOperator());
        return new RuleConditionVO(ruleConditionDO.getId(), ruleConditionDO.getRuleId(), ruleConditionDO.getParamType(), paramTypeEnum == null ? null : paramTypeEnum.getName(),
                ruleConditionDO.getOperator(), operatorEnum == null ? null : operatorEnum.name(), ruleConditionDO.getParamName(), ruleConditionDO.getParamValue(),
                dateTimeFormatter.format(ruleConditionDO.getDateCreated().toLocalDateTime()),
                dateTimeFormatter.format(ruleConditionDO.getDateUpdated().toLocalDateTime()));
    }
}
