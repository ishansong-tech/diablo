package com.ishansong.diablo.plugin.condition.strategy;

import com.ishansong.diablo.core.model.condition.ConditionData;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

public interface MatchStrategy {

    Boolean match(List<ConditionData> conditionDataList, ServerWebExchange exchange);
}
