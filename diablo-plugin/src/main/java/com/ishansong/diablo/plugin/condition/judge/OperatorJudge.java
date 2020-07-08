package com.ishansong.diablo.plugin.condition.judge;

import com.ishansong.diablo.core.model.condition.ConditionData;

@FunctionalInterface
public interface OperatorJudge {

    Boolean judge(ConditionData conditionData, String realData);

}
