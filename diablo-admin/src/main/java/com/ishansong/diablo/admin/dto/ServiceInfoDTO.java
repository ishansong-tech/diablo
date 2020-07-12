package com.ishansong.diablo.admin.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ServiceInfoDTO implements Serializable {

    private String id;

    private String name;

    private String env;

    private Integer port;

}
