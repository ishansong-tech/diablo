package com.ishansong.diablo.core.enums;

import com.ishansong.diablo.core.exception.DiabloException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum RpcTypeEnum {

    HTTP("http", true),

    DUBBO("dubbo", true),

    SPRING_CLOUD("springCloud", true);

    private final String name;

    private final Boolean support;

    public static List<RpcTypeEnum> acquireSupports() {
        return Arrays.stream(RpcTypeEnum.values())
                .filter(e -> e.support).collect(Collectors.toList());
    }

    public static RpcTypeEnum acquireByName(final String name) {
        return Arrays.stream(RpcTypeEnum.values())
                .filter(e -> e.support && e.name.equals(name)).findFirst()
                .orElseThrow(() -> new DiabloException(" this rpc type can not support!"));
    }
}
