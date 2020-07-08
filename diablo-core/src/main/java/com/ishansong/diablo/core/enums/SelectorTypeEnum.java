package com.ishansong.diablo.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SelectorTypeEnum {

    FULL_FLOW(0, "全流量"),

    CUSTOM_FLOW(1, "自定义流量");

    private final int code;

    private final String name;

    public static String getSelectorTypeByCode(final int code) {
        for (SelectorTypeEnum selectorTypeEnum : SelectorTypeEnum.values()) {
            if (selectorTypeEnum.getCode() == code) {
                return selectorTypeEnum.getName();
            }
        }
        return null;
    }
}
