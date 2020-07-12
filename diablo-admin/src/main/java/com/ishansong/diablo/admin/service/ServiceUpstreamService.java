package com.ishansong.diablo.admin.service;

import com.ishansong.diablo.admin.dto.ServiceUpstreamDTO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.query.ServiceUpstreamQuery;
import com.ishansong.diablo.admin.vo.ServiceUpstreamVO;

import java.util.List;

public interface ServiceUpstreamService {

    int createOrUpdate(ServiceUpstreamDTO serviceUpstreamDTO);

    int delete(List<String> ids);

    ServiceUpstreamVO findById(String id);

    CommonPager<ServiceUpstreamVO> listByPage(ServiceUpstreamQuery serviceUpstreamQuery);

    String sync(String env,String serviceName);

}
