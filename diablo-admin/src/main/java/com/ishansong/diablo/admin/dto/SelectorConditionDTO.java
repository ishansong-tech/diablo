package com.ishansong.diablo.admin.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SelectorConditionDTO implements Serializable {

    private String id;

    private String selectorId;

    private String paramType;

    private String operator;

    private String paramName;

    private String paramValue;
}
