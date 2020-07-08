package com.ishansong.diablo.core.model.rule;

import com.ishansong.diablo.core.model.condition.ConditionData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleData implements Serializable {

    private String id;

    private String name;

    private String pluginName;

    private String selectorId;

    private String serviceInfoId;

    private Integer matchMode;

    private Integer sort;

    private Boolean enabled;

    private Boolean loged;

    private String handle;

    private String upstreamHandle;

    private List<ConditionData> conditionDataList;
}
