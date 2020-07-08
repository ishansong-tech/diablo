package com.ishansong.diablo.web.handler;

import com.dianping.cat.Cat;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.core.model.rule.FailbackData;
import com.ishansong.diablo.core.utils.GenerateTraceIdUtil;
import com.ishansong.diablo.plugin.plugins.DiabloPlugin;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

import java.net.SocketException;
import java.util.List;
import java.util.Optional;

@Slf4j(topic = "requestTraceLogger")
public final class DiabloWebHandler implements WebHandler {

    private volatile boolean started = false;

    private static final Logger logger = LoggerFactory.getLogger(DiabloWebHandler.class);

    private static final String TID = "Tid";

    private List<DiabloPlugin> plugins;
    private FailbackReporter failbackReporter;

    public DiabloWebHandler(final List<DiabloPlugin> plugins, final FailbackReporter failbackReporter) {
        this.plugins = plugins;
        this.failbackReporter = failbackReporter;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initAfterStartup() {

        try {
            // 预设加载10s
            Thread.sleep(10000);

            started = true;
        } catch (InterruptedException e) {
            // ingore
        }

        logger.info("DiabloWebHandler ApplicationReadyEvent initAfterStartup started={}", started);
    }

    @Override
    public Mono<Void> handle(final ServerWebExchange exchange) {

        if ("/health".equals(exchange.getRequest().getPath().value())) {
            return Mono.defer(() -> {

                String enable = exchange.getRequest().getQueryParams().getFirst("enable");

                if (!Strings.isNullOrEmpty(enable)) {
                    started = Optional.of(enable).map(Boolean::valueOf).orElse(false);

                    return Mono.empty();
                }

                if (!started) {

                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                }

                return Mono.empty();
            });
        } else if ("/favicon.ico".equals(exchange.getRequest().getPath().value())) {
            return Mono.empty();
        }

        String traceId = exchange.getRequest().getHeaders().getFirst(TID);
        if (Strings.isNullOrEmpty(traceId)) {
            traceId = GenerateTraceIdUtil.INSTANCE.getRequestTraceId(exchange.getRequest());
        }

        exchange.getAttributes().put(Constants.CLIENT_RESPONSE_TRACE_ID, traceId);

        return new DefaultDiabloPluginChain(plugins)
                .execute(exchange)
                .doOnError(r -> {

                    RequestDTO requestDTO = exchange.getAttribute(Constants.REQUESTDTO);

                    DefaultTransaction transaction = (DefaultTransaction) Cat.newTransaction(Constants.TRANSACTION_TYPE_RESPONSE_STATUS, r.getClass().getCanonicalName());
                    transaction.setDurationStart(Optional.ofNullable(requestDTO).map(RequestDTO::getDurationStart).orElse(System.nanoTime()));

                    String trace = Throwables.getStackTraceAsString(r);
                    transaction.setStatus(trace);
                    transaction.complete();

                    if (r instanceof SocketException) {

                        String ruleId = exchange.getAttribute(Constants.GATEWAY_CONTEXT_RULE_ID);
                        String upstreamHost = exchange.getAttribute(Constants.GATEWAY_CONTEXT_UPSTREAM_HOST);
                        failbackReporter.report(FailbackData.builder().ruleId(ruleId).upstreamHost(upstreamHost).build());

                        log.warn("DiabloWebHandler.handle failback report, uri: {}, ruleId:{}, upstreamHost:{}, doOnError:{}", exchange.getRequest().getURI().getPath(), ruleId, upstreamHost, trace);
                    }

                    log.error("DiabloWebHandler.handle, uri: {} doOnError:{}", exchange.getRequest().getURI().getPath(), trace);
                });
//                .doFinally(f -> {
//                    // 后续有时间确认 没有清除掉
//                    IssRpcContext.clean();
//                });
    }

    private static class DefaultDiabloPluginChain implements DiabloPluginChain {

        private int index;

        private final List<DiabloPlugin> plugins;

        DefaultDiabloPluginChain(final List<DiabloPlugin> plugins) {
            this.plugins = plugins;
        }

        @Override
        public Mono<Void> execute(final ServerWebExchange exchange) {

            if (this.index < plugins.size()) {
                DiabloPlugin plugin = plugins.get(this.index++);
                try {
                    return plugin.execute(exchange, this);
                } catch (Exception ex) {
                    log.error("DefaultDiabloPluginChain.execute, traceId: {}, uri: {}, error:{}", exchange.getAttribute(Constants.CLIENT_RESPONSE_TRACE_ID), exchange.getRequest().getURI().getPath(), Throwables.getStackTraceAsString(ex));

                    throw ex;
                }
            } else {
                return Mono.empty(); // complete
            }
        }
    }
}
