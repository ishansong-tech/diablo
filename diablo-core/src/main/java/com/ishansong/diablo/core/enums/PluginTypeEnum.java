package com.ishansong.diablo.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PluginTypeEnum {

    BEFORE("before"),

    FUNCTION("function"),

    LAST("last");

    private final String name;

}
