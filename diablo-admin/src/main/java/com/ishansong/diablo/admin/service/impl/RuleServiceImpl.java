package com.ishansong.diablo.admin.service.impl;

import com.ishansong.diablo.admin.dto.RuleConditionDTO;
import com.ishansong.diablo.admin.dto.RuleDTO;
import com.ishansong.diablo.admin.dto.RuleUpstreamDTO;
import com.ishansong.diablo.admin.entity.*;
import com.ishansong.diablo.admin.listener.DataChangedEvent;
import com.ishansong.diablo.admin.mapper.*;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.page.RuleCommonPager;
import com.ishansong.diablo.admin.query.RuleConditionQuery;
import com.ishansong.diablo.admin.query.RuleQuery;
import com.ishansong.diablo.admin.service.RuleService;
import com.ishansong.diablo.admin.service.ServiceInfoService;
import com.ishansong.diablo.admin.transfer.ConditionTransfer;
import com.ishansong.diablo.admin.utils.PreEnv;
import com.ishansong.diablo.admin.vo.RuleConditionVO;
import com.ishansong.diablo.admin.vo.RuleVO;
import com.ishansong.diablo.admin.vo.ServiceAppVO;
import com.ishansong.diablo.admin.entity.*;
import com.ishansong.diablo.admin.mapper.*;
import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.enums.DataEventTypeEnum;
import com.ishansong.diablo.core.model.condition.ConditionData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.utils.GsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service("ruleService")
public class RuleServiceImpl implements RuleService {

    private final RuleMapper ruleMapper;

    private final RuleConditionMapper ruleConditionMapper;

    private final SelectorMapper selectorMapper;

    private final ServiceInfoMapper serviceInfoMapper;

    private final ServiceUpstreamMapper serviceUpstreamMapper;

    private final com.ishansong.diablo.admin.pre.mapper.ServiceInfoMapper preServiceInfoMapper;

    private final PluginMapper pluginMapper;

    private final ServiceInfoService serviceInfoService;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    public RuleServiceImpl(final RuleMapper ruleMapper,
                           final RuleConditionMapper ruleConditionMapper,
                           final SelectorMapper selectorMapper,
                           final ServiceInfoMapper serviceInfoMapper,
                           final ServiceUpstreamMapper serviceUpstreamMapper,
                           final com.ishansong.diablo.admin.pre.mapper.ServiceInfoMapper preServiceInfoMapper,
                           final PluginMapper pluginMapper,
                           final ServiceInfoService serviceInfoService,
                           final ApplicationEventPublisher eventPublisher) {
        this.ruleMapper = ruleMapper;
        this.ruleConditionMapper = ruleConditionMapper;
        this.selectorMapper = selectorMapper;
        this.serviceInfoMapper = serviceInfoMapper;
        this.serviceUpstreamMapper = serviceUpstreamMapper;
        this.preServiceInfoMapper = preServiceInfoMapper;
        this.pluginMapper = pluginMapper;
        this.serviceInfoService = serviceInfoService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createOrUpdate(final RuleDTO ruleDTO) {

        int ruleCount;
        RuleDO ruleDO = RuleDO.buildRuleDO(ruleDTO);

        // fetch service info, assign upstream handle
        List<RuleUpstreamDTO> ruleUpstreams = serviceInfoService.findUpstreamHandle(ruleDO.getServiceInfoId());
        if (!CollectionUtils.isEmpty(ruleUpstreams)) {
            ruleDO.setUpstreamHandle(GsonUtils.getInstance().toJson(ruleUpstreams));
        }

        List<RuleConditionDTO> ruleConditions = ruleDTO.getRuleConditions();
        if (StringUtils.isEmpty(ruleDTO.getId())) {
            ruleCount = ruleMapper.insertSelective(ruleDO);
            ruleConditions.forEach(ruleConditionDTO -> {
                ruleConditionDTO.setRuleId(ruleDO.getId());
                ruleConditionMapper.insertSelective(RuleConditionDO.buildRuleConditionDO(ruleConditionDTO));
            });
        } else {
            ruleCount = ruleMapper.updateSelective(ruleDO);
            //delete rule condition then add
            ruleConditionMapper.deleteByQuery(new RuleConditionQuery(ruleDO.getId()));
            ruleConditions.forEach(ruleConditionDTO -> {
                ruleConditionDTO.setRuleId(ruleDO.getId());
                RuleConditionDO ruleConditionDO = RuleConditionDO.buildRuleConditionDO(ruleConditionDTO);
                ruleConditionMapper.insertSelective(ruleConditionDO);
            });
        }

        SelectorDO selectorDO = selectorMapper.selectById(ruleDO.getSelectorId());
        PluginDO pluginDO = pluginMapper.selectById(selectorDO.getPluginId());

        List<ConditionData> conditionDataList =
                ruleConditions.stream().map(ConditionTransfer.INSTANCE::mapToRuleDTO).collect(Collectors.toList());
        // publish change event.
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.RULE, DataEventTypeEnum.UPDATE,
                Collections.singletonList(RuleDO.transFrom(ruleDO, pluginDO.getName(), conditionDataList))));

        return ruleCount;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public int updateByServiceInfoId(String serviceInfoId, String upstreamHandle) {

        int ruleCount = ruleMapper.updateByServiceInfoId(serviceInfoId, upstreamHandle);

        return ruleCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(final List<String> ids) {
        for (String id : ids) {
            RuleDO ruleDO = ruleMapper.selectById(id);
            SelectorDO selectorDO = selectorMapper.selectById(ruleDO.getSelectorId());
            PluginDO pluginDO = pluginMapper.selectById(selectorDO.getPluginId());
            ruleMapper.delete(id);
            ruleConditionMapper.deleteByQuery(new RuleConditionQuery(id));

            //发送删规则事件
            eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.RULE, DataEventTypeEnum.DELETE,
                    Collections.singletonList(RuleDO.transFrom(ruleDO, pluginDO.getName(), null))));
        }
        return ids.size();
    }

    @Override
    public RuleVO findById(final String id) {

        List<ServiceAppVO> serviceApps;
        if (PreEnv.isPre()) {
            serviceApps = Optional.ofNullable(serviceInfoMapper.selectAll()).map(s -> s.stream().map(ServiceAppVO::buildServiceAppVO).collect(Collectors.toList())).orElse(Collections.emptyList());
        } else {
            serviceApps = Collections.emptyList();
        }

        RuleVO ruleVO = RuleVO.buildRuleVO(ruleMapper.selectById(id),
                ruleConditionMapper.selectByQuery(new RuleConditionQuery(id)).stream().map(RuleConditionVO::buildRuleConditionVO).collect(Collectors.toList()),
                serviceApps);

        String serviceInfoId = ruleVO.getServiceInfoId();
        if (!PreEnv.isPre()) {
            ruleVO.setServiceApp(Optional.ofNullable(preServiceInfoMapper.selectById(serviceInfoId)).map(ServiceAppVO::buildServiceAppVO).map(ServiceAppVO::getName).orElse("-"));
        }

        return ruleVO;
    }

    @Override
    public RuleCommonPager<RuleVO> listByPage(final RuleQuery ruleQuery) {

        List<ServiceAppVO> serviceApps;
        if (PreEnv.isPre()) {

            Map<String, ServiceInfoDO> serviceInfoMap = serviceInfoMapper.selectAllMap();

            if (!CollectionUtils.isEmpty(serviceInfoMap)) {

                Collection<ServiceInfoDO> serviceInfos = serviceInfoMap.values();

                List<String> serviceInfoIds = new ArrayList<>(serviceInfoMap.keySet());
                List<ServiceUpstreamDO> upstreamList = serviceUpstreamMapper.selectByServiceInfoIds(serviceInfoIds);
                Map<String, List<ServiceUpstreamDO>> upstreams = upstreamList.stream().collect(Collectors.groupingBy(ServiceUpstreamDO::getServiceInfoId));

                serviceApps = serviceInfos.stream().map(ServiceAppVO::buildServiceAppVO).map(s -> {
                    Integer port = Optional.ofNullable(serviceInfoMap.get(s.getId())).map(ServiceInfoDO::getPort).orElse(null);

                    if (port != null) {

                        List<RuleUpstreamDTO> ruleUpstreams = Optional.ofNullable(upstreams).map(u -> u.get(s.getId())).map(l -> l.stream().map(u -> u.setPort(port)).map(RuleUpstreamDTO::buildRuleUpstream).collect(Collectors.toList())).orElse(Collections.emptyList());
                        s.setRuleUpstreams(ruleUpstreams);
                    }
                    return s;
                }).sorted(Comparator.comparing(ServiceAppVO::getName)).collect(Collectors.toList());
            } else {
                serviceApps = Collections.emptyList();
            }
        } else {
            serviceApps = Collections.emptyList();
        }

        PageParameter pageParameter = ruleQuery.getPageParameter();
        return new RuleCommonPager<>(
                new PageParameter(pageParameter.getCurrentPage(),
                        pageParameter.getPageSize(), ruleMapper.countByQuery(ruleQuery)),
                ruleMapper.selectByQuery(ruleQuery).stream()
                          .map(RuleVO::buildRuleVO)
                          .collect(Collectors.toList()),
                serviceApps);
    }

    @Override
    public List<RuleData> listAll() {
        return ruleMapper.selectAll()
                         .stream()
                         .filter(Objects::nonNull)
                         .map(this::buildRuleData)
                         .collect(Collectors.toList());
    }

    @Override
    public List<RuleData> findBySelectorId(String selectorId) {
        return ruleMapper.findBySelectorId(selectorId)
                         .stream()
                         .filter(Objects::nonNull)
                         .map(this::buildRuleData)
                         .collect(Collectors.toList());
    }

    private RuleData buildRuleData(final RuleDO ruleDO) {
        // query for conditions
        List<ConditionData> conditions = ruleConditionMapper.selectByQuery(
                new RuleConditionQuery(ruleDO.getId()))
                                                            .stream()
                                                            .filter(Objects::nonNull)
                                                            .map(ConditionTransfer.INSTANCE::mapToRuleDO)
                                                            .collect(Collectors.toList());
        SelectorDO selectorDO = selectorMapper.selectById(ruleDO.getSelectorId());
        if (Objects.isNull(selectorDO)) {
            return null;
        }
        PluginDO pluginDO = pluginMapper.selectById(selectorDO.getPluginId());
        if (Objects.isNull(pluginDO)) {
            return null;
        }
        return RuleDO.transFrom(ruleDO, pluginDO.getName(), conditions);
    }

}
