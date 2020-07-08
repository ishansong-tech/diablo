package com.ishansong.diablo.core.model.selector;

import com.ishansong.diablo.core.model.condition.ConditionData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectorData implements Serializable {

    private String id;

    private String pluginId;

    private String pluginName;

    private String name;

    private Integer matchMode;

    private Integer type;

    private Integer sort;

    private Boolean enabled;

    private Boolean loged;

    private Boolean continued;

    private String handle;

    private List<ConditionData> conditionList;
}
