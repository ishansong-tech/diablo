package com.ishansong.diablo.plugin.plugins.token;

import com.alibaba.dubbo.rpc.RpcResult;
import com.alicp.jetcache.Cache;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.exception.DiabloException;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.core.utils.Md5Utils;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TokenCommand {

    private final ServerWebExchange exchange;

    private final Cache<String, Object> tokenUserIdGatewayCache;

    private final DiabloPluginChain chain;

    public TokenCommand(ServerWebExchange exchange, final Cache<String, Object> tokenUserIdGatewayCache, DiabloPluginChain chain) {
        this.exchange = exchange;
        this.tokenUserIdGatewayCache = tokenUserIdGatewayCache;
        this.chain=chain;
    }

    public Mono<Void> doInvoke() {

        try {
            return Mono.create(c -> {
                RequestDTO requestDTO = (RequestDTO) exchange.getAttributes().get(Constants.REQUESTDTO);
                Object hashMapRpcResult = tokenUserIdGatewayCache.get(Md5Utils.md5(requestDTO.getToken()));
                c.success(hashMapRpcResult);
            }).doOnError(e -> log.error("TokenCommand doInvoke async invoke fail, ", e))
                    .flatMap(r -> {
                        RequestDTO requestDTO = ((RequestDTO) exchange.getAttributes().get(Constants.REQUESTDTO));
                        if (Objects.nonNull(r) && r instanceof RpcResult) {
                            if(Objects.nonNull(((RpcResult) r).getValue())){
                                Map tokenRes = (HashMap) ((RpcResult) r).getValue();
                                //fail 0.1
                                if (!Objects.equals(tokenRes.get("status"), HttpStatus.OK.value()) || Objects.isNull(tokenRes.get("data"))) {
                                    log.warn("TokenCommand doInvoke async invoke status fail or data is null,token:{},clientId:{},status:{},err:{},data:{},exception:{}",requestDTO.getToken(),requestDTO.getClientId(),tokenRes.get("status"),tokenRes.get("err"),tokenRes.get("data"),tokenRes.get("exception"));
                                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                    return Mono.error(new RuntimeException("Diablo check token fail,"+tokenRes.get("err")));
                                }
                                Map user = (HashMap) tokenRes.get("data");
                                exchange.getAttributes().put(Constants.DIABLOUSERID, (Long) user.get("userId"));
                                tokenUserIdGatewayCache.put(Md5Utils.md5(requestDTO.getToken()), user, (Long) user.get("expiresIn") + 60*5, TimeUnit.SECONDS);
                            }else {
                                //fail 0.2
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return  Mono.error(new RuntimeException("Diablo check token fail,data is null"));
                            }
                        }else if(Objects.nonNull(r) && r instanceof HashMap){
                            //get redis cache
                            Map user = (HashMap) r;
                            if(Objects.nonNull(user)){
                                exchange.getAttributes().put(Constants.DIABLOUSERID, (Long) user.get("userId"));
                            }
                        }

                        return chain.execute(exchange);
                    });
        } catch (Exception e) {
            throw new DiabloException(e.getMessage());
        }

    }
}
