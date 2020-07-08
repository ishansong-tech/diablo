package com.ishansong.diablo.plugin.plugins;

import com.ishansong.diablo.plugin.condition.strategy.MatchStrategyFactory;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.PluginEnum;
import com.ishansong.diablo.core.enums.SelectorTypeEnum;
import com.ishansong.diablo.core.model.DiabloResult;
import com.ishansong.diablo.core.model.plugin.PluginData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import com.ishansong.diablo.core.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;


@Slf4j(topic = "requestTraceLogger")
@RequiredArgsConstructor
public abstract class AbstractDiabloPlugin implements DiabloPlugin {

    protected final LocalCacheManager localCacheManager;

    protected abstract Mono<Void> doExecute(ServerWebExchange exchange, DiabloPluginChain chain, SelectorData selector, RuleData rule);

    @Override
    public Mono<Void> execute(final ServerWebExchange exchange, final DiabloPluginChain chain) {

        String traceId = exchange.getAttribute(Constants.CLIENT_RESPONSE_TRACE_ID);
        String pluginName = named();
        final PluginData pluginData = localCacheManager.findPluginByName(pluginName);
        if (!(skip(exchange) || pluginData == null || !pluginData.getEnabled())) {
            //获取selector
            final List<SelectorData> selectors = localCacheManager.findSelectorByPluginName(pluginName);
            if (CollectionUtils.isEmpty(selectors)) {

                if (Objects.equals(pluginName, PluginEnum.DIVIDE.getName())) {
                    log.warn("AbstractDiabloPlugin.execute selectors is empty, traceId: {}, name: {}, uri: {}", traceId, pluginName, exchange.getRequest().getURI().getPath());
                }

                return chain.execute(exchange);
            }
            final SelectorData selectorData = filterSelector(selectors, exchange);

            if (Objects.isNull(selectorData)) {
                if (Objects.equals(pluginName, PluginEnum.DIVIDE.getName())) {
                    log.warn("AbstractDiabloPlugin.execute selectorData is null, traceId: {}, name: {}, uri: {}, host: {}", traceId, pluginName, exchange.getRequest().getURI().getPath(), exchange.getRequest().getHeaders().getFirst("Host"));
                }

                return chain.execute(exchange);
            }

            List<RuleData> rules = localCacheManager.findRuleBySelectorId(selectorData.getId());

            if (CollectionUtils.isEmpty(rules)) {
                rules = localCacheManager.findRuleBySelectorId(selectorData.getId());

                if (CollectionUtils.isEmpty(rules)) {
                    if (Objects.equals(pluginName, PluginEnum.DIVIDE.getName())) {
                        log.warn("AbstractDiabloPlugin.execute rules is empty, traceId: {}, name: {}, selectorName: {}, uri: {}", traceId, pluginName, selectorData.getName(), exchange.getRequest().getURI().getPath());
                    }

                    return chain.execute(exchange);
                }
            }

            RuleData rule = filterRule(exchange, rules);

            if (Objects.isNull(rule)) {
                //If the divide or dubbo or spring cloud plug-in does not match, return directly
                if (PluginEnum.DIVIDE.getName().equals(pluginName) || PluginEnum.DUBBO.getName().equals(pluginName) || PluginEnum.SPRING_CLOUD.getName().equals(pluginName)) {

                    log.warn("AbstractDiabloPlugin.execute rule is null traceId: {}, name: {}, selectorName: {}, traceId: {}, uri: {}, ruleSize: {}",
                            traceId, pluginName, selectorData.getName(), traceId, exchange.getRequest().getURI().getPath(), rules.size());

                    final DiabloResult error = DiabloResult.error(HttpStatus.NOT_FOUND.value(),
                            Constants.UPSTREAM_NOT_FIND);

                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
                                                                              .wrap(Objects.requireNonNull(JsonUtils.toJson(error)).getBytes(Charset.forName(StandardCharsets.UTF_8.name())))));
                }

                return chain.execute(exchange);
            }

            return doExecute(exchange, chain, selectorData, rule);
        }

        return chain.execute(exchange);
    }

    public SelectorData filterSelector(final List<SelectorData> selectors, final ServerWebExchange exchange) {
        return selectors.stream()
                        .filter(selector -> selector.getEnabled() && filterSelector(selector, exchange))
                        .findFirst().orElse(null);
    }

    private Boolean filterSelector(final SelectorData selector, final ServerWebExchange exchange) {
        if (selector.getType() == SelectorTypeEnum.CUSTOM_FLOW.getCode()) {
            if (CollectionUtils.isEmpty(selector.getConditionList())) {
                return false;
            }
            return MatchStrategyFactory.of(selector.getMatchMode())
                                       .match(selector.getConditionList(), exchange);
        }
        return true;
    }

    private RuleData filterRule(final ServerWebExchange exchange, final List<RuleData> rules) {
        return rules.stream()
                    .filter(rule -> Objects.nonNull(rule) && rule.getEnabled())
                    .filter(ruleData -> MatchStrategyFactory.of(ruleData.getMatchMode())
                                                            .match(ruleData.getConditionDataList(), exchange))
                    .findFirst().orElse(null);
    }
}
