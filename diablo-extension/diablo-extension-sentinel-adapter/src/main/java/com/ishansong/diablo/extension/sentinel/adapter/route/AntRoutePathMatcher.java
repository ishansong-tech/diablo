package com.ishansong.diablo.extension.sentinel.adapter.route;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;

public class AntRoutePathMatcher implements Predicate<ServerWebExchange> {

    private final String pattern;

    private final PathMatcher pathMatcher;
    private final boolean canMatch;

    public AntRoutePathMatcher(String pattern) {
        AssertUtil.assertNotBlank(pattern, "pattern cannot be blank");
        this.pattern = pattern;
        this.pathMatcher = new AntPathMatcher();
        this.canMatch = pathMatcher.isPattern(pattern);
    }

    @Override
    public boolean test(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        if (canMatch) {
            return pathMatcher.match(pattern, path);
        }
        return false;
    }

    public String getPattern() {
        return pattern;
    }
}
