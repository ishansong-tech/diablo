package com.ishansong.diablo.extension.sentinel.adapter.route;

import com.alibaba.csp.sentinel.util.function.Predicate;
import org.springframework.web.server.ServerWebExchange;

public final class RouteMatchers {

    public static Predicate<ServerWebExchange> all() {
        return exchange -> true;
    }

    public static Predicate<ServerWebExchange> antPath(String pathPattern) {
        return new AntRoutePathMatcher(pathPattern);
    }

    public static Predicate<ServerWebExchange> exactPath(final String path) {
        return exchange -> exchange.getRequest().getPath().value().equals(path);
    }

    public static Predicate<ServerWebExchange> regexPath(String pathPattern) {
        return new RegexRoutePathMatcher(pathPattern);
    }

    private RouteMatchers() {}
}
