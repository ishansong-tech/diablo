package com.ishansong.diablo.extension.sentinel.adapter;

import com.alibaba.csp.sentinel.adapter.gateway.common.param.RequestItemParser;
import org.springframework.http.HttpCookie;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetSocketAddress;
import java.util.Optional;

public class ServerWebExchangeItemParser implements RequestItemParser<ServerWebExchange> {

    @Override
    public String getPath(ServerWebExchange exchange) {
        return exchange.getRequest().getPath().value();
    }

    @Override
    public String getRemoteAddress(ServerWebExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null) {
            return null;
        }
        return remoteAddress.getAddress().getHostAddress();
    }

    @Override
    public String getHeader(ServerWebExchange exchange, String key) {
        return exchange.getRequest().getHeaders().getFirst(key);
    }

    @Override
    public String getUrlParam(ServerWebExchange exchange, String paramName) {
        return exchange.getRequest().getQueryParams().getFirst(paramName);
    }

    @Override
    public String getCookieValue(ServerWebExchange exchange, String cookieName) {
        return Optional.ofNullable(exchange.getResponse().getCookies().getFirst(cookieName))
            .map(HttpCookie::getValue)
            .orElse(null);
    }
}
