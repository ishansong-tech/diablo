package com.ishansong.diablo.extension.sentinel.adapter.callback;

import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface BlockRequestHandler {

    Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t);
}
