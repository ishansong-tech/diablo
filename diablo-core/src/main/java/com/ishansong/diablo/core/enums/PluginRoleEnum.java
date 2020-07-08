package com.ishansong.diablo.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PluginRoleEnum {

    SYS(0, "sys"),

    CUSTOM(1, "custom");

    private final Integer code;

    private final String name;

}
