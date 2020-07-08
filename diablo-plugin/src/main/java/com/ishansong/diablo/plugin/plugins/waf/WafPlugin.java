package com.ishansong.diablo.plugin.plugins.waf;

import com.google.common.base.Strings;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.PluginEnum;
import com.ishansong.diablo.core.enums.PluginTypeEnum;
import com.ishansong.diablo.core.enums.WafEnum;
import com.ishansong.diablo.core.model.DiabloResult;
import com.ishansong.diablo.core.model.convert.WafHandle;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import com.ishansong.diablo.core.utils.GsonUtils;
import com.ishansong.diablo.plugin.plugins.AbstractDiabloPlugin;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class WafPlugin extends AbstractDiabloPlugin {

    public WafPlugin(LocalCacheManager localCacheManager) {
        super(localCacheManager);
    }

    @Override
    protected Mono<Void> doExecute(ServerWebExchange exchange, DiabloPluginChain chain, SelectorData selector, RuleData rule) {

        @NotBlank String handle = rule.getHandle();

        final WafHandle wafHandle = GsonUtils.getInstance().fromJson(handle, WafHandle.class);

        if (Objects.isNull(wafHandle) || Strings.isNullOrEmpty(wafHandle.getPermission())) {

            log.error("waf handler can not configuration: {}", wafHandle);
            return chain.execute(exchange);
        }

        if (WafEnum.REJECT.getName().equals(wafHandle.getPermission())) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);

            final DiabloResult error = DiabloResult.error(Integer.parseInt(wafHandle.getStatusCode()), Constants.REJECT_MSG);

            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                    .bufferFactory().wrap(Objects.requireNonNull(GsonUtils.getInstance().toJson(error).getBytes(Charset.forName(StandardCharsets.UTF_8.name()))))));
        }

        return chain.execute(exchange);
    }

    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.BEFORE;
    }

    @Override
    public int getOrder() {
        return PluginEnum.WAF.getCode();
    }

    @Override
    public String named() {
        return PluginEnum.WAF.getName();
    }
}
