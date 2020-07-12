package com.ishansong.diablo.admin.mapper;

import com.ishansong.diablo.admin.entity.DubboResourceDO;
import com.ishansong.diablo.admin.query.DubboResourceQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DubboResourceMapper {

    List<DubboResourceDO> selectAll();

    List<DubboResourceDO> selectByPage(DubboResourceQuery dubboResourceQuery);

    int countByQuery(DubboResourceQuery dubboResourceQuery);

    DubboResourceDO selectById(String id);

    int insertSelective(DubboResourceDO dubboResourceDO);

    int updateSelective(DubboResourceDO dubboResourceDO);

    int delete(String id);
}
