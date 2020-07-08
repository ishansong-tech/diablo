package com.ishansong.diablo.plugin.plugins.dubbo;

import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.google.common.base.Throwables;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.exception.CommonErrorCode;
import com.ishansong.diablo.core.exception.DiabloException;
import com.ishansong.diablo.core.model.dubbo.DubboExtConfig;
import com.ishansong.diablo.core.model.dubbo.DubboResourceStream;
import com.ishansong.diablo.core.model.selector.DubboSelectorHandle;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.Optional;

@Slf4j
public class DubboCommand {

    private final ServerWebExchange exchange;

    private final DiabloPluginChain chain;

    private final DubboProxyService dubboProxyService;

    private final DubboResourceStream dubboMappingResource;

    private final Pair<String[], Object[]> pair;

    private final DubboSelectorHandle dubboSelectorHandle;

    public DubboCommand(ServerWebExchange exchange, DiabloPluginChain chain, DubboProxyService dubboProxyService,
                        DubboResourceStream dubboMappingResource, Pair<String[], Object[]> pair, DubboSelectorHandle dubboSelectorHandle) {
        this.exchange = exchange;
        this.chain = chain;
        this.dubboProxyService = dubboProxyService;
        this.dubboMappingResource = dubboMappingResource;
        this.pair = pair;
        this.dubboSelectorHandle = dubboSelectorHandle;
    }

    public Mono<Void> doRpcInvoke() {

        GenericService genericService = dubboProxyService.buildGenericService(dubboMappingResource, dubboSelectorHandle);

        try {

            Boolean async = Optional.ofNullable(dubboMappingResource.getDubboExtConfig()).map(DubboExtConfig::getAsync).orElse(false);

            if (async) {

                return Mono.create(c -> {
                    genericService.$invoke(dubboMappingResource.getMethod(), pair.getLeft(), pair.getRight());
                    ((FutureAdapter) (RpcContext.getContext().getFuture())).getFuture().setCallback(new ResponseCallback() {
                        @Override
                        public void done(Object result) {
                            c.success(result);
                        }

                        @Override
                        public void caught(Throwable exception) {
                            c.error(exception);
                        }
                    });
                }).doOnError(e -> log.error("DubboCommand doRpcInvoke async invoke fail, ", e))
                        .flatMap(r -> {
                               if (Objects.nonNull(r) && r instanceof RpcResult) {
                                   if(Objects.nonNull(((RpcResult) r).getValue())){
                                       exchange.getAttributes().put(Constants.DUBBO_RPC_RESULT, ((RpcResult) r).getValue());
                                   }else {
                                       exchange.getAttributes().put(Constants.DUBBO_RPC_EXCEPTION, Objects.isNull(((RpcResult) r).getException()) ? CommonErrorCode.ERROR_MSG : ((RpcResult) r).getException() .getMessage());
                                   }
                               }

                               return chain.execute(exchange);
                           });

            } else {
                return Mono.create(s -> {
                    Object result = genericService.$invoke(dubboMappingResource.getMethod(), pair.getLeft(), pair.getRight());

                    s.success(result);
                }).subscribeOn(Schedulers.parallel())
                           .doOnError(e -> log.error("DubboCommand doRpcInvoke sync invoke fail, ", e))
                           .flatMap(o -> {
                               if (Objects.nonNull(o)) {
                                   exchange.getAttributes().put(Constants.DUBBO_RPC_RESULT, o);
                               }

                               return chain.execute(exchange);
                           });
            }

        } catch (Exception e) {
            log.error("DubboProxyService genericInvoker $invoke fail, paramMap={}, cause={}", pair, Throwables.getStackTraceAsString(e));

            throw new DiabloException(e.getMessage());
        }

    }
}
