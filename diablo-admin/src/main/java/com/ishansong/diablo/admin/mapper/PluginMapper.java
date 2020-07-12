package com.ishansong.diablo.admin.mapper;

import com.ishansong.diablo.admin.entity.PluginDO;
import com.ishansong.diablo.admin.query.PluginQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PluginMapper {

    PluginDO selectById(String id);

    PluginDO selectByName(String name);

    List<PluginDO> selectByQuery(PluginQuery pluginQuery);

    List<PluginDO> selectAll();

    Integer countByQuery(PluginQuery pluginQuery);

    int insert(PluginDO pluginDO);

    int insertSelective(PluginDO pluginDO);

    int update(PluginDO pluginDO);

    int updateEnable(PluginDO pluginDO);

    int updateSelective(PluginDO pluginDO);

    int delete(String id);
}
