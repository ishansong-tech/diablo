package com.ishansong.diablo.core.enums;

import com.ishansong.diablo.core.exception.DiabloException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum OperatorEnum {

    MATCH("match", true),

    EQ("=", true),

    REGEX("regEx", true),

    GT(">", false),

    LT("<", false),

    PREFIX("prefix", true),

    LIKE("like", true);

    private final String alias;

    private final Boolean support;

    public static List<OperatorEnum> acquireSupport() {
        return Arrays.stream(OperatorEnum.values())
                     .filter(e -> e.support).collect(Collectors.toList());
    }

    public static OperatorEnum getOperatorEnumByAlias(final String alias) {
        return Arrays.stream(OperatorEnum.values())
                     .filter(e -> e.getAlias().equals(alias) && e.support).findFirst()
                     .orElseThrow(() -> new DiabloException(" this  operator can not support!"));

    }
}
