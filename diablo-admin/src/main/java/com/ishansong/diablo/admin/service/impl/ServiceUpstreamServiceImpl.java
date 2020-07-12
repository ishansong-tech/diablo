package com.ishansong.diablo.admin.service.impl;

import com.google.common.collect.Lists;
import com.ishansong.diablo.admin.dto.ServiceUpstreamDTO;
import com.ishansong.diablo.admin.entity.ServiceUpstreamDO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.mapper.ServiceUpstreamMapper;
import com.ishansong.diablo.admin.query.ServiceUpstreamQuery;
import com.ishansong.diablo.admin.service.ServiceUpstreamService;
import com.ishansong.diablo.admin.vo.ServiceUpstreamVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service("serviceUpstreamService")
public class ServiceUpstreamServiceImpl implements ServiceUpstreamService {

    private final ServiceUpstreamMapper serviceUpstreamMapper;

    @Autowired(required = false)
    public ServiceUpstreamServiceImpl(final ServiceUpstreamMapper serviceUpstreamMapper) {
        this.serviceUpstreamMapper = serviceUpstreamMapper;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int createOrUpdate(final ServiceUpstreamDTO serviceUpstreamDTO) {
        int count;
        ServiceUpstreamDO serviceUpstreamDO = ServiceUpstreamDO.buildServiceUpstreamDO(serviceUpstreamDTO);

        if (StringUtils.isEmpty(serviceUpstreamDTO.getId())) {
            count = serviceUpstreamMapper.insertSelective(serviceUpstreamDO);
        } else {
            count = serviceUpstreamMapper.updateSelective(serviceUpstreamDO);
        }

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(final List<String> ids) {
        int count = 0;
        for (String id : ids) {
            count += serviceUpstreamMapper.delete(id);
        }
        return count;
    }

    @Override
    public ServiceUpstreamVO findById(final String id) {
        return ServiceUpstreamVO.buildServiceUpstreamVO(serviceUpstreamMapper.selectById(id));
    }

    @Override
    public CommonPager<ServiceUpstreamVO> listByPage(final ServiceUpstreamQuery serviceUpstreamQuery) {
        PageParameter pageParameter = serviceUpstreamQuery.getPageParameter();

        Integer count= serviceUpstreamMapper.countByQuery(serviceUpstreamQuery);

        if(count<=0){
            return new CommonPager<>(
                    new PageParameter(pageParameter.getCurrentPage(), pageParameter.getPageSize(),
                            0), Lists.newArrayList());
        }

        List<ServiceUpstreamDO> serviceInfoDOList = serviceUpstreamMapper.selectByQuery(serviceUpstreamQuery);

        List<ServiceUpstreamVO> result = serviceInfoDOList.stream()
                                                          .map(ServiceUpstreamVO::buildServiceUpstreamVO)
                                                          .collect(Collectors.toList());

        return new CommonPager<>(
                new PageParameter(pageParameter.getCurrentPage(), pageParameter.getPageSize(),count), result);
    }

    @Override
    public String sync(String env, String serviceNameParam) {

        //todo
        return "同步成功";


    }
}
