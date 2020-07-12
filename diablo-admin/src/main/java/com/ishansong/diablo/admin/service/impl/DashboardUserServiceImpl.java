package com.ishansong.diablo.admin.service.impl;

import com.ishansong.diablo.admin.dto.DashboardUserDTO;
import com.ishansong.diablo.admin.entity.DashboardUserDO;
import com.ishansong.diablo.admin.mapper.DashboardUserMapper;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.query.DashboardUserQuery;
import com.ishansong.diablo.admin.service.DashboardUserService;
import com.ishansong.diablo.admin.vo.DashboardUserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("dashboardUserService")
public class DashboardUserServiceImpl implements DashboardUserService {

    private final DashboardUserMapper dashboardUserMapper;

    @Autowired(required = false)
    public DashboardUserServiceImpl(final DashboardUserMapper dashboardUserMapper) {
        this.dashboardUserMapper = dashboardUserMapper;
    }

    @Override
    public int createOrUpdate(final DashboardUserDTO dashboardUserDTO) {
        DashboardUserDO dashboardUserDO = DashboardUserDO.buildDashboardUserDO(dashboardUserDTO);
        if (StringUtils.isEmpty(dashboardUserDTO.getId())) {
            return dashboardUserMapper.insertSelective(dashboardUserDO);
        } else {
            return dashboardUserMapper.updateSelective(dashboardUserDO);
        }
    }

    @Override
    public int delete(final List<String> ids) {
        int dashboardUserCount = 0;
        for (String id : ids) {
            dashboardUserCount += dashboardUserMapper.delete(id);
        }
        return dashboardUserCount;
    }

    @Override
    public DashboardUserVO findById(final String id) {
        return DashboardUserVO.buildDashboardUserVO(dashboardUserMapper.selectById(id));
    }

    @Override
    public DashboardUserVO findByQuery(final String userName, final String password) {
        return DashboardUserVO.buildDashboardUserVO(dashboardUserMapper.findByQuery(userName, password));
    }

    @Override
    public CommonPager<DashboardUserVO> listByPage(final DashboardUserQuery dashboardUserQuery) {
        PageParameter pageParameter = dashboardUserQuery.getPageParameter();
        return new CommonPager<>(
                new PageParameter(pageParameter.getCurrentPage(), pageParameter.getPageSize(), dashboardUserMapper.countByQuery(dashboardUserQuery)),
                dashboardUserMapper.selectByQuery(dashboardUserQuery).stream()
                        .map(DashboardUserVO::buildDashboardUserVO)
                        .collect(Collectors.toList()));
    }
}
