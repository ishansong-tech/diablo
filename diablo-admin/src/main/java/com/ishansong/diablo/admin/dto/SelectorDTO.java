package com.ishansong.diablo.admin.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SelectorDTO implements Serializable {

    private String id;

    private String pluginId;

    private String name;

    private Integer matchMode;

    private Integer type;

    private Integer sort;

    private Boolean enabled;

    private Boolean loged;

    private Boolean continued;

    private String handle;

    private List<SelectorConditionDTO> selectorConditions;
}
