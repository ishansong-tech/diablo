package com.ishansong.diablo.core.enums;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum HttpMethodEnum {

    HEAD("head", true),

    GET("get", true),

    POST("post", true),

    PUT("put", true),

    OPTIONS("options", true),

    PATCH("patch", true),

//    TRACE("trace", true),

    DELETE("delete", true);

    private final String name;

    private final Boolean support;

    private final static Map<String, HttpMethodEnum> methodEnumMap = Arrays.stream(HttpMethodEnum.values()).collect(Collectors.toMap(HttpMethodEnum::getName, e -> e));

    public static HttpMethodEnum acquireByName(final String name) {
        if (Strings.isNullOrEmpty(name)) {
            return null;
        }

        return methodEnumMap.get(name);
    }

}
