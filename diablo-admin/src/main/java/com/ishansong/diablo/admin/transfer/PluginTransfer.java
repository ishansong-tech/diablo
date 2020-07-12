package com.ishansong.diablo.admin.transfer;

import com.ishansong.diablo.admin.entity.PluginDO;
import com.ishansong.diablo.admin.vo.PluginVO;
import com.ishansong.diablo.core.model.plugin.PluginData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PluginTransfer {

    PluginTransfer INSTANCE = Mappers.getMapper(PluginTransfer.class);

    PluginData mapToData(PluginDO pluginDO);

    PluginData mapDataTOVO(PluginVO pluginVO);


}
