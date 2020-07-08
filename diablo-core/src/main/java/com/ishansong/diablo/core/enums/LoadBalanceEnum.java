package com.ishansong.diablo.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LoadBalanceEnum {

    HASH(1, "hash", true),

    RANDOM(2, "random", true),

    ROUND_ROBIN(3, "roundRobin", true);

    private final int code;

    private final String name;

    private final boolean support;

}
