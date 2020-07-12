package com.ishansong.diablo.admin.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RuleConditionDTO implements Serializable {

    private String id;

    private String ruleId;

    private String paramType;

    private String operator;

    private String paramName;

    private String paramValue;
}
