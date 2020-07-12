package com.ishansong.diablo.admin.mapper;

import com.ishansong.diablo.admin.entity.SelectorConditionDO;
import com.ishansong.diablo.admin.query.SelectorConditionQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SelectorConditionMapper {

    SelectorConditionDO selectById(String id);

    List<SelectorConditionDO> selectByQuery(SelectorConditionQuery selectorConditionQuery);

    int insert(SelectorConditionDO selectorConditionDO);

    int insertSelective(SelectorConditionDO selectorConditionDO);

    int update(SelectorConditionDO selectorConditionDO);

    int updateSelective(SelectorConditionDO selectorConditionDO);

    int delete(String id);

    int deleteByQuery(SelectorConditionQuery selectorConditionQuery);

    int deleteBySelectorId(String selectorId);

    int insertBatch(@Param("selectorConditions") List<SelectorConditionDO> selectorConditions);
}
