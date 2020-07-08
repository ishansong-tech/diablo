package com.ishansong.diablo.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum MatchModeEnum {

    AND(0, "and"),

    OR(1, "or");

    private final int code;

    private final String name;

    public static String getMatchModeByCode(final int code) {
        return Arrays.stream(MatchModeEnum.values())
                .filter(e -> e.code == code).findFirst()
                .orElse(MatchModeEnum.AND)
                .getName();
    }
}
