package com.ishansong.diablo.core.enums;

import com.ishansong.diablo.core.exception.DiabloException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum ParamTypeEnum {

    POST("post", true),

    URI("uri", true),

    QUERY("query", true),

    COOKIE("cookie", true),

    HOST("host", true),

    IP("ip", true),

    HEADER("header", true);

    private final String name;

    private final Boolean support;

    public static List<ParamTypeEnum> acquireSupport() {
        return Arrays.stream(ParamTypeEnum.values())
                .filter(e -> e.support).collect(Collectors.toList());
    }

    public static ParamTypeEnum getParamTypeEnumByName(final String name) {
        return Arrays.stream(ParamTypeEnum.values())
                .filter(e -> e.getName().equals(name) && e.support).findFirst()
                .orElseThrow(() -> new DiabloException(" this  param type can not support!"));
    }
}
