package com.ishansong.diablo.admin.entity;

import com.ishansong.diablo.admin.dto.DubboResourceDTO;
import com.ishansong.diablo.core.utils.GsonUtils;
import com.ishansong.diablo.core.utils.UUIDUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

@Data
public class DubboResourceDO extends BaseDO {

    private String key;

    private String name;

    private String serviceName;

    private String namespace;

    private String method;

    /**
     * json array
     *
     * @see com.ishansong.diablo.core.model.dubbo.ParamMetasData
     */
    private String paramMetas;

    /**
     * @see com.ishansong.diablo.core.model.dubbo.DubboExtConfig
     */
    private String extConfig;

    private String apiConfig;

    private Boolean enabled;

    /**
     * @see com.ishansong.diablo.core.enums.ParamObjectType
     */
    private Integer objectType;

    /**
     * 1：QueryString，2：RequestBody
     */
    private Integer paramSourceType;

    /**
     * 1: GET 2: POST
     */
    private Integer httpRequestType;

    private String owner;

    public DubboResourceDO() {
    }

    public DubboResourceDO(String key, String name, String serviceName, String namespace, String method, Boolean enabled, Integer objectType, Integer paramSourceType, Integer httpRequestType, String owner) {
        this.key = key;
        this.name = name;
        this.serviceName = serviceName;
        this.namespace = namespace;
        this.method = method;
        this.enabled = enabled;
        this.objectType = objectType;
        this.paramSourceType = paramSourceType;
        this.httpRequestType = httpRequestType;
        this.owner = owner;
    }

    public static DubboResourceDO buildDubboResourceDO(DubboResourceDTO dto) {

        DubboResourceDO resource = new DubboResourceDO(dto.getKey(), dto.getName(), dto.getServiceName(), dto.getNamespace(), dto.getMethod(), dto.getEnabled(),
                dto.getObjectType(), dto.getParamSourceType(), dto.getHttpRequestType(), dto.getOwner());

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (StringUtils.isEmpty(dto.getId())) {
            resource.setId(UUIDUtils.generateShortUuid());
            resource.setDateCreated(timestamp);
        } else {
            resource.setId(dto.getId());
        }

        resource.setParamMetas(GsonUtils.getInstance().toJson(dto.getParamMetas()));
        resource.setExtConfig(GsonUtils.getInstance().toJson(dto.getDubboExtConfig()));
        resource.setApiConfig(GsonUtils.getInstance().toJson(dto.getApiConfig()));
        resource.setDateUpdated(timestamp);

        return resource;
    }
}
