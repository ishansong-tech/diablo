package com.ishansong.diablo.core.model.dubbo;

import com.ishansong.diablo.core.enums.ParamObjectType;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class DubboResourceStream {

    private String serviceName;

    private String namespace;

    private String method;

    private Integer paramObjectType;

    private LinkedHashMap<String, String> paramMetas;

    private DubboExtConfig dubboExtConfig;

    private ApiConfig apiConfig;
}
