package com.ishansong.diablo.plugin.plugins;

import com.ishansong.diablo.core.enums.PluginTypeEnum;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface DiabloPlugin {

    Mono<Void> execute(ServerWebExchange exchange, DiabloPluginChain chain);

    PluginTypeEnum pluginType();

    int getOrder();

    String named();

    default Boolean skip(ServerWebExchange exchange) {
        return false;
    }

}

