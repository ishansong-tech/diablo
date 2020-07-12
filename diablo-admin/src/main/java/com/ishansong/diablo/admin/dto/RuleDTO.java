package com.ishansong.diablo.admin.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RuleDTO implements Serializable {

    private String id;

    private String selectorId;

    private String serviceInfoId;

    private Integer matchMode;

    private String name;

    private Boolean enabled;

    private Boolean loged;

    private Integer sort;

    private String handle;

    private String upstreamHandle;

    private List<RuleConditionDTO> ruleConditions;
}
