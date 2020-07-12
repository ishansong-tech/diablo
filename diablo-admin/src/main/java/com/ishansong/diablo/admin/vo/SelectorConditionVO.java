package com.ishansong.diablo.admin.vo;

import com.ishansong.diablo.admin.entity.SelectorConditionDO;
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
public class SelectorConditionVO implements Serializable {

    private String id;

    private String selectorId;

    private String paramType;

    private String paramTypeName;

    private String operator;

    private String operatorName;

    private String paramName;

    private String paramValue;

    private String dateCreated;

    private String dateUpdated;

    public static SelectorConditionVO buildSelectorConditionVO(final SelectorConditionDO selectorConditionDO) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
        ParamTypeEnum paramTypeEnum = ParamTypeEnum.getParamTypeEnumByName(selectorConditionDO.getParamType());
        OperatorEnum operatorEnum = OperatorEnum.getOperatorEnumByAlias(selectorConditionDO.getOperator());
        return new SelectorConditionVO(selectorConditionDO.getId(), selectorConditionDO.getSelectorId(), selectorConditionDO.getParamType(),
                paramTypeEnum == null ? null : paramTypeEnum.name(), selectorConditionDO.getOperator(),
                operatorEnum == null ? null : operatorEnum.name(), selectorConditionDO.getParamName(), selectorConditionDO.getParamValue(),
                dateTimeFormatter.format(selectorConditionDO.getDateCreated().toLocalDateTime()),
                dateTimeFormatter.format(selectorConditionDO.getDateUpdated().toLocalDateTime()));
    }
}
