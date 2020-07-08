package com.ishansong.diablo.plugin.plugins.dubbo;

import com.google.common.base.Strings;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.ParamObjectType;
import com.ishansong.diablo.core.enums.PluginEnum;
import com.ishansong.diablo.core.enums.PluginTypeEnum;
import com.ishansong.diablo.core.enums.RpcTypeEnum;
import com.ishansong.diablo.core.model.dubbo.ApiConfig;
import com.ishansong.diablo.core.model.dubbo.DubboResourceStream;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.DubboSelectorHandle;
import com.ishansong.diablo.core.model.selector.SelectorData;
import com.ishansong.diablo.core.utils.GsonUtils;
import com.ishansong.diablo.core.utils.UrlUtils;
import com.ishansong.diablo.plugin.plugins.AbstractDiabloPlugin;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
public class DubboPlugin extends AbstractDiabloPlugin {

    private final DubboProxyService dubboProxyService;

    public DubboPlugin(LocalCacheManager localCacheManager, DubboProxyService dubboProxyService) {
        super(localCacheManager);

        this.dubboProxyService = dubboProxyService;
    }

    @Override
    protected Mono<Void> doExecute(ServerWebExchange exchange, DiabloPluginChain chain, SelectorData selector, RuleData rule) {

        DubboSelectorHandle selectorHandle = GsonUtils.getInstance().fromJson(selector.getHandle(), DubboSelectorHandle.class);

        String resourceKey = exchange.getRequest().getHeaders().getFirst(Constants.RESOURCE_KEY);
        resourceKey = (Strings.isNullOrEmpty(resourceKey) ? UrlUtils.getQueryString(exchange.getRequest().getURI().getQuery(),Constants.RESOURCE_KEY) : resourceKey);

        if (Strings.isNullOrEmpty(selectorHandle.getRegistry()) || Strings.isNullOrEmpty(selectorHandle.getAppName())) {
            log.error("DubboPlugin dubbo handle require param not configuration, resourceKey:{}", resourceKey);

            exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);

            return chain.execute(exchange);
        }

        if (Strings.isNullOrEmpty(resourceKey)) {
            log.warn("DubboPlugin dubbo handle resource key is empty");

            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);

            return chain.execute(exchange);
        }

        DubboResourceStream dubboMappingResource = localCacheManager.findDubbResource(resourceKey);
        if (dubboMappingResource == null) {
            log.warn("DubboPlugin dubbo handle dubboMappingResource is empty, resourceKey:{}", resourceKey);

            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);

            return chain.execute(exchange);
        }

        // mapping paramMap
        Pair<String[], Object[]> pair = buildMappingParamMap(exchange, dubboMappingResource);

        if (pair.getLeft().length != pair.getRight().length) {

            log.warn("DubboPlugin dubbo handle params is inconsistent, resourceKey:{}, pair:{}", resourceKey, GsonUtils.getInstance().toJson(pair));

            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);

            return chain.execute(exchange);
        }

        if (!corsRequest(exchange, dubboMappingResource, resourceKey)) {

            return chain.execute(exchange);
        }

        DubboCommand dubboCommand = new DubboCommand(exchange, chain, dubboProxyService, dubboMappingResource, pair, selectorHandle);

        return dubboCommand.doRpcInvoke();
    }

    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.FUNCTION;
    }

    @Override
    public int getOrder() {
        return PluginEnum.DUBBO.getCode();
    }

    @Override
    public String named() {
        return PluginEnum.DUBBO.getName();
    }

    @Override
    public Boolean skip(final ServerWebExchange exchange) {
        final RequestDTO body = exchange.getAttribute(Constants.REQUESTDTO);
        return !Objects.equals(Objects.requireNonNull(body).getRpcType(), RpcTypeEnum.DUBBO.getName());
    }

    private Pair<String[], Object[]> buildMappingParamMap(ServerWebExchange exchange, DubboResourceStream dubboMappingResource) {

        List<String> parameterTypes = new ArrayList<>();
        List<Object> args = new ArrayList<>();

        if (HttpMethod.GET.equals(exchange.getRequest().getMethod())) {
            //请求参数
            MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
            //后台配置参数
            LinkedHashMap<String, String> paramMetas = dubboMappingResource.getParamMetas();
            boolean isEmptyParamMetas = CollectionUtils.isEmpty(paramMetas);
            boolean isEmptyQueryParams = CollectionUtils.isEmpty(paramMetas);

            dubboMappingResource.getParamMetas().forEach((k, v) -> {
                if (!isEmptyParamMetas && !isEmptyQueryParams) {
                    List<String> paramValue = queryParams.get(k);
                    parameterTypes.add(v);
                    if (paramValue != null && paramValue.size() > 1) {
                        args.add(paramValue);
                    } else {
                        args.add(queryParams.getFirst(k));
                    }
                }
            });
        } else {
            String json = exchange.getAttribute(Constants.DUBBO_PARAMS);

            if (!Strings.isNullOrEmpty(json)) {
                if (Objects.equals(dubboMappingResource.getParamObjectType(), ParamObjectType.COMPOSE.getCode())) {
                    List<Object> params = GsonUtils.getInstance().fromList(json);

                    // paramMetas is LinkedHashMap
                    if (dubboMappingResource.getParamMetas().size() <= params.size()) {

                        LinkedHashMap<String, String> paramMetas = dubboMappingResource.getParamMetas();
                        List<String> paramKeys = new ArrayList<>(paramMetas.keySet());

                        for (int i = 0; i < params.size(); i++) {

                            String metaKey = paramKeys.get(i);
                            String metaTypeValue = paramMetas.get(metaKey);

                            parameterTypes.add(metaTypeValue);
                            // 没有定义key {"java.util.List":"java.util.List","java.lang.Long":"java.lang.Long"}
                            if (Objects.equals(metaKey, metaTypeValue)) {
                                args.add(params.get(i));
                            } else {
                                // 定义了key {"userId":"java.lang.Long"}
                                Object o = params.get(i);
                                if (o != null) {
                                    Map map = (Map) o;
                                    Object realValue = map.get(metaKey);

                                    args.add(realValue);
                                }
                            }
                        }
                    }
                } else {
                    LinkedHashMap<String, String> paramMetas = dubboMappingResource.getParamMetas();
                    if (paramMetas.size() == 1 && paramMetas.containsKey(List.class.getCanonicalName())) {

                        parameterTypes.add(List.class.getCanonicalName());
                        args.add(GsonUtils.getInstance().fromList(json));
                    } else {
                        Map<String, Object> jsonMap = GsonUtils.getInstance().toObjectMap(json);
                        paramMetas.forEach((k, v) -> {
                            parameterTypes.add(v);
                            if (Objects.equals(k, v)) {
                                args.add(jsonMap);
                            } else {
                                args.add(jsonMap.get(k));
                            }
                        });
                    }
                }

            }
        }
        return Pair.of(parameterTypes.toArray(new String[0]), args.toArray());
    }

    private boolean corsRequest(ServerWebExchange exchange, DubboResourceStream dubboMappingResource, String resourceKey) {
        ApiConfig apiConfig = dubboMappingResource.getApiConfig();
        if (apiConfig == null) {
            return true;
        }

        List<String> allowDomains = apiConfig.getAllowDomain();
        if (!CollectionUtils.isEmpty(allowDomains)) {

            String host = exchange.getRequest().getHeaders().getFirst("Host");
            if (!allowDomains.contains(host)) {
                log.warn("DubboPlugin dubbo handle allow domain warning, resourceKey:{}, host:{}", resourceKey, host);

                return false;
            }
        }

        String corsOrigin = apiConfig.getCorsOrigin();
        if (Strings.isNullOrEmpty(corsOrigin)) {
            return true;
        }

        if (CorsUtils.isCorsRequest(exchange.getRequest()) && CorsUtils.isSameOrigin(exchange.getRequest())) {

            HttpHeaders responseHeaders = exchange.getResponse().getHeaders();

            exchange.getResponse().getHeaders().addAll(HttpHeaders.VARY, Arrays.asList(HttpHeaders.ORIGIN,
                    HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS));

            responseHeaders.setAccessControlAllowOrigin(corsOrigin);
            exchange.getResponse().getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
            exchange.getResponse().getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*");


            Boolean corsCredentials = apiConfig.getCorsCredentials();
            if (corsCredentials != null) {
                responseHeaders.setAccessControlAllowCredentials(corsCredentials);
            }

            // default 1800 seconds (30 minutes)
            responseHeaders.setAccessControlMaxAge(apiConfig.getCorsMaxAge() != null ? apiConfig.getCorsMaxAge() : 1800L);

            return true;
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);

            log.warn("DubboPlugin dubbo handle cors request warning, resourceKey:{}, origin:{}", resourceKey, exchange.getRequest().getHeaders().get(HttpHeaders.ORIGIN));
            return false;
        }
    }
}
