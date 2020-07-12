package com.ishansong.diablo.admin.vo;

import com.ishansong.diablo.admin.entity.PluginDO;
import com.ishansong.diablo.core.enums.PluginEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginVO implements Serializable {

    private String id;

    private Integer code;

    private Integer role;

    private String name;

    private String config;

    private Boolean enabled;

    private String dateCreated;

    private String dateUpdated;

    public static PluginVO buildPluginVO(final PluginDO pluginDO) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        PluginEnum pluginEnum = PluginEnum.getPluginEnumByName(pluginDO.getName());
        return new PluginVO(pluginDO.getId(), pluginEnum == null ? null : pluginEnum.getCode(),
                pluginDO.getRole(), pluginDO.getName(), pluginDO.getConfig(), pluginDO.getEnabled(),
                dateTimeFormatter.format(pluginDO.getDateCreated().toLocalDateTime()),
                dateTimeFormatter.format(pluginDO.getDateUpdated().toLocalDateTime()));
    }
}
