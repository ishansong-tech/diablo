package com.ishansong.diablo.plugin.plugins;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface DiabloPluginChain {

    Mono<Void> execute(ServerWebExchange exchange);

}
