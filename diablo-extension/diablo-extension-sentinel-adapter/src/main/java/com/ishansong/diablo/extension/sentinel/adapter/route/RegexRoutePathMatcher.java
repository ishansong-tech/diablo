package com.ishansong.diablo.extension.sentinel.adapter.route;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import org.springframework.web.server.ServerWebExchange;

import java.util.regex.Pattern;

public class RegexRoutePathMatcher implements Predicate<ServerWebExchange> {

    private final String pattern;
    private final Pattern regex;

    public RegexRoutePathMatcher(String pattern) {
        AssertUtil.assertNotBlank(pattern, "pattern cannot be blank");
        this.pattern = pattern;
        this.regex = Pattern.compile(pattern);
    }

    @Override
    public boolean test(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        return regex.matcher(path).matches();
    }

    public String getPattern() {
        return pattern;
    }
}
