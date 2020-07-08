package com.ishansong.diablo.core.model.selector;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class DivideUpstream implements Serializable {

    private String upstreamHost;

    private String protocol;

    private String upstreamUrl;

    private int weight;

    private boolean status = true;

    private long timestamp;

    private boolean healthStatus;

    private String healthUri;

    private String serviceName;

}
