package com.ishansong.diablo.admin.mapper;

import com.ishansong.diablo.admin.entity.ServiceInfoDO;
import com.ishansong.diablo.admin.query.ServiceInfoQuery;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ServiceInfoMapper {

    ServiceInfoDO selectById(String id);

    List<ServiceInfoDO> selectByQuery(ServiceInfoQuery serviceInfoQuery);

    Integer countByQuery(ServiceInfoQuery serviceInfoQuery);

    int insert(ServiceInfoDO serviceInfoDO);

    int insertSelective(ServiceInfoDO serviceInfoDO);

    int update(ServiceInfoDO serviceInfoDO);

    int updateSelective(ServiceInfoDO serviceInfoDO);

    int delete(String id);

    List<ServiceInfoDO> selectAll();

    @MapKey("id")
    Map<String, ServiceInfoDO> selectAllMap();
}
