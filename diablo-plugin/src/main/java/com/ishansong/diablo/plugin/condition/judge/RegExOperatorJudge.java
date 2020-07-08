package com.ishansong.diablo.plugin.condition.judge;

import com.ishansong.diablo.core.model.condition.ConditionData;

import java.util.regex.Pattern;

public class RegExOperatorJudge implements OperatorJudge {

    @Override
    public Boolean judge(final ConditionData conditionData, final String realData) {
        return Pattern.matches(conditionData.getParamValue(), realData);
    }
}
