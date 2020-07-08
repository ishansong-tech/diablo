package com.ishansong.diablo.web.filter;

import com.google.common.base.Strings;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.RpcTypeEnum;
import com.ishansong.diablo.core.utils.UrlUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class BodyWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String queryString = request.getURI().getQuery();
        String rpcType = Optional.ofNullable((Strings.isNullOrEmpty(request.getHeaders().getFirst(Constants.RPC_TYPE)) ? UrlUtils.getQueryString(queryString,Constants.RPC_TYPE) : request.getHeaders().getFirst(Constants.RPC_TYPE))).orElse("http");

        if (RpcTypeEnum.DUBBO.getName().equals(rpcType)
                && MediaType.APPLICATION_JSON.isCompatibleWith(request.getHeaders().getContentType())
                && HttpMethod.POST.equals(request.getMethod())) {

            DefaultServerRequest serverRequest = new DefaultServerRequest(exchange);

            return serverRequest.bodyToMono(String.class).flatMap(t -> {

                exchange.getAttributes().put(Constants.DUBBO_PARAMS, t);

                return chain.filter(exchange);
            });
        }

        return chain.filter(exchange);
    }
}
