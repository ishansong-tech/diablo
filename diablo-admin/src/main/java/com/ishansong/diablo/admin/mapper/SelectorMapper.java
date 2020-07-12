package com.ishansong.diablo.admin.mapper;

import com.ishansong.diablo.admin.entity.SelectorDO;
import com.ishansong.diablo.admin.entity.SelectorPublishDO;
import com.ishansong.diablo.admin.query.SelectorQuery;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SelectorMapper {

    SelectorDO selectById(String id);

    List<SelectorDO> findByIds(@Param("ids") List<String> ids);

    List<SelectorDO> selectByQuery(SelectorQuery selectorQuery);

    List<SelectorDO> findByPluginId(String pluginId);

    Integer countByQuery(SelectorQuery selectorQuery);

    int insert(SelectorDO selectorDO);

    int insertSelective(SelectorDO selectorDO);

    int update(SelectorDO selectorDO);

    int updateSelective(SelectorDO selectorDO);

    int delete(String id);

    int deleteByPluginId(String pluginId);

    List<SelectorDO> selectAll();

    @MapKey("id")
    Map<String, SelectorPublishDO> selectBySelectorIds(@Param("selectorIds") List<String> selectorIds);
}
