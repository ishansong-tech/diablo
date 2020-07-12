package com.ishansong.diablo.admin.service;

import com.ishansong.diablo.admin.dto.RuleUpstreamDTO;
import com.ishansong.diablo.admin.dto.ServiceInfoDTO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.query.ServiceInfoQuery;
import com.ishansong.diablo.admin.vo.ServiceInfoVO;

import java.util.List;

public interface ServiceInfoService {

    int createOrUpdate(ServiceInfoDTO serviceInfoDTO);

    int delete(List<String> ids);

    ServiceInfoVO findById(String id);

    CommonPager<ServiceInfoVO> listByPage(ServiceInfoQuery serviceInfoQuery);

    List<ServiceInfoVO> selectByQuery(ServiceInfoQuery serviceInfoQuery);

    List<RuleUpstreamDTO> findUpstreamHandle(String serviceInfoId);
}
