package com.ishansong.diablo.web.filter;

import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.HttpMethodEnum;
import com.ishansong.diablo.core.enums.RpcTypeEnum;
import com.ishansong.diablo.core.model.DiabloResult;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.core.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class ParamWebFilter extends AbstractWebFilter {

    @Override
    protected Mono<Boolean> doFilter(final ServerWebExchange exchange, final WebFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        final RequestDTO requestDTO = RequestDTO.transform(request);
        if (verify(requestDTO, exchange)) {
            exchange.getAttributes().put(Constants.REQUESTDTO, requestDTO);
            exchange.getAttributes().put(Constants.DUBBO_TOKEN_RPC_TIMEOUT, 1000);
        } else {
            return Mono.just(false);
        }
        return Mono.just(true);
    }

    @Override
    protected Mono<Void> doDenyResponse(final ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        final DiabloResult result = DiabloResult.error("you param is error please check with doc!");
        return response.writeWith(Mono.just(response.bufferFactory()
                                                    .wrap(GsonUtils.getInstance().toJson(result).getBytes(Charset.forName(StandardCharsets.UTF_8.name())))));
    }

    private Boolean verify(final RequestDTO requestDTO, final ServerWebExchange exchange) {
        if (Objects.isNull(requestDTO)) {
            return false;
        }
        final RpcTypeEnum rpcTypeEnum = RpcTypeEnum.acquireByName(requestDTO.getRpcType());
        if (Objects.isNull(rpcTypeEnum)) {
            return false;
        }

        HttpMethodEnum httpMethodEnum = HttpMethodEnum.acquireByName(requestDTO.getHttpMethod());
        if (httpMethodEnum == null) {

            log.error("ParamWebFilter doFilter verify warning, uri:{}, httpMethod:{}", exchange.getRequest().getURI().getPath(), requestDTO.getHttpMethod());

            return false;
        }

        return true;
    }

}
