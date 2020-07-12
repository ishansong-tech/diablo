package com.ishansong.diablo.admin.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ServiceUpstreamDTO implements Serializable {

    private String id;

    private String serviceInfoId;

    private String hostName;

    private String env;

    private String hostIp;
}
