package com.ishansong.diablo.admin.pre.service.impl;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.AbstractMessage;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.gson.reflect.TypeToken;
import com.ishansong.diablo.admin.dto.RuleUpstreamDTO;
import com.ishansong.diablo.admin.entity.*;
import com.ishansong.diablo.admin.mapper.RuleConditionMapper;
import com.ishansong.diablo.admin.mapper.RuleMapper;
import com.ishansong.diablo.admin.mapper.SelectorConditionMapper;
import com.ishansong.diablo.admin.mapper.SelectorMapper;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.pre.service.ConfigSubscribeService;
import com.ishansong.diablo.admin.pre.service.PrepareService;
import com.ishansong.diablo.admin.pre.service.PrepareTransferService;
import com.ishansong.diablo.admin.query.SelectorConditionQuery;
import com.ishansong.diablo.admin.query.SelectorVersionQuery;
import com.ishansong.diablo.admin.service.RuleService;
import com.ishansong.diablo.admin.service.ServiceInfoService;
import com.ishansong.diablo.admin.transfer.SelectorBackupTransfer;
import com.ishansong.diablo.admin.utils.PreEnv;
import com.ishansong.diablo.admin.vo.SelectorBackupVO;
import com.ishansong.diablo.admin.vo.SelectorBackupVersionVO;
import com.ishansong.diablo.admin.entity.*;
import com.ishansong.diablo.core.constant.AdminConstants;
import com.ishansong.diablo.core.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PrepareServiceImpl implements PrepareService {

    private SelectorMapper selectorMapper;

    private SelectorConditionMapper selectorConditionMapper;

    private final RuleMapper ruleMapper;

    private final RuleConditionMapper ruleConditionMapper;

    private final RuleService ruleService;

    private PrepareTransferService prepareTransferService;

    private ConfigSubscribeService configSubscribeService;

    private final ServiceInfoService serviceInfoService;

    private com.ishansong.diablo.admin.pre.mapper.backup.SelectorMapper backupSelectorMapper;

    private com.ishansong.diablo.admin.pre.mapper.backup.SelectorConditionMapper backupSelectorConditionMapper;

    private final com.ishansong.diablo.admin.pre.mapper.backup.RuleMapper backupRuleMapper;

    private final com.ishansong.diablo.admin.pre.mapper.backup.RuleConditionMapper backupRuleConditionMapper;

    private com.ishansong.diablo.admin.pre.mapper.SelectorMapper preSelectorMapper;

    private com.ishansong.diablo.admin.pre.mapper.SelectorConditionMapper preSelectorConditionMapper;

    private final com.ishansong.diablo.admin.pre.mapper.RuleMapper preRuleMapper;

    private final com.ishansong.diablo.admin.pre.mapper.RuleConditionMapper preRuleConditionMapper;

    @Autowired(required = false)
    public PrepareServiceImpl(SelectorMapper selectorMapper, SelectorConditionMapper selectorConditionMapper, RuleMapper ruleMapper, RuleConditionMapper ruleConditionMapper, RuleService ruleService,
                              PrepareTransferService prepareTransferService, ConfigSubscribeService configSubscribeService, ServiceInfoService serviceInfoService,
                              com.ishansong.diablo.admin.pre.mapper.backup.SelectorMapper backupSelectorMapper, com.ishansong.diablo.admin.pre.mapper.backup.SelectorConditionMapper backupSelectorConditionMapper,
                              com.ishansong.diablo.admin.pre.mapper.backup.RuleMapper backupRuleMapper, com.ishansong.diablo.admin.pre.mapper.backup.RuleConditionMapper backupRuleConditionMapper,
                              com.ishansong.diablo.admin.pre.mapper.SelectorMapper preSelectorMapper, com.ishansong.diablo.admin.pre.mapper.SelectorConditionMapper preSelectorConditionMapper,
                              com.ishansong.diablo.admin.pre.mapper.RuleMapper preRuleMapper, com.ishansong.diablo.admin.pre.mapper.RuleConditionMapper preRuleConditionMapper) {
        this.selectorMapper = selectorMapper;
        this.selectorConditionMapper = selectorConditionMapper;
        this.ruleMapper = ruleMapper;
        this.ruleConditionMapper = ruleConditionMapper;
        this.ruleService = ruleService;
        this.prepareTransferService = prepareTransferService;
        this.configSubscribeService = configSubscribeService;
        this.serviceInfoService = serviceInfoService;
        this.backupSelectorMapper = backupSelectorMapper;
        this.backupSelectorConditionMapper = backupSelectorConditionMapper;
        this.backupRuleMapper = backupRuleMapper;
        this.backupRuleConditionMapper = backupRuleConditionMapper;
        this.preSelectorMapper = preSelectorMapper;
        this.preSelectorConditionMapper = preSelectorConditionMapper;
        this.preRuleMapper = preRuleMapper;
        this.preRuleConditionMapper = preRuleConditionMapper;
    }


    @Override
    public Map<String, String> publish(List<String> ids, String remark) {
        return publish(ids, remark, false);
    }

    /**
     * 预发环境执行的操作 pre环境数据库配置为 pre为diablo, online为diablo_pre
     *
     * @param ids
     * @return
     */
    @Override
    public Map<String, String> publish(List<String> ids, String remark, Boolean autoSync) {

        Map<String, String> result = new HashMap<>();

        long nanoTime = System.nanoTime();
        Stopwatch started = Stopwatch.createStarted();

        List<SelectorDO> syncSelectorDTOS = selectorMapper.findByIds(ids);

        if (CollectionUtils.isEmpty(syncSelectorDTOS)) {
            log.warn("PrepareService publish waring syncSelectorDTOS is empty, syncSelectorId={}", ids);

            result.put(AdminConstants.OPERATE_RESULT_CODE, AdminConstants.SELECOTR_NOT_EXIST);
            return result;
        }
        // 暂时通过删除和新增事务完成, 综合比较此方案优于新增和修改,测试容错性和并发性assemblySyncList(syncSelectorDTOS, onlineSelectorDOS, syncInsertSelector, syncUpdateSelector);

        for (Iterator<SelectorDO> iterator = syncSelectorDTOS.iterator(); iterator.hasNext(); ) {

            SelectorDO syncSelectorDO = iterator.next();

            Timestamp datePublished = new Timestamp(System.currentTimeMillis());
            syncSelectorDO.setDatePublished(datePublished);

            String syncSelectorId = syncSelectorDO.getId();
            // selector conditions
            List<SelectorConditionDO> syncSelectorConditions = selectorConditionMapper.selectByQuery(new SelectorConditionQuery(syncSelectorId));

            // selector rules
            List<RuleDO> syncSelectorRules = ruleMapper.findBySelectorId(syncSelectorId);
            List<String> syncRuleIds = null;
            List<RuleConditionDO> syncRuleConditions = null;
            if (CollectionUtils.isNotEmpty(syncSelectorRules)) {
                syncRuleIds = syncSelectorRules.stream().map(RuleDO::getId).collect(Collectors.toList());
                // selector rule conditions
                syncRuleConditions = ruleConditionMapper.findByRuleIds(syncRuleIds);
            }

            // fetch backup online selector for roll-back
            SelectorDO backupSelectorDO = preSelectorMapper.selectById(syncSelectorId);
            List<SelectorConditionDO> backupSelectorConditions = preSelectorConditionMapper.selectBySelectorId(syncSelectorId);
            List<RuleDO> backupRules = preRuleMapper.findBySelectorId(syncSelectorId);

            List<String> backupRuleIds = null;
            List<RuleConditionDO> backupRuleConditions = null;
            if (CollectionUtils.isNotEmpty(backupRules)) {
                backupRuleIds = backupRules.stream().map(RuleDO::getId).collect(Collectors.toList());

                backupRuleConditions = preRuleConditionMapper.findByRuleIds(backupRuleIds);
            }

            if (backupSelectorDO != null) {
                syncSelectorDO.setDateRollbacked(backupSelectorDO.getDateRollbacked());

                backupSelectorDO.setRemark(remark);
                backupSelectorDO.setDatePublished(datePublished);
                if (CollectionUtils.isNotEmpty(backupSelectorConditions)) {
                    backupSelectorConditions.forEach(s -> s.setDatePublished(datePublished));
                }

                if (CollectionUtils.isNotEmpty(backupRules)) {
                    backupRules.forEach(r -> r.setDatePublished(datePublished));
                }

                if (CollectionUtils.isNotEmpty(backupRuleConditions)) {
                    backupRuleConditions.forEach(rc -> rc.setDatePublished(datePublished));
                }
            }

            try {
                // insert selector/selectorCondition/rule/ruleCondition, confirm Transactional online database
                Map<String, Integer> transferResult = prepareTransferService.transferSelector(syncSelectorId, syncSelectorDO, syncSelectorConditions, syncSelectorRules, syncRuleIds, syncRuleConditions,
                        backupRuleIds, backupSelectorDO, backupSelectorConditions, backupRules, backupRuleConditions);

                result.putAll(transferResult.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))));

                if (!autoSync) {
                    Map<String, Object> message = new HashMap<>();
                    message.put(AdminConstants.NOTICE_MESSAGE_TIMESTAMP, nanoTime);

                    try {
                        configSubscribeService.notifyChanged(GsonUtils.getInstance().toJson(message));

                        reportedTransaction(autoSync, "publish", Message.SUCCESS, nanoTime, result);
                    } catch (Exception e) {

                        String trace = Throwables.getStackTraceAsString(e);
                        log.error("PrepareService publish notifyChanged failed, syncSelectorId={}, syncRuleIds={}, cause={}", syncSelectorId, syncRuleIds, trace);

                        result.put(AdminConstants.OPERATE_RESULT_CODE, AdminConstants.NOTICE_FAILED);

                        reportedTransaction(autoSync, "publish-notifyChanged", trace, nanoTime, result);
                    }
                }
            } catch (Exception e) {
                String trace = Throwables.getStackTraceAsString(e);
                log.error("PrepareService publish transferSelector failed, syncSelectorId={}, cause={}", syncSelectorId, trace);

                reportedTransaction(autoSync, "publish", trace, nanoTime, result);
                throw e;
            }

            result.put(AdminConstants.SELECTOR_NAME, syncSelectorDO.getName());
        }

        started.stop();
        // warn level
        log.warn("PrepareService publish finish, syncSelectorId={}, result={}, elapsed={}", ids, result, started.elapsed(TimeUnit.MILLISECONDS));

        return result;
    }

    @Override
    public Map<String, String> rollback(String id, Long timestamp) {

        Map<String, String> result = new HashMap<>();

        long nanoTime = System.nanoTime();
        Stopwatch started = Stopwatch.createStarted();

        Timestamp datePublished = new Timestamp(timestamp);

        SelectorDO backupSelector = backupSelectorMapper.findByIdAndDatePublished(id, datePublished);

        if (backupSelector == null) {
            log.warn("PrepareService rollback waring backupSelectors is empty, syncSelectorId={}", id);

            result.put(AdminConstants.OPERATE_RESULT_CODE, AdminConstants.BACKUP_SELECOTR_NOT_EXIST);
            return result;
        }


        String backupSelectorId = backupSelector.getId();

        List<SelectorConditionDO> backupSelectorConditions = backupSelectorConditionMapper.selectBySelectorIdAndDatePublished(backupSelectorId, datePublished);
        List<RuleDO> backupRules = backupRuleMapper.findBySelectorIdAndDatePublished(backupSelectorId, datePublished);

        List<String> backupRuleIds = null;
        List<RuleConditionDO> backupRuleConditions = null;
        if (CollectionUtils.isNotEmpty(backupRules)) {
            backupRuleIds = backupRules.stream().map(RuleDO::getId).collect(Collectors.toList());

            backupRuleConditions = backupRuleConditionMapper.findByRuleIdsAndDatePublished(backupRuleIds, datePublished);
        }

        try {
            Map<String, Integer> rollbackResult = prepareTransferService.rollbackSelector(backupSelectorId, backupRuleIds, backupSelector, backupSelectorConditions, backupRules, backupRuleConditions);
            result.putAll(rollbackResult.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))));

            Map<String, Object> message = new HashMap<>();
            message.put(AdminConstants.NOTICE_MESSAGE_TIMESTAMP, nanoTime);

            try {
                configSubscribeService.notifyChanged(GsonUtils.getInstance().toJson(message));

                reportedTransaction(false, "rollback", Message.SUCCESS, nanoTime, result);
            } catch (Exception e) {

                String trace = Throwables.getStackTraceAsString(e);
                log.error("PrepareService rollback notifyChanged failed, syncSelectorId={}, syncRuleIds={}, cause={}", backupSelectorId, backupRuleIds, trace);

                result.put(AdminConstants.OPERATE_RESULT_CODE, AdminConstants.ROLLBACK_FAILED);

                reportedTransaction(false, "rollback-notifyChanged", trace, nanoTime, result);
            }
        } catch (Exception e) {

            String trace = Throwables.getStackTraceAsString(e);
            log.error("PrepareService publish transferSelector failed, backupSelectorId={}, cause={}", backupSelectorId, trace);

            reportedTransaction(false, "rollback", trace, nanoTime, result);
            throw e;
        }

        result.put(AdminConstants.SELECTOR_NAME, backupSelector.getName());

        started.stop();
        // warn level
        log.warn("PrepareService rollback finish, backupSelectorId={}, timestamp={}, result={}, elapsed={}", id, timestamp, result, started.elapsed(TimeUnit.MILLISECONDS));

        return result;
    }

    @Override
    public SelectorBackupVO queryBackupByIdAndTimestamp(String id, Long timestamp) {

        Timestamp datePublished = new Timestamp(timestamp);

        SelectorDO selectorDO = backupSelectorMapper.findByIdAndDatePublished(id, datePublished);

        if (selectorDO == null) {
            return null;
        }

        List<SelectorConditionDO> selectorConditions = backupSelectorConditionMapper.selectBySelectorIdAndDatePublished(id, datePublished);

        List<RuleDO> rules = backupRuleMapper.findBySelectorIdAndDatePublished(id, datePublished);

        List<String> ruleIds = rules.stream().map(RuleDO::getId).collect(Collectors.toList());

        SelectorBackupVO.SelectorBackup selectorBackup = SelectorBackupTransfer.INSTANCE.mapToSelectorBackup(selectorDO);

        if (CollectionUtils.isNotEmpty(selectorConditions)) {
            List<SelectorBackupVO.SelectorConditionBackup> conditionBackups = SelectorBackupTransfer.INSTANCE.mapToSelectorBackupCondition(selectorConditions);
            selectorBackup.setSelectorConditionBackups(conditionBackups);
        }

        List<SelectorBackupVO.RuleBackup> backupRules = null;
        if (CollectionUtils.isNotEmpty(rules) && CollectionUtils.isNotEmpty(ruleIds)) {
            List<RuleConditionDO> ruleConditions = backupRuleConditionMapper.findByRuleIdsAndDatePublished(ruleIds, datePublished);

            List<SelectorBackupVO.RuleConditionBackup> backupRuleConditions = SelectorBackupTransfer.INSTANCE.mapToRuleConditionBackup(ruleConditions);

            Map<String, List<SelectorBackupVO.RuleConditionBackup>> ruleConditionMap = backupRuleConditions.stream().collect(Collectors.groupingBy(SelectorBackupVO.RuleConditionBackup::getRuleId));

            backupRules = SelectorBackupTransfer.INSTANCE.mapToRuleBackup(rules);
            backupRules.forEach(r -> r.setRuleConditionBackups(ruleConditionMap.get(r.getId())));
        }

        return new SelectorBackupVO(selectorBackup, backupRules);
    }

    @Override
    public List<SelectorBackupVersionVO> queryBackupVersionById(String selectorId, Integer currentPage, Integer pageSize) {

        List<SelectorDO> selectors = backupSelectorMapper.selectByQuery(new SelectorVersionQuery(selectorId, new PageParameter(currentPage, pageSize)));

        if (CollectionUtils.isEmpty(selectors)) {
            return Collections.emptyList();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        return selectors.stream().map(t -> {

            Long rollbackTime = Optional.ofNullable(t).map(SelectorDO::getDatePublished).map(Timestamp::getTime).orElse(null);
            String rollbackDate = Optional.ofNullable(t).map(SelectorDO::getDatePublished).map(Timestamp::toLocalDateTime).map(formatter::format).orElse("-");

            return new SelectorBackupVersionVO(t.getId(), rollbackTime, rollbackDate, t.getRemark());
        }).collect(Collectors.toList());
    }

    private final Type ruleUpstreamType = new TypeToken<List<RuleUpstreamDTO>>() {
    }.getType();

    @Override
    public Map<String, String> syncRuleHost(String serviceInfoId) {

        // 线上调用, 预发布调用直接返回
        Map<String, String> result = new HashMap<>();
        if (!PreEnv.isPre()) {
            log.info("PrepareService syncRuleHost environment is online");
            return result;
        }

        long nanoTime = System.nanoTime();

        List<RuleDO> rules = ruleMapper.selectByServiceId(serviceInfoId);
        if (CollectionUtils.isEmpty(rules)) {
            log.warn("PrepareService syncRuleHost rules empty for serviceInfoId: {}", serviceInfoId);

            return result;
        }

        List<RuleUpstreamDTO> upstreamHandle = serviceInfoService.findUpstreamHandle(serviceInfoId);
        Map<String, List<RuleDO>> selectorRules = rules.stream().collect(Collectors.groupingBy(RuleDO::getSelectorId));

        Set<RuleUpstreamDTO> newUpstreamHandle = new HashSet<>();
        for (Iterator<List<RuleDO>> iterator = selectorRules.values().iterator(); iterator.hasNext(); ) {
            List<RuleDO> originRules = iterator.next();

            Map<String, RuleUpstreamDTO> originUpstreams = originRules.stream()
                                                                      .map(RuleDO::getUpstreamHandle)
                                                                      .map(u -> (List<RuleUpstreamDTO>) GsonUtils.getInstance().fromJson(u, ruleUpstreamType))
                                                                      .flatMap(List::stream)
                                                                      .collect(Collectors.toMap(RuleUpstreamDTO::getUpstreamHost, u -> u));

            upstreamHandle.forEach(s -> {
                RuleUpstreamDTO ruleUpstream = originUpstreams.get(s.getUpstreamHost());

                if (ruleUpstream != null) {
                    String protocol = ruleUpstream.getProtocol();
                    if (!Strings.isNullOrEmpty(protocol)) {
                        s.setProtocol(protocol);
                    }
                    int weight = ruleUpstream.getWeight();
                    s.setWeight(weight);
                }

                newUpstreamHandle.add(s);
            });
        }

        if (CollectionUtils.isEmpty(newUpstreamHandle)) {
            newUpstreamHandle.addAll(upstreamHandle);
        }

        if (CollectionUtils.isNotEmpty(newUpstreamHandle)) {
            int ruleCount = ruleService.updateByServiceInfoId(serviceInfoId, GsonUtils.getInstance().toJson(newUpstreamHandle));
            result.put("规则主机", String.valueOf(ruleCount));
        } else {
            result.put("规则主机为空", "0");
            return result;
        }

        result.putAll(this.publish(new ArrayList<>(selectorRules.keySet()), "系统自动同步主机触发", true));

        Map<String, Object> message = new HashMap<>();
        message.put(AdminConstants.NOTICE_MESSAGE_TIMESTAMP, nanoTime);
        try {
            configSubscribeService.notifyChanged(GsonUtils.getInstance().toJson(message));

            reportedTransaction(true, "publish", Message.SUCCESS, nanoTime, result);
        } catch (Exception e) {

            String trace = Throwables.getStackTraceAsString(e);
            log.error("PrepareService syncRuleHost notifyChanged failed, serviceInfoId={}, nanoTime={}, cause={}", serviceInfoId, nanoTime, trace);
            result.put(AdminConstants.OPERATE_RESULT_CODE, AdminConstants.NOTICE_FAILED);

            reportedTransaction(true, "publish-notifyChanged", trace, nanoTime, result);
        }

        return result;
    }

    private void reportedTransaction(boolean autoSync, String name, String status, long durationStart, Map<String, String> result) {

        String type = "Prepare_Environment";
        if (autoSync) {
            type = "Auto_Sync_" + type;
        }
        DefaultTransaction transaction = (DefaultTransaction) Cat.newTransaction(type, name);
        createConsumerCross(transaction, type, durationStart, result);

        transaction.setDurationStart(durationStart);

        transaction.setStatus(status);
        transaction.complete();
    }

    private void createConsumerCross(Transaction transaction, String type, long durationStart, Map<String, String> result) {

        AbstractMessage gatewayCallEvent = (AbstractMessage) Cat.newEvent(type, GsonUtils.getInstance().toJson(result));
        gatewayCallEvent.setTimestamp(durationStart);

        gatewayCallEvent.setStatus(Event.SUCCESS);
        gatewayCallEvent.setCompleted(true);

        transaction.addChild(gatewayCallEvent);
    }

    private <T> void assemblySyncList(List<T> syncList, List<T> onlineList, List<T> syncInsertList, List<T> syncUpdateList) {
        for (Iterator<T> iterator = syncList.iterator(); iterator.hasNext(); ) {
            T syncDO = iterator.next();

            if (CollectionUtils.isEmpty(onlineList)) {
                syncInsertList.add(syncDO);

                continue;
            }

            if (onlineList.removeIf(o -> Objects.equals(((BaseDO) syncDO).getId(), ((BaseDO) o).getId()))) {
                syncUpdateList.add(syncDO);

                continue;
            }

            syncInsertList.add(syncDO);
        }
    }


}
