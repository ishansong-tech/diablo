package com.ishansong.diablo.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ParamSourceType {

    QueryString(1, "QueryString"),

    RequestBody(2, "RequestBody");

    private final int code;

    private final String name;

    public static String acquireName(Integer objectType) {
        if (objectType == null) {
            return "";
        }

        return Arrays.stream(ParamSourceType.values())
                     .filter(t -> t.code == objectType)
                     .map(ParamSourceType::getName)
                     .findFirst().orElse("");

    }
}
