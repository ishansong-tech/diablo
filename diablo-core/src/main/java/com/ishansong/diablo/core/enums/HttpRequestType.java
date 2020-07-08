package com.ishansong.diablo.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum HttpRequestType {

    GET(1, "GET"),

    POST(2, "POST");

    private final int code;

    private final String name;

    public static String acquireName(Integer objectType) {
        if (objectType == null) {
            return "";
        }

        return Arrays.stream(HttpRequestType.values())
                     .filter(t -> t.code == objectType)
                     .map(HttpRequestType::getName)
                     .findFirst().orElse("");

    }
}
