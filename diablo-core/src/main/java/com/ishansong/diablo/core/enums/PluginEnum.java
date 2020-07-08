package com.ishansong.diablo.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum PluginEnum {

    GLOBAL(1, 0, "global"),

    SIGN(2, 0, "sign"),

    WAF(10, 0, "waf"),

    TOKEN(11, 0, "token"),

    RATE_LIMITER(20, 0, "rate_limiter"),

    REWRITE(30, 0, "rewrite"),

    REDIRECT(40, 0, "redirect"),

    DIVIDE(50, 0, "divide"),

    WEB_SOCKET(51, 0, "webSocket"),

    DUBBO(60, 0, "dubbo"),

    SPRING_CLOUD(70, 0, "springCloud"),

    BREAKER(75, 0, "breaker"),

    MONITOR(80, 0, "monitor"),

    RESPONSE(100, 0, "response");

    private final int code;

    private final int role;

    private final String name;

    public static PluginEnum getPluginEnumByName(final String name) {
        return Arrays.stream(PluginEnum.values())
                .filter(pluginEnum -> pluginEnum.getName().equals(name))
                .findFirst().orElse(PluginEnum.GLOBAL);
    }
}
