package com.ishansong.diablo.core.enums;

import com.ishansong.diablo.core.exception.DiabloException;

import java.util.Arrays;
import java.util.Objects;

public enum DataEventTypeEnum {

    DELETE,

    CREATE,

    UPDATE,

    REFRESH,

    MYSELF;

    public static DataEventTypeEnum acquireByName(final String name) {
        return Arrays.stream(DataEventTypeEnum.values())
                .filter(e -> Objects.equals(e.name(), name))
                .findFirst().orElseThrow(() -> new DiabloException(" this DataEventTypeEnum can not support!"));
    }


}
