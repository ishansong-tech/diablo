package com.ishansong.diablo.admin.entity;

import com.ishansong.diablo.admin.dto.RuleDTO;
import com.ishansong.diablo.core.model.condition.ConditionData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.utils.UUIDUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.List;

@Data
public class RuleDO extends BaseDO {

    private String selectorId;

    private String serviceInfoId;

    private Integer matchMode;

    private String name;

    private Boolean enabled;

    private Boolean loged;

    private Integer sort;

    private String handle;

    private String upstreamHandle;

    private Timestamp datePublished;

    public static RuleDO buildRuleDO(final RuleDTO ruleDTO) {
        if (ruleDTO != null) {
            RuleDO ruleDO = new RuleDO();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (StringUtils.isEmpty(ruleDTO.getId())) {
                ruleDO.setId(UUIDUtils.generateShortUuid());
                ruleDO.setDateCreated(currentTime);
            } else {
                ruleDO.setId(ruleDTO.getId());
            }

            ruleDO.setSelectorId(ruleDTO.getSelectorId());
            ruleDO.setServiceInfoId(ruleDTO.getServiceInfoId());
            ruleDO.setMatchMode(ruleDTO.getMatchMode());
            ruleDO.setName(ruleDTO.getName());
            ruleDO.setEnabled(ruleDTO.getEnabled());
            ruleDO.setLoged(ruleDTO.getLoged());
            ruleDO.setSort(ruleDTO.getSort());
            ruleDO.setHandle(ruleDTO.getHandle());
            ruleDO.setUpstreamHandle(ruleDTO.getUpstreamHandle());
            ruleDO.setDateUpdated(currentTime);
            return ruleDO;
        }
        return null;
    }

    public static RuleData transFrom(final RuleDO ruleDO, final String pluginName, final List<ConditionData> conditionDataList) {
        return new RuleData(ruleDO.getId(),
                ruleDO.getName(),
                pluginName,
                ruleDO.getSelectorId(),
                ruleDO.getServiceInfoId(),
                ruleDO.getMatchMode(),
                ruleDO.getSort(),
                ruleDO.getEnabled(),
                ruleDO.getLoged(),
                ruleDO.getHandle(),
                ruleDO.getUpstreamHandle(),
                conditionDataList);
    }
}
