package com.ishansong.diablo.admin.pre.mapper;

import com.ishansong.diablo.admin.entity.SelectorDO;
import com.ishansong.diablo.admin.entity.SelectorPublishDO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository("preSelectorMapper")
public interface SelectorMapper {

    SelectorDO selectById(String id);

    List<SelectorDO> findByIds(@Param("ids") List<String> ids);

    int delete(String id);

    int insertSelective(SelectorDO selectorDO);

    @MapKey("id")
    Map<String, SelectorPublishDO> selectBySelectorIds(@Param("selectorIds") List<String> selectorIds);
}
