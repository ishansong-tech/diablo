package com.ishansong.diablo.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ResultEnum {

    SUCCESS("success"),

    TIME_OUT("timeOut"),

    ERROR("error"),;

    private final String name;


}
