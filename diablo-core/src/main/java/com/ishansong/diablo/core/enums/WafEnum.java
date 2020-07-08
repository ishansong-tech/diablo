package com.ishansong.diablo.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum WafEnum {

    REJECT(0, "reject"),

    ALLOW(1, "allow");

    private final int code;

    private final String name;
}
