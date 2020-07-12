package com.ishansong.diablo.admin.dto;

import com.ishansong.diablo.admin.vo.ParamMetasVO;
import com.ishansong.diablo.core.model.dubbo.ApiConfig;
import com.ishansong.diablo.core.model.dubbo.DubboExtConfig;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DubboResourceDTO {

    private String id;

    @NotBlank(message = "资源key不能为空")
    private String key;

    @NotBlank(message = "资源名称不能为空")
    private String name;

    /**
     * 示例: order-service
     */
    @NotBlank(message = "服务名称不能为空")
    private String serviceName;

    @NotBlank(message = "接口名称不能为空")
    private String namespace;

    @NotBlank(message = "接口方法不能为空")
    private String method;

    /**
     * json array
     *
     * @see com.ishansong.diablo.core.model.dubbo.ParamMetasData
     */
    @NotNull(message = "参数元信息不能为空")
    private List<ParamMetasVO> paramMetas;

    /**
     * @see com.ishansong.diablo.core.model.dubbo.DubboExtConfig
     */
    @NotNull(message = "dubbo方法属性不能为空")
    private DubboExtConfig dubboExtConfig;

    /**
     * @see com.ishansong.diablo.core.model.dubbo.ApiConfig
     */
    @NotNull(message = "api属性不能为空")
    private ApiConfig apiConfig;

    private Boolean enabled = false;

    /**
     * @see com.ishansong.diablo.core.enums.ParamObjectType
     */
    @NotNull(message = "对象参数类型不能为空")
    private Integer objectType;

    /**
     * 1：QueryString，2：RequestBody
     */
    @NotNull(message = "参数来源不能为空")
    private Integer paramSourceType;

    /**
     * 1: GET 2: POST
     */
    @NotNull(message = "请求类型不能为空")
    private Integer httpRequestType;

    private String owner;

    String allowDomainStr;

    String redisKeyNameStr;
}
