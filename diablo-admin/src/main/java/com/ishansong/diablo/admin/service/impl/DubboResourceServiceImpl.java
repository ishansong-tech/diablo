package com.ishansong.diablo.admin.service.impl;

import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import com.ishansong.diablo.admin.dto.DubboResourceDTO;
import com.ishansong.diablo.admin.entity.DubboResourceDO;
import com.ishansong.diablo.admin.mapper.DubboResourceMapper;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.query.DubboResourceQuery;
import com.ishansong.diablo.admin.service.DubboResourceService;
import com.ishansong.diablo.admin.utils.PreEnv;
import com.ishansong.diablo.admin.vo.DubboResourceDetailVO;
import com.ishansong.diablo.admin.vo.DubboResourceVO;
import com.ishansong.diablo.core.model.dubbo.ApiConfig;
import com.ishansong.diablo.core.model.dubbo.DubboExtConfig;
import com.ishansong.diablo.core.model.dubbo.DubboResourceData;
import com.ishansong.diablo.core.model.dubbo.ParamMetasData;
import com.ishansong.diablo.core.utils.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DubboResourceServiceImpl implements DubboResourceService {

    private final DubboResourceMapper dubboResourceMapper;

    private final com.ishansong.diablo.admin.pre.mapper.DubboResourceMapper preDubboResourceMapper;

    private final Type paramMetasType = new TypeToken<List<ParamMetasData>>() {
    }.getType();

    private final Type extConfigType = new TypeToken<DubboExtConfig>() {
    }.getType();

    private final Type apiConfigType = new TypeToken<ApiConfig>() {
    }.getType();

    @Autowired(required = false)
    public DubboResourceServiceImpl(final DubboResourceMapper dubboResourceMapper,
                                    final com.ishansong.diablo.admin.pre.mapper.DubboResourceMapper preDubboResourceMapper) {
        this.dubboResourceMapper = dubboResourceMapper;
        this.preDubboResourceMapper = preDubboResourceMapper;
    }

    @Override
    public List<DubboResourceData> listAll() {

        if (PreEnv.isPre()) {
            return dubboResourceMapper.selectAll()
                                      .stream()
                                      .filter(Objects::nonNull)
                                      .map(this::buildDubboResource)
                                      .collect(Collectors.toList());
        }

        return preDubboResourceMapper.selectAll()
                                     .stream()
                                     .filter(Objects::nonNull)
                                     .map(this::buildDubboResource)
                                     .collect(Collectors.toList());
    }

    @Override
    public CommonPager<DubboResourceVO> listByPage(DubboResourceQuery dubboResourceQuery) {

        if (PreEnv.isPre()) {
            List<DubboResourceVO> dubboResources = dubboResourceMapper.selectByPage(dubboResourceQuery)
                                                                      .stream()
                                                                      .filter(Objects::nonNull)
                                                                      .map(DubboResourceVO::buildDubboResourceVO)
                                                                      .collect(Collectors.toList());

            PageParameter pageParameter = dubboResourceQuery.getPageParameter();
            return new CommonPager<>(
                    new PageParameter(pageParameter.getCurrentPage(), pageParameter.getPageSize(), dubboResourceMapper.countByQuery(dubboResourceQuery)),
                    dubboResources);
        }


        List<DubboResourceVO> dubboResources = preDubboResourceMapper.selectByPage(dubboResourceQuery)
                                                                     .stream()
                                                                     .filter(Objects::nonNull)
                                                                     .map(DubboResourceVO::buildDubboResourceVO)
                                                                     .collect(Collectors.toList());

        PageParameter pageParameter = dubboResourceQuery.getPageParameter();
        return new CommonPager<>(
                new PageParameter(pageParameter.getCurrentPage(), pageParameter.getPageSize(), preDubboResourceMapper.countByQuery(dubboResourceQuery)),
                dubboResources);
    }

    @Override
    public DubboResourceDetailVO findById(String id) {
        return Optional.ofNullable(dubboResourceMapper.selectById(id)).map(DubboResourceDetailVO::buildDubboResourceVO).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int save(DubboResourceDTO dubboResourceDto) {

        DubboResourceDO dubboResourceDO = DubboResourceDO.buildDubboResourceDO(dubboResourceDto);

        return dubboResourceMapper.insertSelective(dubboResourceDO);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int update(DubboResourceDTO dubboResourceDto) {

        DubboResourceDO dubboResourceDO = DubboResourceDO.buildDubboResourceDO(dubboResourceDto);

        return dubboResourceMapper.updateSelective(dubboResourceDO);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int delete(List<String> ids) {

        ids.forEach(dubboResourceMapper::delete);
        return ids.size();
    }


    private DubboResourceData buildDubboResource(DubboResourceDO resource) {

        DubboResourceData dubboResourceData = new DubboResourceData(resource.getKey(), resource.getServiceName(),
                resource.getNamespace(), resource.getMethod(), resource.getObjectType(), resource.getEnabled());

        dubboResourceData.setParamMetas(GsonUtils.getInstance().fromJson(resource.getParamMetas(), paramMetasType));

        String extConfig = resource.getExtConfig();
        if (!Strings.isNullOrEmpty(extConfig)) {
            dubboResourceData.setDubboExtConfig(GsonUtils.getInstance().fromJson(extConfig, extConfigType));
        }

        String apiConfig = resource.getApiConfig();
        if (!Strings.isNullOrEmpty(apiConfig)) {
            dubboResourceData.setApiConfig(GsonUtils.getInstance().fromJson(apiConfig, apiConfigType));
        }

        return dubboResourceData;
    }
}
