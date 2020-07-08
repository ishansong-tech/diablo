package com.ishansong.diablo.plugin.condition.judge;

import com.google.common.collect.Maps;
import com.ishansong.diablo.core.enums.OperatorEnum;
import com.ishansong.diablo.core.model.condition.ConditionData;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

public class OperatorJudgeFactory {

    private static final Map<String, OperatorJudge> OPERATOR_JUDGE_MAP = Maps.newHashMapWithExpectedSize(4);

    static {
        OPERATOR_JUDGE_MAP.put(OperatorEnum.EQ.getAlias(), new EqOperatorJudge());
        OPERATOR_JUDGE_MAP.put(OperatorEnum.MATCH.getAlias(), new MatchOperatorJudge());
        OPERATOR_JUDGE_MAP.put(OperatorEnum.LIKE.getAlias(), new LikeOperatorJudge());
        OPERATOR_JUDGE_MAP.put(OperatorEnum.REGEX.getAlias(), new RegExOperatorJudge());
        OPERATOR_JUDGE_MAP.put(OperatorEnum.PREFIX.getAlias(), new PrefixOperatorJudge());
    }

    public static Boolean judge(final ConditionData conditionData, final String realData) {
        if (Objects.isNull(conditionData) || StringUtils.isBlank(realData)) {
            return false;
        }
        return OPERATOR_JUDGE_MAP.get(conditionData.getOperator()).judge(conditionData, realData);
    }
}
