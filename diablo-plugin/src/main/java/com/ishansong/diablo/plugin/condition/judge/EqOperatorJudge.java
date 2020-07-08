package com.ishansong.diablo.plugin.condition.judge;

import com.ishansong.diablo.core.model.condition.ConditionData;

import java.util.Objects;

public class EqOperatorJudge implements OperatorJudge {

    @Override
    public Boolean judge(final ConditionData conditionData, final String realData) {
        return Objects.equals(realData, conditionData.getParamValue().trim());
    }
}
