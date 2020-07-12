package com.ishansong.diablo.admin.vo;

import com.ishansong.diablo.admin.entity.RuleDO;
import com.ishansong.diablo.core.enums.MatchModeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleVO implements Serializable {

    private String id;

    private String selectorId;

    private String serviceInfoId;

    private Integer matchMode;

    private String matchModeName;

    private String name;

    private Boolean enabled;

    private Boolean loged;

    private Integer sort;

    private String handle;

    private String upstreamHandle;

    private List<RuleConditionVO> ruleConditions;

    private List<ServiceAppVO> serviceApps;

    private String serviceApp;

    private String dateCreated;

    private String dateUpdated;

    public static RuleVO buildRuleVO(final RuleDO ruleDO) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
        return new RuleVO(ruleDO.getId(), ruleDO.getSelectorId(), ruleDO.getServiceInfoId(), ruleDO.getMatchMode(), MatchModeEnum.getMatchModeByCode(ruleDO.getMatchMode()),
                ruleDO.getName(), ruleDO.getEnabled(), ruleDO.getLoged(), ruleDO.getSort(), ruleDO.getHandle(), ruleDO.getUpstreamHandle(), null,
                null,
                null,
                dateTimeFormatter.format(ruleDO.getDateCreated().toLocalDateTime()),
                dateTimeFormatter.format(ruleDO.getDateUpdated().toLocalDateTime()));
    }

    public static RuleVO buildRuleVO(final RuleDO ruleDO, final List<RuleConditionVO> ruleConditions, final List<ServiceAppVO> serviceApps) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
        return new RuleVO(ruleDO.getId(), ruleDO.getSelectorId(), ruleDO.getServiceInfoId(), ruleDO.getMatchMode(), MatchModeEnum.getMatchModeByCode(ruleDO.getMatchMode()),
                ruleDO.getName(), ruleDO.getEnabled(), ruleDO.getLoged(), ruleDO.getSort(), ruleDO.getHandle(), ruleDO.getUpstreamHandle(), ruleConditions,
                serviceApps,
                serviceApps.stream().filter(r -> Objects.equals(r.getId(), ruleDO.getServiceInfoId())).findFirst().map(ServiceAppVO::getName).orElse(null),
                dateTimeFormatter.format(ruleDO.getDateCreated().toLocalDateTime()),
                dateTimeFormatter.format(ruleDO.getDateUpdated().toLocalDateTime()));
    }
}
