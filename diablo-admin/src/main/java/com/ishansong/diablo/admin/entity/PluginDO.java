package com.ishansong.diablo.admin.entity;

import com.ishansong.diablo.admin.dto.PluginDTO;
import com.ishansong.diablo.core.utils.UUIDUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import java.sql.Timestamp;

@Data
public class PluginDO extends BaseDO {

    private String name;

    private String config;

    private Boolean enabled;

    private Integer role;

    public static PluginDO buildPluginDO(final PluginDTO pluginDTO) {
        if (pluginDTO != null) {
            PluginDO pluginDO = new PluginDO();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (StringUtils.isEmpty(pluginDTO.getId())) {
                pluginDO.setId(UUIDUtils.generateShortUuid());
                pluginDO.setDateCreated(currentTime);
            } else {
                pluginDO.setId(pluginDTO.getId());
            }
            pluginDO.setName(pluginDTO.getName());
            pluginDO.setConfig(pluginDTO.getConfig());
            pluginDO.setEnabled(pluginDTO.getEnabled());
            pluginDO.setRole(pluginDTO.getRole());
            pluginDO.setDateUpdated(currentTime);
            return pluginDO;
        }
        return null;
    }
}
