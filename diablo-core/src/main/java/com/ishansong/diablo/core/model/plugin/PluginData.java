package com.ishansong.diablo.core.model.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginData implements Serializable {

    private String id;

    private String name;

    private String config;

    private Integer role;

    private Boolean enabled;

}
