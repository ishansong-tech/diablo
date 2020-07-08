package com.ishansong.diablo.core.model.dubbo;

import lombok.Data;

@Data
public class DubboExtConfig {

    /**
     * 服务分组,接口多实现区分,需和提供方一致
     */
    private String group;

    /**
     * 版本号,需和提供方一致
     */
    private String version;

    /**
     * 负载均衡策略  random,roundrobin,leastactive，分别表示：随机，轮询，最少活跃调用
     */
    private String loadbalance;

    /**
     * 超时时间(毫秒)
     */
    private Integer timeout;

    /**
     * 重试次数,不需要重试设置0
     */
    private Integer retries = 0;

    /**
     * 2.6 异步
     */
    private Boolean async;

    /**
     * 2.7 callback异步调用,不阻塞当前线程, 需要提供者服务升级dubbo2.7以上
     */
    private Boolean callbackAsync;

    /**
     * 检查提供者是否存在
     */
    private Boolean check;

    /**
     * 服务调用分层标签
     */
    private String layer;


}
