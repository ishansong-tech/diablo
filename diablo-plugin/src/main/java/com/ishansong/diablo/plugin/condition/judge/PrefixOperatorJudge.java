package com.ishansong.diablo.plugin.condition.judge;

import com.ishansong.diablo.core.model.condition.ConditionData;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

public class PrefixOperatorJudge implements OperatorJudge {

    private final PathMatcher pathMatcher;

    public PrefixOperatorJudge() {
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    public Boolean judge(ConditionData conditionData, String realData) {
        return pathMatcher.match(conditionData.getParamValue(), realData);
    }
}
