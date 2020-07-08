package com.ishansong.diablo.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ParamObjectType {

    /**
     * 1普通对象或url参数, dubbo接口单个参数或多个基本类型组合的参数或单个对象
     * {
     * "userId": 1
     * }
     */
    COMMON(1, "普通参数"),

    /**
     * 2 多个参数组合，dubbo接口多个参数(对象和基本类型混合)
     * [
     * {
     * "userId": 1
     * },
     * ["zhangsan"]
     * ]
     */
    COMPOSE(2, "组合嵌套参数");

    private final int code;

    private final String name;

    public static String acquireName(Integer objectType) {
        if (objectType == null) {
            return "";
        }

        return Arrays.stream(ParamObjectType.values())
                     .filter(t -> t.code == objectType)
                     .map(ParamObjectType::getName)
                     .findFirst().orElse("");

    }
}
