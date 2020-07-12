package com.ishansong.diablo.admin.service.impl;

import com.ishansong.diablo.admin.dto.SelectorConditionDTO;
import com.ishansong.diablo.admin.dto.SelectorDTO;
import com.ishansong.diablo.admin.entity.*;
import com.ishansong.diablo.admin.listener.DataChangedEvent;
import com.ishansong.diablo.admin.mapper.*;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.query.RuleConditionQuery;
import com.ishansong.diablo.admin.query.RuleQuery;
import com.ishansong.diablo.admin.query.SelectorConditionQuery;
import com.ishansong.diablo.admin.query.SelectorQuery;
import com.ishansong.diablo.admin.service.SelectorService;
import com.ishansong.diablo.admin.transfer.ConditionTransfer;
import com.ishansong.diablo.admin.utils.PreEnv;
import com.ishansong.diablo.admin.vo.SelectorConditionVO;
import com.ishansong.diablo.admin.vo.SelectorVO;
import com.ishansong.diablo.admin.entity.*;
import com.ishansong.diablo.admin.mapper.*;
import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.enums.DataEventTypeEnum;
import com.ishansong.diablo.core.model.condition.ConditionData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service("selectorService")
public class SelectorServiceImpl implements SelectorService {

    private SelectorMapper selectorMapper;

    private SelectorConditionMapper selectorConditionMapper;

    private final RuleMapper ruleMapper;

    private final RuleConditionMapper ruleConditionMapper;

    private final PluginMapper pluginMapper;

    private final ApplicationEventPublisher eventPublisher;

    private com.ishansong.diablo.admin.pre.mapper.SelectorMapper preSelectorMapper;

    @Autowired(required = false)
    public SelectorServiceImpl(final SelectorMapper selectorMapper,
                               final SelectorConditionMapper selectorConditionMapper,
                               final PluginMapper pluginMapper,
                               final RuleMapper ruleMapper,
                               final RuleConditionMapper ruleConditionMapper,
                               final ApplicationEventPublisher eventPublisher,
                               final com.ishansong.diablo.admin.pre.mapper.SelectorMapper preSelectorMapper) {
        this.selectorMapper = selectorMapper;
        this.selectorConditionMapper = selectorConditionMapper;
        this.pluginMapper = pluginMapper;
        this.ruleMapper = ruleMapper;
        this.ruleConditionMapper = ruleConditionMapper;
        this.eventPublisher = eventPublisher;
        this.preSelectorMapper = preSelectorMapper;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int createOrUpdate(final SelectorDTO selectorDTO) {
        int selectorCount;
        SelectorDO selectorDO = SelectorDO.buildSelectorDO(selectorDTO);
        List<SelectorConditionDTO> selectorConditionDTOs = selectorDTO.getSelectorConditions();
        if (StringUtils.isEmpty(selectorDTO.getId())) {
            selectorCount = selectorMapper.insertSelective(selectorDO);
            selectorConditionDTOs.forEach(selectorConditionDTO -> {
                selectorConditionDTO.setSelectorId(selectorDO.getId());
                selectorConditionMapper.insertSelective(SelectorConditionDO.buildSelectorConditionDO(selectorConditionDTO));
            });
        } else {
            selectorCount = selectorMapper.updateSelective(selectorDO);
            //delete rule condition then add
            selectorConditionMapper.deleteByQuery(new SelectorConditionQuery(selectorDO.getId()));
            selectorConditionDTOs.forEach(selectorConditionDTO -> {
                selectorConditionDTO.setSelectorId(selectorDO.getId());
                SelectorConditionDO selectorConditionDO = SelectorConditionDO.buildSelectorConditionDO(selectorConditionDTO);
                selectorConditionMapper.insertSelective(selectorConditionDO);
            });
        }
        PluginDO pluginDO = pluginMapper.selectById(selectorDO.getPluginId());
        List<ConditionData> conditionDataList =
                selectorConditionDTOs.stream().map(ConditionTransfer.INSTANCE::mapToSelectorDTO).collect(Collectors.toList());
        // publish change event.
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.SELECTOR, DataEventTypeEnum.UPDATE,
                Collections.singletonList(SelectorDO.transFrom(selectorDO, pluginDO.getName(), conditionDataList))));
        return selectorCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(final List<String> ids) {
        for (String id : ids) {

            SelectorDO selectorDO = selectorMapper.selectById(id);
            PluginDO pluginDO = pluginMapper.selectById(selectorDO.getPluginId());

            selectorMapper.delete(id);
            selectorConditionMapper.deleteByQuery(new SelectorConditionQuery(id));

            //发送删除选择器事件
            eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.SELECTOR, DataEventTypeEnum.DELETE,
                    Collections.singletonList(SelectorDO.transFrom(selectorDO, pluginDO.getName(), null))));

            //清除规则与规则条件
            final List<RuleDO> ruleDOList = ruleMapper.selectByQuery(new RuleQuery(id, null));
            if (CollectionUtils.isNotEmpty(ruleDOList)) {
                for (RuleDO ruleDO : ruleDOList) {
                    ruleMapper.delete(ruleDO.getId());
                    ruleConditionMapper.deleteByQuery(new RuleConditionQuery(ruleDO.getId()));
                    //发送删除选择器事件
                    eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.RULE, DataEventTypeEnum.DELETE,
                            Collections.singletonList(RuleDO.transFrom(ruleDO, pluginDO.getName(), null))));

                }
            }
        }
        return ids.size();
    }

    @Override
    public SelectorVO findById(final String id) {
        return SelectorVO.buildSelectorVO(selectorMapper.selectById(id),
                selectorConditionMapper.selectByQuery(
                        new SelectorConditionQuery(id))
                                       .stream()
                                       .map(SelectorConditionVO::buildSelectorConditionVO)
                                       .collect(Collectors.toList()));
    }

    @Override
    public CommonPager<SelectorVO> listByPage(final SelectorQuery selectorQuery) {
        PageParameter pageParameter = selectorQuery.getPageParameter();

        List<SelectorDO> selectorDOS = selectorMapper.selectByQuery(selectorQuery);


        List<String> selectorIds = selectorDOS.stream().map(SelectorDO::getId).collect(Collectors.toList());

        Map<String, SelectorPublishDO> publishTimes;
        if (PreEnv.isPre()) {
            publishTimes = preSelectorMapper.selectBySelectorIds(selectorIds);
        } else {
            publishTimes = selectorMapper.selectBySelectorIds(selectorIds);
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

        List<SelectorVO> result = selectorDOS.stream()
                                             .map(SelectorVO::buildSelectorVO)
                                             .map(t -> t.setDatePublished(Optional.ofNullable(publishTimes)
                                                                                  .map(m -> m.get(t.getId()))
                                                                                  .map(SelectorPublishDO::getDatePublished)
                                                                                  .map(Timestamp::toLocalDateTime)
                                                                                  .map(dateTimeFormatter::format)
                                                                                  .orElse("-")))
                                             .map(t -> t.setDateRollbacked(Optional.ofNullable(publishTimes)
                                                                                   .map(m -> m.get(t.getId()))
                                                                                   .map(SelectorPublishDO::getDateRollbacked)
                                                                                   .map(Timestamp::toLocalDateTime)
                                                                                   .map(dateTimeFormatter::format)
                                                                                   .orElse("-")))
                                             .collect(Collectors.toList());


        return new CommonPager<>(
                new PageParameter(pageParameter.getCurrentPage(), pageParameter.getPageSize(),
                        selectorMapper.countByQuery(selectorQuery)), result);
    }

    @Override
    public List<SelectorData> findByPluginId(String pluginId) {
        return selectorMapper.findByPluginId(pluginId)
                             .stream()
                             .map(this::buildSelectorData)
                             .collect(Collectors.toList());
    }

    @Override
    public List<SelectorData> listAll() {
        return selectorMapper.selectAll()
                             .stream()
                             .filter(Objects::nonNull)
                             .map(this::buildSelectorData)
                             .collect(Collectors.toList());
    }

    private SelectorData buildSelectorData(final SelectorDO selectorDO) {
        // find conditions
        List<ConditionData> conditionDataList = selectorConditionMapper
                .selectByQuery(new SelectorConditionQuery(selectorDO.getId()))
                .stream()
                .filter(Objects::nonNull)
                .map(ConditionTransfer.INSTANCE::mapToSelectorDO)
                .collect(Collectors.toList());
        PluginDO pluginDO = pluginMapper.selectById(selectorDO.getPluginId());
        if (Objects.isNull(pluginDO)) {
            return null;
        }
        return SelectorDO.transFrom(selectorDO, pluginDO.getName(), conditionDataList);
    }

}
