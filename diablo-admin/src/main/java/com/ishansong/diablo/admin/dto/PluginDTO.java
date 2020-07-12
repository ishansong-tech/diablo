package com.ishansong.diablo.admin.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PluginDTO implements Serializable {

    private String id;

    private String name;

    private String config;

    private Integer role;

    private Boolean enabled;
}
