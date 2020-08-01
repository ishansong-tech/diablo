package com.ishansong.diablo.plugin.limiter;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayParamParser;
import com.alibaba.csp.sentinel.adapter.reactor.EntryConfig;
import com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorTransformer;
import com.google.common.base.Strings;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.PluginEnum;
import com.ishansong.diablo.core.enums.PluginTypeEnum;
import com.ishansong.diablo.core.model.dubbo.DubboResourceStream;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import com.ishansong.diablo.core.utils.UrlUtils;
import com.ishansong.diablo.extension.sentinel.adapter.ServerWebExchangeItemParser;
import com.ishansong.diablo.extension.sentinel.adapter.api.DiabloApiMatcherManager;
import com.ishansong.diablo.extension.sentinel.adapter.api.matcher.WebExchangeApiMatcher;
import com.ishansong.diablo.plugin.plugins.AbstractDiabloPlugin;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

public class LimiterPlugin extends AbstractDiabloPlugin {

    private final GatewayParamParser<ServerWebExchange> paramParser = new GatewayParamParser<ServerWebExchange>(new ServerWebExchangeItemParser());

    private Boolean sentinelEnable=false;

    public LimiterPlugin(LocalCacheManager localCacheManager) {
        super(localCacheManager);
    }

    @Override
    protected Mono<Void> doExecute(ServerWebExchange exchange, DiabloPluginChain chain, SelectorData selector, RuleData rule) {


        Mono<Void> asyncResult = chain.execute(exchange);

        Set<String> matchingApis = pickMatchingApiDefinitions(exchange);

        // monitor high-frequency api & configs
        String apiName;
        if (CollectionUtils.isEmpty(matchingApis)) {
            if (Boolean.TRUE.equals(sentinelEnable)) {
                apiName = exchange.getRequest().getURI().getPath();
            } else {
                return asyncResult;
            }
        } else {
            apiName = matchingApis.iterator().next();
        }

        String monitorApiName = apiName;
        String resourceKey = exchange.getRequest().getHeaders().getFirst(Constants.RESOURCE_KEY);
        resourceKey = (Strings.isNullOrEmpty(resourceKey) ? UrlUtils.getQueryString(exchange.getRequest().getURI().getQuery(),Constants.RESOURCE_KEY) : resourceKey);
        if (!Strings.isNullOrEmpty(resourceKey)) {
            DubboResourceStream resource = localCacheManager.findDubbResource(resourceKey);
            if (resource != null) {
                String fullInterfaceName = resource.getNamespace() + "." + resource.getMethod();

                exchange.getAttributes().put(Constants.DUBBO_CALL_SERVICE_NAME, resource.getServiceName());

                monitorApiName = fullInterfaceName;

                // 区分调用来源
                apiName = resource.getServiceName() + "#" + resourceKey;
            }
        }

        exchange.getAttributes().put(Constants.GATEWAY_CONTEXT_API_NAME, monitorApiName);

        // 测试resourceId和 customApiName
        Object[] apiParams = paramParser.parseParameterFor(apiName, exchange, r -> r.getResourceMode() == Constants.RESOURCE_MODE_CUSTOM_API_NAME || r.getResourceMode() == Constants.RESOURCE_MODE_ROUTE_ID);
        asyncResult = asyncResult.transform(new SentinelReactorTransformer<>(new EntryConfig(apiName, EntryType.IN, 1, apiParams)));

        return asyncResult;
    }

    private Set<String> pickMatchingApiDefinitions(ServerWebExchange exchange) {
        return DiabloApiMatcherManager.getApiMatcherMap().values()
                .stream()
                .filter(m -> m.test(exchange))
                .map(WebExchangeApiMatcher::getApiName)
                .collect(Collectors.toSet());
    }

    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.BEFORE;
    }

    @Override
    public int getOrder() {
        return PluginEnum.RATE_LIMITER.getCode();
    }

    @Override
    public String named() {
        return PluginEnum.RATE_LIMITER.getName();
    }
}
