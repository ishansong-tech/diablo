package com.ishansong.diablo.plugin.plugins.monitor;

import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayParamParser;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.extension.sentinel.adapter.ServerWebExchangeItemParser;
import com.ishansong.diablo.plugin.cat.reactor.CatConfig;
import com.ishansong.diablo.plugin.cat.reactor.CatReactorTransformer;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.PluginEnum;
import com.ishansong.diablo.core.enums.PluginTypeEnum;
import com.ishansong.diablo.core.model.access.AccessLog;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import com.ishansong.diablo.plugin.plugins.AbstractDiabloPlugin;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import com.ishansong.diablo.plugin.plugins.utils.AccessLogUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

public class MonitorPlugin extends AbstractDiabloPlugin {

    private final GatewayParamParser<ServerWebExchange> paramParser = new GatewayParamParser<ServerWebExchange>(new ServerWebExchangeItemParser());


    private static final Logger logger = LoggerFactory.getLogger(MonitorPlugin.class);

    private final String hostNameSeparator = ".";

    @Value("${diablo.accessLog.enable:true}")
    private Boolean accessLogEnable;

    @Value("${diablo.accessLog.percentagy:100}")
    private Long accessLogPercentagy;

    public MonitorPlugin(LocalCacheManager localCacheManager) {
        super(localCacheManager);
    }

    private AccessLog buildAccessLog(final ServerWebExchange exchange) {
        final RequestDTO requestDTO = exchange.getAttribute(Constants.REQUESTDTO);
        if (Objects.isNull(requestDTO) || Objects.isNull(exchange.getRequest().getRemoteAddress())) {
            return null;
        }

        //return paramParser.parseParameterForAccessLog(exchange, requestDTO); todo
        return new AccessLog();
    }

    @Override
    protected Mono<Void> doExecute(ServerWebExchange exchange, DiabloPluginChain chain, SelectorData selector, RuleData rule) {

        String routeHost = exchange.getAttribute(Constants.GATEWAY_CONTEXT_UPSTREAM_HOST);

        Mono<Void> asyncResult = chain.execute(exchange);

        // monitor high-frequency api & configs
        String apiName = Optional.ofNullable(exchange.getAttributes().get(Constants.GATEWAY_CONTEXT_API_NAME)).map(Object::toString).orElse(exchange.getRequest().getURI().getPath());

        RequestDTO requestDTO = exchange.getAttribute(Constants.REQUESTDTO);

        long durationStart = Optional.ofNullable(requestDTO).map(RequestDTO::getDurationStart).orElse(System.nanoTime());
        long eventStart = Optional.ofNullable(requestDTO).map(RequestDTO::getStartDateTime).orElse(System.currentTimeMillis());

        if (Boolean.TRUE.equals(accessLogEnable)) {
            try {
                //access log
                int eventStartHashValue = Math.abs((eventStart + "").hashCode()) % 100;
                if (eventStartHashValue <= accessLogPercentagy) {
                    AccessLogUtil.postAccessLogEvent(buildAccessLog(exchange));
                }

            } catch (Exception ex) {
                logger.error("access log error:{}", Throwables.getStackTraceAsString(ex));
            }
        }

        String transactionType = Optional.ofNullable(exchange.getRequest()).map(ServerHttpRequest::getHeaders).map(h -> h.getFirst("Host"))
                                         .map(s -> StringUtils.substringBefore(s, hostNameSeparator)).map(s -> Constants.TRANSACTION_TYPE_URL + "_" + s)
                                         .orElse(Constants.TRANSACTION_TYPE_URL);

        // dubbo cat point
        String dubboServiceName = exchange.getAttribute(Constants.DUBBO_CALL_SERVICE_NAME);

        if (!Strings.isNullOrEmpty(dubboServiceName)) {
            transactionType = Constants.DUBBO_TRANSACTION_TYPE_URL + dubboServiceName;
            routeHost = dubboServiceName;
        }

        asyncResult = asyncResult.transform(new CatReactorTransformer<>(new CatConfig(transactionType, apiName, routeHost, durationStart, eventStart)));

        return asyncResult;
    }

    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.LAST;
    }

    @Override
    public int getOrder() {
        return PluginEnum.MONITOR.getCode();
    }

    @Override
    public String named() {
        return PluginEnum.MONITOR.getName();
    }

    private String contextName(String route) {
        return Constants.GATEWAY_CONTEXT_ROUTE_PREFIX + route;
    }
}
