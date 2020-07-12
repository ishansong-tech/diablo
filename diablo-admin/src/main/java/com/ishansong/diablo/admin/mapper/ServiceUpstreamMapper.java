package com.ishansong.diablo.admin.mapper;

import com.ishansong.diablo.admin.entity.ServiceUpstreamDO;
import com.ishansong.diablo.admin.query.ServiceUpstreamQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceUpstreamMapper {

    ServiceUpstreamDO selectById(String id);

    List<ServiceUpstreamDO> selectByQuery(ServiceUpstreamQuery serviceUpstreamQuery);

    Integer countByQuery(ServiceUpstreamQuery serviceUpstreamQuery);

    int insert(ServiceUpstreamDO serviceUpstreamDO);

    int insertSelective(ServiceUpstreamDO serviceUpstreamDO);

    int update(ServiceUpstreamDO serviceUpstreamDO);

    int updateSelective(ServiceUpstreamDO serviceUpstreamDO);

    int delete(String id);

    List<ServiceUpstreamDO> selectAll();

    List<ServiceUpstreamDO> selectIpsByServiceInfoId(String serviceInfoId);

    List<ServiceUpstreamDO> selectByServiceInfoIds(@Param("serviceInfoIds") List<String> serviceInfoIds);
}
