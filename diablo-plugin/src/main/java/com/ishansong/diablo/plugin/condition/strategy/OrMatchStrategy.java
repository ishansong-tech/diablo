package com.ishansong.diablo.plugin.condition.strategy;

import com.ishansong.diablo.plugin.condition.judge.OperatorJudgeFactory;
import com.ishansong.diablo.core.model.condition.ConditionData;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

public class OrMatchStrategy extends AbstractMatchStrategy implements MatchStrategy {

    @Override
    public Boolean match(final List<ConditionData> conditionDataList, final ServerWebExchange exchange) {
        return conditionDataList
                .stream()
                .anyMatch(condition -> OperatorJudgeFactory.judge(condition, buildRealData(condition, exchange)));
    }
}
