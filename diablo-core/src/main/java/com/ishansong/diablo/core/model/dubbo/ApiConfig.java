package com.ishansong.diablo.core.model.dubbo;

import lombok.Data;

import java.util.List;

@Data
public class ApiConfig {

    /**
     * 登录token验证
     */
    private Boolean token;

    /**
     * 接口签名验证
     */
    private Boolean sign;

    /**
     * 请求的origin值 或 *, *表示任意域名请求
     */
    private String corsOrigin;

    /**
     * 是否允许发送cookie
     */
    private Boolean corsCredentials;

    // 单位(s)
    private Long corsMaxAge;

    /**
     * 接口指定域名访问,防止内部接口,外网域名访问到
     */
    private List<String> allowDomain;

    /**
     * 指定前缀则进行缓存
     */
    private String redisPrefix;

    /**
     * redis缓存有效期
     */
    private Long redisExpire;

    /**
     * 指定哪些key需要作为redis key唯一
     */
    private List<String> redisKeyName;

}
