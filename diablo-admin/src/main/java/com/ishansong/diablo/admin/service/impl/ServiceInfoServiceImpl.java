package com.ishansong.diablo.admin.service.impl;

import com.google.common.base.Strings;
import com.ishansong.diablo.admin.dto.RuleUpstreamDTO;
import com.ishansong.diablo.admin.dto.ServiceInfoDTO;
import com.ishansong.diablo.admin.entity.ServiceInfoDO;
import com.ishansong.diablo.admin.entity.ServiceUpstreamDO;
import com.ishansong.diablo.admin.mapper.ServiceInfoMapper;
import com.ishansong.diablo.admin.mapper.ServiceUpstreamMapper;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.query.ServiceInfoQuery;
import com.ishansong.diablo.admin.service.ServiceInfoService;
import com.ishansong.diablo.admin.vo.ServiceInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service("serviceInfoService")
public class ServiceInfoServiceImpl implements ServiceInfoService {

    private final ServiceInfoMapper serviceInfoMapper;

    private final ServiceUpstreamMapper serviceUpstreamMapper;

    @Autowired(required = false)
    public ServiceInfoServiceImpl(final ServiceInfoMapper serviceInfoMapper,
                                  final ServiceUpstreamMapper serviceUpstreamMapper) {
        this.serviceInfoMapper = serviceInfoMapper;
        this.serviceUpstreamMapper = serviceUpstreamMapper;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int createOrUpdate(final ServiceInfoDTO serviceInfoDTO) {
        int count;
        ServiceInfoDO serviceInfoDO = ServiceInfoDO.buildServiceInfoDO(serviceInfoDTO);

        if (StringUtils.isEmpty(serviceInfoDTO.getId())) {
            count = serviceInfoMapper.insertSelective(serviceInfoDO);
        } else {
            count = serviceInfoMapper.updateSelective(serviceInfoDO);
        }

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(final List<String> ids) {
        int count = 0;
        for (String id : ids) {
            count += serviceInfoMapper.delete(id);
        }
        return count;
    }

    @Override
    public ServiceInfoVO findById(final String id) {
        return ServiceInfoVO.buildServiceInfoVO(serviceInfoMapper.selectById(id));
    }

    @Override
    public CommonPager<ServiceInfoVO> listByPage(final ServiceInfoQuery serviceInfoQuery) {
        PageParameter pageParameter = serviceInfoQuery.getPageParameter();

        List<ServiceInfoDO> serviceInfoDOList = serviceInfoMapper.selectByQuery(serviceInfoQuery);

        List<ServiceInfoVO> result = serviceInfoDOList.stream()
                                                      .map(ServiceInfoVO::buildServiceInfoVO)
                                                      .collect(Collectors.toList());


        return new CommonPager<>(
                new PageParameter(pageParameter.getCurrentPage(), pageParameter.getPageSize(),
                        serviceInfoMapper.countByQuery(serviceInfoQuery)), result);
    }

    @Override
    public List<ServiceInfoVO> selectByQuery(ServiceInfoQuery serviceInfoQuery) {
        List<ServiceInfoDO> serviceInfoDOList = serviceInfoMapper.selectByQuery(serviceInfoQuery);

        List<ServiceInfoVO> result = serviceInfoDOList.stream()
                                                      .map(ServiceInfoVO::buildServiceInfoVO)
                                                      .collect(Collectors.toList());
        return result;
    }

    @Override
    public List<RuleUpstreamDTO> findUpstreamHandle(String serviceInfoId) {
        if (Strings.isNullOrEmpty(serviceInfoId)) {
            return Collections.emptyList();
        }

        ServiceInfoDO serviceInfoDO = serviceInfoMapper.selectById(serviceInfoId);
        if (serviceInfoDO == null) {
            return Collections.emptyList();
        }

        Integer port = serviceInfoDO.getPort();

        List<ServiceUpstreamDO> upstreams = serviceUpstreamMapper.selectIpsByServiceInfoId(serviceInfoId);

        if (CollectionUtils.isEmpty(upstreams)) {
            return Collections.emptyList();
        }

        return upstreams.stream()
                        .map(u -> new RuleUpstreamDTO(u.getHostName(), "", u.getHostIp() + ":" + port, 100))
                        .collect(Collectors.toList());
    }


}
