package com.ishansong.diablo.plugin.plugins.token;

import com.alicp.jetcache.Cache;
import com.ishansong.diablo.plugin.plugins.dubbo.DubboProxyService;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.model.dubbo.DubboResourceStream;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.core.model.selector.DubboSelectorHandle;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class TokenAuthSDKService {

    private final Cache<String, Object> tokenUserIdGatewayCache;

    public TokenAuthSDKService(Cache<String, Object> tokenUserIdGatewayCache) {
        this.tokenUserIdGatewayCache = tokenUserIdGatewayCache;
    }

    public Mono<Void> checkToken(ServerWebExchange exchange, DubboProxyService dubboProxyService, DubboResourceStream dubboMappingResource, Pair pair, DubboSelectorHandle selectorHandle, DiabloPluginChain chain) {

        RequestDTO requestDTO = (RequestDTO) exchange.getAttributes().get(Constants.REQUESTDTO);

        if (Strings.isEmpty(requestDTO.getToken()) || Strings.isEmpty(requestDTO.getClientId())) {
            log.warn("token or clientId is null, requestDTO:{}",requestDTO.toString());
            return Mono.error(new RuntimeException("Diablo token or ClientId is null"));

        }
        TokenCommand dubboToeknCommand = new TokenCommand(exchange,tokenUserIdGatewayCache, chain);
        return dubboToeknCommand.doInvoke();

    }

}