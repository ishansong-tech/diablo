package com.ishansong.diablo.admin.vo;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import com.ishansong.diablo.admin.entity.DubboResourceDO;
import com.ishansong.diablo.core.enums.ParamObjectType;
import com.ishansong.diablo.core.model.dubbo.ApiConfig;
import com.ishansong.diablo.core.model.dubbo.DubboExtConfig;
import com.ishansong.diablo.core.utils.GsonUtils;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class DubboResourceDetailVO {

    private String id;

    private String resourceKey;

    private String name;

    private String serviceName;

    private String namespace;

    private String method;

    private List<ParamMetasVO> paramMetas;

    private DubboExtConfig dubboExtConfig;

    private ApiConfig apiConfig;

    /**
     * @see ParamObjectType
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

    private Boolean enabled;

    private String owner;

    private String dateCreated;

    private String dateUpdated;

    private String allowDomainStr;

    private String redisKeyNameStr;

    public DubboResourceDetailVO() {
    }

    public DubboResourceDetailVO(String id, String resourceKey, String name, String serviceName, String namespace, String method, Boolean enabled, String owner) {
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

    public static DubboResourceDetailVO buildDubboResourceVO(DubboResourceDO resource) {

        DubboResourceDetailVO dubboResourceVO = new DubboResourceDetailVO(resource.getId(), resource.getKey(), resource.getName(), resource.getServiceName(), resource.getNamespace(), resource.getMethod(), resource.getEnabled(), resource.getOwner());
        dubboResourceVO.setObjectType(resource.getObjectType());
        dubboResourceVO.setHttpRequestType(resource.getHttpRequestType());
        dubboResourceVO.setParamSourceType(resource.getParamSourceType());
        dubboResourceVO.setParamMetas(GsonUtils.getInstance().fromJson(resource.getParamMetas(), paramMetasType));

        String extConfig = resource.getExtConfig();
        if (!Strings.isNullOrEmpty(extConfig)) {
            dubboResourceVO.setDubboExtConfig(GsonUtils.getInstance().fromJson(extConfig, extConfigType));
        }

        String apiConfig = resource.getApiConfig();
        if (!Strings.isNullOrEmpty(apiConfig)) {
            ApiConfig api = GsonUtils.getInstance().fromJson(apiConfig, apiConfigType);
            dubboResourceVO.setApiConfig(api);

            List<String> redisKeyName = api.getRedisKeyName();
            if (!CollectionUtils.isEmpty(redisKeyName)) {
                String redisKeyNameStr = Joiner.on(",").join(redisKeyName);
                dubboResourceVO.setRedisKeyNameStr(redisKeyNameStr);
            }

            List<String> allowDomain = api.getAllowDomain();
            if (!CollectionUtils.isEmpty(allowDomain)) {
                String allowDomainStr = Joiner.on(",").join(allowDomain);
                dubboResourceVO.setAllowDomainStr(allowDomainStr);
            }
        }

        dubboResourceVO.setDateCreated(dateTimeFormatter.format(resource.getDateCreated().toLocalDateTime()));
        dubboResourceVO.setDateUpdated(dateTimeFormatter.format(resource.getDateUpdated().toLocalDateTime()));

        return dubboResourceVO;
    }

}
