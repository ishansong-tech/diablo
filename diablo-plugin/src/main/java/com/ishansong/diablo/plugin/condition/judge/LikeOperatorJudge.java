package com.ishansong.diablo.plugin.condition.judge;

import com.ishansong.diablo.core.model.condition.ConditionData;

public class LikeOperatorJudge implements OperatorJudge {

    @Override
    public Boolean judge(final ConditionData conditionData, final String realData) {
        return realData.contains(conditionData.getParamValue().trim());
    }
}
