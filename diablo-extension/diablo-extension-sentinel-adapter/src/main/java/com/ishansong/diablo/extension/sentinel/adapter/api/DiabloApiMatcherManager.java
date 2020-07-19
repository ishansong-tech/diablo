package com.ishansong.diablo.extension.sentinel.adapter.api;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.ishansong.diablo.extension.sentinel.adapter.api.matcher.WebExchangeApiMatcher;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class DiabloApiMatcherManager {

    private static final Map<String, WebExchangeApiMatcher> API_MATCHER_MAP = new ConcurrentHashMap<>();

    public static Map<String, WebExchangeApiMatcher> getApiMatcherMap() {
        return Collections.unmodifiableMap(API_MATCHER_MAP);
    }

    public static Optional<WebExchangeApiMatcher> getMatcher(final String apiName) {
        return Optional.ofNullable(apiName)
            .map(e -> API_MATCHER_MAP.get(apiName));
    }

    public static Set<ApiDefinition> getApiDefinitionSet() {
        return API_MATCHER_MAP.values()
            .stream()
            .map(WebExchangeApiMatcher::getApiDefinition)
            .collect(Collectors.toSet());
    }

    static synchronized void loadApiDefinitions(/*@Valid*/ Set<ApiDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            API_MATCHER_MAP.clear();
            return;
        }
        definitions.forEach(DiabloApiMatcherManager::addApiDefinition);
    }

    static void addApiDefinition(ApiDefinition definition) {
        API_MATCHER_MAP.put(definition.getApiName(), new WebExchangeApiMatcher(definition));
    }

    private DiabloApiMatcherManager() {}
}
