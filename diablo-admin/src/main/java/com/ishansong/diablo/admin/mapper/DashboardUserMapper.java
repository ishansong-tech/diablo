package com.ishansong.diablo.admin.mapper;

import com.ishansong.diablo.admin.entity.DashboardUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.ishansong.diablo.admin.query.DashboardUserQuery;

import java.util.List;

@Mapper
public interface DashboardUserMapper {

    DashboardUserDO selectById(String id);

    DashboardUserDO findByQuery(@Param("userName") String userName, @Param("password") String password);

    List<DashboardUserDO> selectByQuery(DashboardUserQuery dashboardUserQuery);

    Integer countByQuery(DashboardUserQuery dashboardUserQuery);

    int insert(DashboardUserDO dashboardUserDO);

    int insertSelective(DashboardUserDO dashboardUserDO);

    int update(DashboardUserDO dashboardUserDO);

    int updateSelective(DashboardUserDO dashboardUserDO);

    int delete(String id);
}
