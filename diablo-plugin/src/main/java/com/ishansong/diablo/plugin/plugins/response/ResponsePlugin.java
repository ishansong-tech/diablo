package com.ishansong.diablo.plugin.plugins.response;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.google.common.base.Strings;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.PluginEnum;
import com.ishansong.diablo.core.enums.PluginTypeEnum;
import com.ishansong.diablo.core.enums.RpcTypeEnum;
import com.ishansong.diablo.core.model.DiabloResult;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.core.utils.JsonUtils;
import com.ishansong.diablo.plugin.plugins.DiabloPlugin;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

@Slf4j(topic = "requestTraceLogger")
public class ResponsePlugin implements DiabloPlugin {

    @Override
    public Mono<Void> execute(final ServerWebExchange exchange, final DiabloPluginChain chain) {

        return chain.execute(exchange).then(Mono.defer(() -> {

            final RequestDTO requestDTO = exchange.getAttribute(Constants.REQUESTDTO);

            ServerHttpResponse response = exchange.getResponse();

            String traceId = exchange.getAttribute(Constants.CLIENT_RESPONSE_TRACE_ID);

            if (RpcTypeEnum.DUBBO.getName().equals(requestDTO.getRpcType())) {

                Object result = exchange.getAttribute(Constants.DUBBO_RPC_RESULT);

                if (Objects.isNull(result)) {

                    Object dubboParams = exchange.getAttribute(Constants.DUBBO_PARAMS);

                    Object interfaceName = exchange.getAttribute(Constants.GATEWAY_CONTEXT_API_NAME);
                    log.warn("ResponsePlugin dubbo result is empty, traceId:{}, interfaceName:{}, dubboParams:{}", traceId, interfaceName, dubboParams);

                    reportedTransaction(requestDTO, Constants.TRANSACTION_TYPE_DUBBO_RESPONSE_EMPTY, Optional.ofNullable(interfaceName).map(Object::toString).orElse("--"));

                    return response.writeWith(Mono.just(response
                            .bufferFactory()
                            .wrap(JsonUtils.toJson(DiabloResult.error(Strings.isNullOrEmpty(exchange.getAttribute(Constants.DUBBO_RPC_EXCEPTION))? Constants.DUBBO_ERROR_RESULT : exchange.getAttribute(Constants.DUBBO_RPC_EXCEPTION))).getBytes())));
                }

                return response.writeWith(Mono.just(response.bufferFactory().wrap(JsonUtils.dubboResultJson(result).getBytes())));
            }

            ClientResponse clientResponse = exchange.getAttribute(Constants.CLIENT_RESPONSE_ATTR);

            if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                final String result = JsonUtils.toJson(DiabloResult.error(HttpStatus.SERVICE_UNAVAILABLE.value(), Constants.UPSTREAM_NOT_FIND));

                String uri = exchange.getRequest().getURI().getPath();
                log.error("ResponsePlugin clientResponse UPSTREAM_NOT_FIND, uri:{}, params:{},traceId:{}, host:{}", uri, exchange.getRequest().getQueryParams(), traceId, exchange.getRequest().getHeaders().getFirst("Host"));

                reportedTransaction(requestDTO, String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()), uri);

                return response.writeWith(Mono.just(exchange.getResponse()
                                                            .bufferFactory().wrap(Objects.requireNonNull(result).getBytes())));
            } else if (Objects.isNull(clientResponse) ||
                    response.getStatusCode() == HttpStatus.BAD_GATEWAY
                    || response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                final String result = JsonUtils.toJson(DiabloResult.error(Constants.HTTP_ERROR_RESULT));

                String uri = exchange.getRequest().getURI().getPath();
                String httpStatus = Optional.ofNullable(clientResponse).map(ClientResponse::statusCode).map(HttpStatus::value).map(String::valueOf).orElse(traceId);
                log.error("ResponsePlugin clientResponse result fail, traceId: {}, uri: {}, httpStatus: {}, params: {},clientResponse isnull: {},response.getStatusCode: {}, host: {}",
                        traceId,
                        uri,
                        httpStatus,
                        exchange.getRequest().getQueryParams(),
                        Objects.isNull(clientResponse),
                        Objects.isNull(clientResponse) ? "-" : response.getStatusCode(), exchange.getRequest().getHeaders().getFirst("Host")
                );

                if (Objects.isNull(clientResponse)) {
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

                    reportedTransaction(requestDTO, Constants.TRANSACTION_TYPE_RESPONSE_NULL, "ResponseNull-" + httpStatus, uri);
                } else if (response.getStatusCode() == null) {
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

                    reportedTransaction(requestDTO, Constants.TRANSACTION_TYPE_RESPONSE_NULL, "StatusNull-" + httpStatus, uri);
                } else {
                    reportedTransaction(requestDTO, httpStatus, uri);
                }


                return response.writeWith(Mono.just(exchange.getResponse()
                                                            .bufferFactory()
                                                            .wrap(Objects.requireNonNull(result).getBytes())));
            } else if (response.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
                final String result = JsonUtils.toJson(DiabloResult.timeout(Constants.TIMEOUT_RESULT));

                String uri = exchange.getRequest().getURI().getPath();

                log.error("ResponsePlugin clientResponse Gateway Timeout, traceId:{}, uri:{}, params:{}", traceId, uri, exchange.getRequest().getQueryParams());
                reportedTransaction(requestDTO, String.valueOf(HttpStatus.GATEWAY_TIMEOUT.value()), uri);

                return response.writeWith(Mono.just(exchange.getResponse()
                                                            .bufferFactory().wrap(Objects.requireNonNull(result).getBytes())));
            } else {
                // other httpStatus
                String httpStatus = Optional.ofNullable(response.getStatusCode()).map(HttpStatus::value).map(String::valueOf).orElse("-");
                String name;
                if (response.getStatusCode() != null && response.getStatusCode().value() < 500) {
                    name = Transaction.SUCCESS;

                    if (response.getStatusCode().value() >= 400) {
                        log.warn("ResponsePlugin clientResponse other status warning, traceId:{}, uri:{}, params:{}, host:{}, httpStatus:{}", traceId, exchange.getRequest().getURI().getPath(), exchange.getRequest().getQueryParams(), exchange.getRequest().getHeaders().getFirst("Host"), httpStatus);
                    }
                } else {
                    name = exchange.getRequest().getURI().getPath();

                    log.error("ResponsePlugin clientResponse other status, traceId:{}, uri:{}, params:{}, host:{}, httpStatus:{}", traceId, name, exchange.getRequest().getQueryParams(), exchange.getRequest().getHeaders().getFirst("Host"), httpStatus);
                }
                reportedTransaction(requestDTO, httpStatus, name);
            }


            return response.writeWith(clientResponse.body(BodyExtractors.toDataBuffers()));
        }));
    }

    private void cleanUp(ServerWebExchange exchange) {

    }

    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.LAST;
    }

    @Override
    public int getOrder() {
        return PluginEnum.RESPONSE.getCode();
    }

    @Override
    public String named() {
        return PluginEnum.RESPONSE.getName();
    }


    private void reportedTransaction(RequestDTO requestDTO, String type, String name, String cause) {
        DefaultTransaction transaction = (DefaultTransaction) Cat.newTransaction(type, name);
        transaction.setDurationStart(Optional.ofNullable(requestDTO).map(RequestDTO::getDurationStart).orElse(System.nanoTime()));

        transaction.setStatus(cause);
        transaction.complete();
    }

    private void reportedTransaction(RequestDTO requestDTO, String name, String cause) {

        reportedTransaction(requestDTO, Constants.TRANSACTION_TYPE_RESPONSE_STATUS, "status_" + name, cause);
    }
}
