package com.ishansong.diablo.plugin.condition.strategy;

import com.google.common.collect.Maps;
import com.ishansong.diablo.core.enums.MatchModeEnum;

import java.util.Map;

public class MatchStrategyFactory {

    private static final Map<Integer, MatchStrategy> MATCH_STRATEGY_MAP = Maps.newHashMapWithExpectedSize(2);

    static {
        MATCH_STRATEGY_MAP.put(MatchModeEnum.AND.getCode(), new AndMatchStrategy());
        MATCH_STRATEGY_MAP.put(MatchModeEnum.OR.getCode(), new OrMatchStrategy());
    }

    public static MatchStrategy of(final Integer strategy) {
        return MATCH_STRATEGY_MAP.get(strategy);
    }
}
