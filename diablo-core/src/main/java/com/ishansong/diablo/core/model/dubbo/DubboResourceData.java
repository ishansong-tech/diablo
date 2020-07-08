package com.ishansong.diablo.core.model.dubbo;

import com.ishansong.diablo.core.enums.ParamObjectType;
import lombok.Data;

import java.util.List;

@Data
public class DubboResourceData {

    private String key;

    private String serviceName;

    private String namespace;

    private String method;

    private Integer objectType;

    private Boolean enabled;

    private List<ParamMetasData> paramMetas;

    private DubboExtConfig dubboExtConfig;

    private ApiConfig apiConfig;

    public DubboResourceData() {
    }

    public DubboResourceData(String key, String serviceName, String namespace, String method, Integer objectType, Boolean enabled) {
        this.key = key;
        this.serviceName = serviceName;
        this.namespace = namespace;
        this.method = method;
        this.objectType = objectType;
        this.enabled = enabled;
    }
}
