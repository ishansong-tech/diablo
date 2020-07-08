package com.ishansong.diablo.plugin.plugins.divide;

import com.ishansong.diablo.core.enums.*;
import com.ishansong.diablo.plugin.plugins.divide.balance.LoadBalanceUtils;
import com.ishansong.diablo.plugin.plugins.divide.http.HttpCommand;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.cache.UpstreamCacheManager;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.model.condition.ConditionData;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.core.model.rule.DivideRuleHandle;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.DivideUpstream;
import com.ishansong.diablo.core.model.selector.SelectorData;
import com.ishansong.diablo.core.utils.GsonUtils;
import com.ishansong.diablo.plugin.plugins.AbstractDiabloPlugin;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Slf4j(topic = "requestTraceLogger")
public class DividePlugin extends AbstractDiabloPlugin {

    private final UpstreamCacheManager upstreamCacheManager;

    private final WebClient webClient;

    public DividePlugin(final LocalCacheManager localCacheManager, final UpstreamCacheManager upstreamCacheManager, final WebClient webClient) {
        super(localCacheManager);
        this.upstreamCacheManager = upstreamCacheManager;
        this.webClient = webClient;
    }

    @Override
    protected Mono<Void> doExecute(final ServerWebExchange exchange, final DiabloPluginChain chain, final SelectorData selector, final RuleData rule) {
        final RequestDTO requestDTO = exchange.getAttribute(Constants.REQUESTDTO);
        final String traceId = exchange.getAttribute(Constants.CLIENT_RESPONSE_TRACE_ID);
        final DivideRuleHandle ruleHandle = GsonUtils.getInstance().fromJson(rule.getHandle(), DivideRuleHandle.class);

        String ruleId = rule.getId();
        final List<DivideUpstream> upstreamList = upstreamCacheManager.findUpstreamListByRuleId(ruleId);
        if (CollectionUtils.isEmpty(upstreamList)) {

            log.warn("DividePlugin.doExecute upstreamList is empty, traceId: {}, uri: {}, ruleName:{}", traceId, exchange.getRequest().getURI().getPath(), rule.getName());
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return chain.execute(exchange);
        }

        final String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();

        DivideUpstream divideUpstream =
                LoadBalanceUtils.selector(upstreamList, ruleHandle.getLoadBalance(), ip);

        if (Objects.isNull(divideUpstream)) {

            log.warn("DividePlugin.doExecute divideUpstream is empty, traceId: {}, uri: {}, loadBalance:{}, ruleName:{}, upstreamSize: {}", traceId, exchange.getRequest().getURI().getPath(), ruleHandle.getLoadBalance(), rule.getName(), upstreamList.size());
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return chain.execute(exchange);
        }

        if (exchange.getAttributeOrDefault(Constants.GATEWAY_ALREADY_ROUTED_ATTR, false)) {
            log.warn("DividePlugin.doExecute alread routed, traceId: {}, uri: {}, ruleName:{}", traceId, exchange.getRequest().getURI().getPath(), rule.getName());

            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return chain.execute(exchange);
        }
        exchange.getAttributes().put(Constants.GATEWAY_ALREADY_ROUTED_ATTR, true);

        exchange.getAttributes().put(Constants.GATEWAY_CONTEXT_UPSTREAM_HOST, divideUpstream.getUpstreamHost());
        exchange.getAttributes().put(Constants.GATEWAY_CONTEXT_RULE_ID, ruleId);

        HttpCommand command = new HttpCommand(exchange, chain,
                requestDTO, divideUpstream, webClient, ruleHandle.getTimeout());
        return command.doHttpInvoke();
    }

    public SelectorData filterSelector(final List<SelectorData> selectors, final ServerWebExchange exchange) {
        return selectors.stream()
                        .filter(selector -> selector.getEnabled() && filterCustomSelector(selector, exchange))
                        .findFirst().orElse(null);
    }

    private Boolean filterCustomSelector(final SelectorData selector, final ServerWebExchange exchange) {
        if (selector.getType() == SelectorTypeEnum.CUSTOM_FLOW.getCode()) {

            List<ConditionData> conditionList = selector.getConditionList();
            if (CollectionUtils.isEmpty(conditionList)) {
                return false;
            }

            // 后台初始定义为host且表达式为 =
            if (MatchModeEnum.AND.getCode() == selector.getMatchMode()) {
                ConditionData conditionData = conditionList.get(0);
                return Objects.equals(exchange.getRequest().getHeaders().getFirst("Host"), conditionData.getParamValue().trim());
            } else {
                return conditionList.stream().anyMatch(c -> Objects.equals(exchange.getRequest().getHeaders().getFirst("Host"), c.getParamValue().trim()));
            }

        }
        return true;
    }

    @Override
    public String named() {
        return PluginEnum.DIVIDE.getName();
    }

    @Override
    public Boolean skip(final ServerWebExchange exchange) {
        final RequestDTO body = exchange.getAttribute(Constants.REQUESTDTO);
        return !Objects.equals(Objects.requireNonNull(body).getRpcType(), RpcTypeEnum.HTTP.getName());
    }

    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.FUNCTION;
    }

    @Override
    public int getOrder() {
        return PluginEnum.DIVIDE.getCode();
    }

}
