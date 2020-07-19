package com.ishansong.diablo.extension.sentinel.adapter.api;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinitionChangeObserver;

import java.util.Set;

public class DiabloApiDefinitionChangeObserver implements ApiDefinitionChangeObserver {

    @Override
    public void onChange(Set<ApiDefinition> apiDefinitions) {
        DiabloApiMatcherManager.loadApiDefinitions(apiDefinitions);
    }
}
