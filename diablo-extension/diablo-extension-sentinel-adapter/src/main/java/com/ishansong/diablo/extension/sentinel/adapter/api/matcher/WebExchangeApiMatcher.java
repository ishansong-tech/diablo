package com.ishansong.diablo.extension.sentinel.adapter.api.matcher;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.matcher.AbstractApiMatcher;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import com.ishansong.diablo.extension.sentinel.adapter.route.RouteMatchers;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

public class WebExchangeApiMatcher extends AbstractApiMatcher<ServerWebExchange> {

    public WebExchangeApiMatcher(ApiDefinition apiDefinition) {
        super(apiDefinition);
    }

    @Override
    protected void initializeMatchers() {
        if (apiDefinition.getPredicateItems() != null) {
            apiDefinition.getPredicateItems().forEach(item ->
                fromApiPredicate(item).ifPresent(matchers::add));
        }
    }

    private Optional<Predicate<ServerWebExchange>> fromApiPredicate(/*@NonNull*/ ApiPredicateItem item) {
        if (item instanceof ApiPathPredicateItem) {
            return fromApiPathPredicate((ApiPathPredicateItem)item);
        }
        return Optional.empty();
    }

    private Optional<Predicate<ServerWebExchange>> fromApiPathPredicate(/*@Valid*/ ApiPathPredicateItem item) {
        String pattern = item.getPattern();
        if (StringUtil.isBlank(pattern)) {
            return Optional.empty();
        }
        switch (item.getMatchStrategy()) {
            case SentinelGatewayConstants.URL_MATCH_STRATEGY_REGEX:
                return Optional.of(RouteMatchers.regexPath(pattern));
            case SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX:
                return Optional.of(RouteMatchers.antPath(pattern));
            default:
                return Optional.of(RouteMatchers.exactPath(pattern));
        }
    }
}
