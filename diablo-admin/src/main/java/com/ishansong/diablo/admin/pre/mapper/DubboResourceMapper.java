package com.ishansong.diablo.admin.pre.mapper;

import com.ishansong.diablo.admin.entity.DubboResourceDO;
import com.ishansong.diablo.admin.query.DubboResourceQuery;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("preDubboResourceMapper")
@Mapper
public interface DubboResourceMapper {

    List<DubboResourceDO> selectAll();

    List<DubboResourceDO> selectByPage(DubboResourceQuery dubboResourceQuery);

    int countByQuery(DubboResourceQuery dubboResourceQuery);
}
