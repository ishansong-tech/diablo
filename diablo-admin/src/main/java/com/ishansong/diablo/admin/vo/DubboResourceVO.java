package com.ishansong.diablo.admin.vo;

import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import com.ishansong.diablo.admin.entity.DubboResourceDO;
import com.ishansong.diablo.core.enums.HttpRequestType;
import com.ishansong.diablo.core.enums.ParamObjectType;
import com.ishansong.diablo.core.enums.ParamSourceType;
import com.ishansong.diablo.core.model.dubbo.ApiConfig;
import com.ishansong.diablo.core.model.dubbo.DubboExtConfig;
import com.ishansong.diablo.core.utils.GsonUtils;
import lombok.Data;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class DubboResourceVO {

    private String id;

    private String resourceKey;

    private String name;

    /**
     * 示例: order-service
     */
    private String serviceName;

    private String namespace;

    private String method;

    /**
     * json array
     *
     * @see ParamMetasVO
     */
    private List<ParamMetasVO> paramMetas;

    private DubboExtConfig dubboExtConfig;

    private ApiConfig apiConfig;

    /**
     * @see com.ishansong.diablo.core.enums.ParamObjectType
     */
    private String objectType;

    /**
     * 1：QueryString，2：RequestBody
     */
    private String paramSourceType;

    /**
     * 1: GET 2: POST
     */
    private String httpRequestType;

    private Boolean enabled;

    private String owner;

    /**
     * created time.
     */
    private String dateCreated;

    /**
     * updated time.
     */
    private String dateUpdated;

    public DubboResourceVO() {
    }

    public DubboResourceVO(String id, String resourceKey, String name, String serviceName, String namespace, String method, Boolean enabled, String owner) {
        this.id = id;
        this.resourceKey = resourceKey;
        this.name = name;
        this.serviceName = serviceName;
        this.namespace = namespace;
        this.method = method;
        this.enabled = enabled;
        this.owner = owner;
    }

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

    private static final Type paramMetasType = new TypeToken<List<ParamMetasVO>>() {
    }.getType();

    private static final Type extConfigType = new TypeToken<DubboExtConfig>() {
    }.getType();

    private static final Type apiConfigType = new TypeToken<ApiConfig>() {
    }.getType();

    public static DubboResourceVO buildDubboResourceVO(DubboResourceDO resource) {

        DubboResourceVO dubboResourceVO = new DubboResourceVO(resource.getId(), resource.getKey(), resource.getName(), resource.getServiceName(), resource.getNamespace(), resource.getMethod(), resource.getEnabled(), resource.getOwner());
        dubboResourceVO.setObjectType(ParamObjectType.acquireName(resource.getObjectType()));
        dubboResourceVO.setHttpRequestType(HttpRequestType.acquireName(resource.getHttpRequestType()));
        dubboResourceVO.setParamSourceType(ParamSourceType.acquireName(resource.getParamSourceType()));
        dubboResourceVO.setParamMetas(GsonUtils.getInstance().fromJson(resource.getParamMetas(), paramMetasType));

        String extConfig = resource.getExtConfig();
        if (!Strings.isNullOrEmpty(extConfig)) {
            dubboResourceVO.setDubboExtConfig(GsonUtils.getInstance().fromJson(extConfig, extConfigType));
        }

        String apiConfig = resource.getApiConfig();
        if (!Strings.isNullOrEmpty(apiConfig)) {
            dubboResourceVO.setApiConfig(GsonUtils.getInstance().fromJson(apiConfig, apiConfigType));
        }

        dubboResourceVO.setDateCreated(dateTimeFormatter.format(resource.getDateCreated().toLocalDateTime()));
        dubboResourceVO.setDateUpdated(dateTimeFormatter.format(resource.getDateUpdated().toLocalDateTime()));

        return dubboResourceVO;
    }

}
