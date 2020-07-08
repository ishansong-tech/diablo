package com.ishansong.diablo.core.model.condition;

import com.ishansong.diablo.core.enums.OperatorEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionData implements Serializable {

    private String paramType;

    private String operator;

    private String paramName;

    private String paramValue;
}
