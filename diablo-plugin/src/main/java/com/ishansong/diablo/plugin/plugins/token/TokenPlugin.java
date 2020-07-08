package com.ishansong.diablo.plugin.plugins.token;

import com.google.common.base.Throwables;
import com.ishansong.diablo.plugin.plugins.dubbo.DubboProxyService;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.ParamObjectType;
import com.ishansong.diablo.core.enums.PluginEnum;
import com.ishansong.diablo.core.enums.PluginTypeEnum;
import com.ishansong.diablo.core.model.dubbo.DubboExtConfig;
import com.ishansong.diablo.core.model.dubbo.DubboResourceStream;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.DubboSelectorHandle;
import com.ishansong.diablo.core.model.selector.SelectorData;
import com.ishansong.diablo.core.utils.GsonUtils;
import com.ishansong.diablo.plugin.plugins.AbstractDiabloPlugin;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class TokenPlugin extends AbstractDiabloPlugin {

    private final TokenAuthSDKService tokenAuthSDKService;

    private final DubboProxyService dubboProxyService;


    public TokenPlugin(LocalCacheManager localCacheManager, TokenAuthSDKService tokenAuthSDKService, DubboProxyService dubboProxyService) {
        super(localCacheManager);
        this.tokenAuthSDKService = tokenAuthSDKService;
        this.dubboProxyService = dubboProxyService;
    }

    @Override
    protected Mono<Void> doExecute(final ServerWebExchange exchange, DiabloPluginChain chain, SelectorData selector, RuleData rule) {
        tokenBase64(exchange);
        DubboSelectorHandle selectorHandle = GsonUtils.getInstance().fromJson(selector.getHandle(), DubboSelectorHandle.class);
        if (Strings.isEmpty(selectorHandle.getRegistry()) || Strings.isEmpty(selectorHandle.getAppName())) {
            log.error("TokenPlugin dubbo handle require param not configuration, selectorHandle:{}", selectorHandle.toString());
            return Mono.error(new RuntimeException("Diablo getRegistry or getAppName is null."));
        }

        DubboResourceStream dubboMappingResource = buildDubboResourceStream(exchange);

        return tokenAuthSDKService.checkToken(exchange, dubboProxyService, dubboMappingResource, buildPair(exchange), selectorHandle,chain);
    }

    private void tokenBase64(final ServerWebExchange exchange) {
        RequestDTO requestDTO = (RequestDTO) exchange.getAttributes().get(Constants.REQUESTDTO);
        log.info("TokenPlugin doExecute Token:{},clientId:{}",requestDTO.getToken(),requestDTO.getClientId());
        String tokenFromRequest="";
        try {
            tokenFromRequest= Optional.ofNullable(
                    new String(Base64.getUrlDecoder()
                            .decode(requestDTO.getToken().getBytes(StandardCharsets.UTF_8.name()))))
                    .orElse(null);
        }
        catch (Exception ex){
            log.info("TokenPlugin doExecute Token:{},error:{}",requestDTO.getToken(), Throwables.getStackTraceAsString(ex));
        }
        requestDTO.setToken(tokenFromRequest);
        exchange.getAttributes().put(Constants.REQUESTDTO,requestDTO);
    }

    private Pair buildPair(ServerWebExchange exchange) {
        RequestDTO requestDTO = (RequestDTO) exchange.getAttributes().get(Constants.REQUESTDTO);
        List<String> parameterTypes = new ArrayList<>();
        parameterTypes.add("java.lang.String");
        parameterTypes.add("java.lang.String");
        List<Object> args = new ArrayList<>();
        args.add(requestDTO.getClientId());
        args.add(requestDTO.getToken());
        return Pair.of(parameterTypes.toArray(new String[0]), args.toArray());
    }

    private DubboResourceStream buildDubboResourceStream(final ServerWebExchange exchange) {

        DubboResourceStream dubboMappingResource = new DubboResourceStream();
        //todo
        return dubboMappingResource;
    }


    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.FUNCTION;
    }

    @Override
    public int getOrder() {
        return PluginEnum.TOKEN.getCode();
    }

    @Override
    public String named() {
        return PluginEnum.TOKEN.getName();
    }

    @Override
    public Boolean skip(ServerWebExchange exchange) {
        final RequestDTO body = exchange.getAttribute(Constants.REQUESTDTO);
        return Strings.isEmpty(Objects.requireNonNull(body).getToken()) ;
    }
}
