package com.ishansong.diablo.admin.pre.mapper;

import com.ishansong.diablo.admin.entity.ServiceInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository("preServiceInfoMapper")
public interface ServiceInfoMapper {

    ServiceInfoDO selectById(String id);
}
