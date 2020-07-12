package com.ishansong.diablo.admin.vo;

import com.ishansong.diablo.admin.entity.SelectorDO;
import com.ishansong.diablo.core.enums.MatchModeEnum;
import com.ishansong.diablo.core.enums.SelectorTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectorVO implements Serializable {

    private String id;

    private String pluginId;

    private String name;

    private Integer matchMode;

    private String matchModeName;

    private Integer type;

    private String typeName;

    private Integer sort;

    private Boolean enabled;

    private Boolean loged;

    private Boolean continued;

    private String handle;

    private List<SelectorConditionVO> selectorConditions;

    private String dateCreated;

    private String dateUpdated;

    private String datePublished;

    private String dateRollbacked;

    public String getDatePublished() {
        return datePublished;
    }

    public SelectorVO setDatePublished(String datePublished) {
        this.datePublished = datePublished;

        return this;
    }

    public String getDateRollbacked() {
        return dateRollbacked;
    }

    public SelectorVO setDateRollbacked(String dateRollbacked) {
        this.dateRollbacked = dateRollbacked;

        return this;
    }

    public static SelectorVO buildSelectorVO(final SelectorDO selectorDO) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
        return new SelectorVO(selectorDO.getId(), selectorDO.getPluginId(), selectorDO.getName(), selectorDO.getMatchMode(), MatchModeEnum.getMatchModeByCode(selectorDO.getMatchMode()),
                selectorDO.getType(), SelectorTypeEnum.getSelectorTypeByCode(selectorDO.getType()), selectorDO.getSort(),
                selectorDO.getEnabled(), selectorDO.getLoged(), selectorDO.getContinued(), selectorDO.getHandle(), null,
                dateTimeFormatter.format(selectorDO.getDateCreated().toLocalDateTime()),
                dateTimeFormatter.format(selectorDO.getDateUpdated().toLocalDateTime()),
                Optional.ofNullable(selectorDO.getDatePublished()).map(Timestamp::toLocalDateTime).map(dateTimeFormatter::format).orElse("-"),
                Optional.ofNullable(selectorDO.getDateRollbacked()).map(Timestamp::toLocalDateTime).map(dateTimeFormatter::format).orElse("-")
        );
    }

    public static SelectorVO buildSelectorVO(final SelectorDO selectorDO, final List<SelectorConditionVO> selectorConditions) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
        return new SelectorVO(selectorDO.getId(), selectorDO.getPluginId(), selectorDO.getName(), selectorDO.getMatchMode(), MatchModeEnum.getMatchModeByCode(selectorDO.getMatchMode()),
                selectorDO.getType(), SelectorTypeEnum.getSelectorTypeByCode(selectorDO.getType()), selectorDO.getSort(),
                selectorDO.getEnabled(), selectorDO.getLoged(), selectorDO.getContinued(), selectorDO.getHandle(), selectorConditions,
                dateTimeFormatter.format(selectorDO.getDateCreated().toLocalDateTime()),
                dateTimeFormatter.format(selectorDO.getDateUpdated().toLocalDateTime()),
                Optional.ofNullable(selectorDO.getDatePublished()).map(Timestamp::toLocalDateTime).map(dateTimeFormatter::format).orElse("-"),
                Optional.ofNullable(selectorDO.getDateRollbacked()).map(Timestamp::toLocalDateTime).map(dateTimeFormatter::format).orElse("-")
        );
    }
}
