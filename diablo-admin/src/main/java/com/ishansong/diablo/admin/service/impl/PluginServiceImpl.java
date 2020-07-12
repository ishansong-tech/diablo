package com.ishansong.diablo.admin.service.impl;

import com.ishansong.diablo.admin.dto.PluginDTO;
import com.ishansong.diablo.admin.entity.RuleDO;
import com.ishansong.diablo.admin.entity.SelectorDO;
import com.ishansong.diablo.admin.mapper.SelectorConditionMapper;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.query.RuleConditionQuery;
import com.ishansong.diablo.admin.entity.PluginDO;
import com.ishansong.diablo.admin.listener.DataChangedEvent;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.service.PluginService;
import com.ishansong.diablo.admin.transfer.PluginTransfer;
import com.ishansong.diablo.admin.vo.PluginVO;
import com.ishansong.diablo.core.constant.AdminConstants;
import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.enums.DataEventTypeEnum;
import com.ishansong.diablo.core.enums.PluginRoleEnum;
import com.ishansong.diablo.core.model.plugin.PluginData;
import org.apache.commons.lang3.StringUtils;
import com.ishansong.diablo.admin.mapper.PluginMapper;
import com.ishansong.diablo.admin.mapper.RuleConditionMapper;
import com.ishansong.diablo.admin.mapper.RuleMapper;
import com.ishansong.diablo.admin.mapper.SelectorMapper;
import com.ishansong.diablo.admin.query.PluginQuery;
import com.ishansong.diablo.admin.query.RuleQuery;
import com.ishansong.diablo.admin.query.SelectorConditionQuery;
import com.ishansong.diablo.admin.query.SelectorQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("pluginService")
public class PluginServiceImpl implements PluginService {

    private final PluginMapper pluginMapper;

    private final SelectorMapper selectorMapper;

    private SelectorConditionMapper selectorConditionMapper;

    private final RuleMapper ruleMapper;

    private final RuleConditionMapper ruleConditionMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    public PluginServiceImpl(final PluginMapper pluginMapper,
                             final SelectorMapper selectorMapper,
                             final SelectorConditionMapper selectorConditionMapper,
                             final RuleMapper ruleMapper,
                             final RuleConditionMapper ruleConditionMapper,
                             final ApplicationEventPublisher eventPublisher) {
        this.pluginMapper = pluginMapper;
        this.selectorMapper = selectorMapper;
        this.selectorConditionMapper = selectorConditionMapper;
        this.ruleMapper = ruleMapper;
        this.ruleConditionMapper = ruleConditionMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrUpdate(final PluginDTO pluginDTO) {
        final String msg = checkData(pluginDTO);
        if (StringUtils.isNoneBlank(msg)) {
            return msg;
        }
        PluginDO pluginDO = PluginDO.buildPluginDO(pluginDTO);
        DataEventTypeEnum eventType = DataEventTypeEnum.CREATE;
        if (StringUtils.isBlank(pluginDTO.getId())) {
            pluginMapper.insertSelective(pluginDO);
        } else {
            eventType = DataEventTypeEnum.UPDATE;
            pluginMapper.updateSelective(pluginDO);
        }

        // publish change event.
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.PLUGIN, eventType,
                Collections.singletonList(PluginTransfer.INSTANCE.mapToData(pluginDO))));
        return StringUtils.EMPTY;
    }

    private String checkData(final PluginDTO pluginDTO) {
        final PluginDO exist = pluginMapper.selectByName(pluginDTO.getName());
        if (StringUtils.isBlank(pluginDTO.getId())) {
            if (Objects.nonNull(exist)) {
                return AdminConstants.PLUGIN_NAME_IS_EXIST;
            }
        } else {
            if (Objects.isNull(exist) || !exist.getId().equals(pluginDTO.getId())) {
                return AdminConstants.PLUGIN_NAME_NOT_EXIST;
            }
        }
        return StringUtils.EMPTY;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(final List<String> ids) {
        for (String id : ids) {
            PluginDO pluginDO = pluginMapper.selectById(id);
            if (Objects.isNull(pluginDO)) {
                return AdminConstants.SYS_PLUGIN_ID_NOT_EXIST;
            }
            // if sys plugin not delete
            if (pluginDO.getRole().equals(PluginRoleEnum.SYS.getCode())) {
                return AdminConstants.SYS_PLUGIN_NOT_DELETE;
            }
            pluginMapper.delete(id);

            final List<SelectorDO> selectorDOList = selectorMapper.selectByQuery(new SelectorQuery(id, null));
            selectorDOList.forEach(selectorDO -> {
                final List<RuleDO> ruleDOS = ruleMapper.selectByQuery(new RuleQuery(selectorDO.getId(), null));
                ruleDOS.forEach(ruleDO -> {
                    ruleMapper.delete(ruleDO.getId());
                    ruleConditionMapper.deleteByQuery(new RuleConditionQuery(ruleDO.getId()));
                });
                selectorMapper.delete(selectorDO.getId());
                selectorConditionMapper.deleteByQuery(new SelectorConditionQuery(selectorDO.getId()));
            });
            // publish change event.
            eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.PLUGIN, DataEventTypeEnum.DELETE,
                    Collections.singletonList(PluginTransfer.INSTANCE.mapToData(pluginDO))));
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String enabled(final List<String> ids, final Boolean enabled) {
        for (String id : ids) {
            PluginDO pluginDO = pluginMapper.selectById(id);
            if (Objects.isNull(pluginDO)) {
                return AdminConstants.SYS_PLUGIN_ID_NOT_EXIST;
            }
            pluginDO.setDateUpdated(new Timestamp(System.currentTimeMillis()));
            pluginDO.setEnabled(enabled);
            pluginMapper.updateEnable(pluginDO);

            // publish change event.
            eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.PLUGIN, DataEventTypeEnum.UPDATE,
                    Collections.singletonList(PluginTransfer.INSTANCE.mapToData(pluginDO))));
        }
        return StringUtils.EMPTY;
    }

    @Override
    public PluginVO findById(final String id) {
        return PluginVO.buildPluginVO(pluginMapper.selectById(id));
    }

    @Override
    public CommonPager<PluginVO> listByPage(final PluginQuery pluginQuery) {
        PageParameter pageParameter = pluginQuery.getPageParameter();
        return new CommonPager<>(
                new PageParameter(pageParameter.getCurrentPage(), pageParameter.getPageSize(),
                        pluginMapper.countByQuery(pluginQuery)),
                pluginMapper.selectByQuery(pluginQuery).stream()
                        .map(PluginVO::buildPluginVO)
                        .collect(Collectors.toList()));
    }

    @Override
    public List<PluginData> listAll() {
        return pluginMapper.selectAll().stream()
                .map(PluginTransfer.INSTANCE::mapToData)
                .collect(Collectors.toList());
    }
}
