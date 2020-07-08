package com.ishansong.diablo.plugin.cat.reactor;

import lombok.Data;

@Data
public class CatConfig {

    private String transactionType;

    private String apiName;

    private String routeHost;

    private long durationStart;

    private long eventStart;

    public CatConfig(String transactionType, String apiName, String routeHost, long durationStart, long eventStart) {
        this.transactionType = transactionType;
        this.apiName = apiName;
        this.routeHost = routeHost;
        this.durationStart = durationStart;
        this.eventStart = eventStart;
    }
}
