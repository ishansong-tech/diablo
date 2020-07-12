package com.ishansong.diablo.admin.service;

import com.ishansong.diablo.admin.dto.DashboardUserDTO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.query.DashboardUserQuery;
import com.ishansong.diablo.admin.vo.DashboardUserVO;

import java.util.List;

public interface DashboardUserService {

    int createOrUpdate(DashboardUserDTO dashboardUserDTO);

    int delete(List<String> ids);

    DashboardUserVO findById(String id);

    DashboardUserVO findByQuery(final String userName, final String password);

    CommonPager<DashboardUserVO> listByPage(DashboardUserQuery dashboardUserQuery);
}
