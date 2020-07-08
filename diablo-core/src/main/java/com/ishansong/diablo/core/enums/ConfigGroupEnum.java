package com.ishansong.diablo.core.enums;

import com.ishansong.diablo.core.exception.DiabloException;

import java.util.Arrays;
import java.util.Objects;

public enum ConfigGroupEnum {

    PLUGIN,

    RULE,

    SELECTOR,

    DUBBO_MAPPING
    ;

    public static ConfigGroupEnum acquireByName(final String name) {
        return Arrays.stream(ConfigGroupEnum.values())
                .filter(e -> Objects.equals(e.name(), name))
                .findFirst().orElseThrow(() -> new DiabloException(" this ConfigGroupEnum can not support!"));
    }


}
