package com.ishansong.diablo.admin.entity;

import com.ishansong.diablo.core.model.condition.ConditionData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import com.ishansong.diablo.core.utils.UUIDUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import com.ishansong.diablo.admin.dto.SelectorDTO;

import java.sql.Timestamp;
import java.util.List;

@Data
public class SelectorDO extends BaseDO {

    private String pluginId;

    private String name;

    private Integer matchMode;

    private Integer type;

    private Integer sort;

    private Boolean enabled;

    private Boolean loged;

    private Boolean continued;

    private Timestamp datePublished;

    private Timestamp dateRollbacked;

    private String handle;

    private String remark;

    public static SelectorDO buildSelectorDO(final SelectorDTO selectorDTO) {
        if (selectorDTO != null) {
            SelectorDO selectorDO = new SelectorDO();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (StringUtils.isEmpty(selectorDTO.getId())) {
                selectorDO.setId(UUIDUtils.generateShortUuid());
                selectorDO.setDateCreated(currentTime);
            } else {
                selectorDO.setId(selectorDTO.getId());
            }

            selectorDO.setPluginId(selectorDTO.getPluginId());
            selectorDO.setName(selectorDTO.getName());
            selectorDO.setMatchMode(selectorDTO.getMatchMode());
            selectorDO.setType(selectorDTO.getType());
            selectorDO.setSort(selectorDTO.getSort());
            selectorDO.setEnabled(selectorDTO.getEnabled());
            selectorDO.setLoged(selectorDTO.getLoged());
            selectorDO.setContinued(selectorDTO.getContinued());
            selectorDO.setDateUpdated(currentTime);
            selectorDO.setHandle(selectorDTO.getHandle());
            return selectorDO;
        }
        return null;
    }

    public static SelectorData transFrom(final SelectorDO selectorDO, final String pluginName, final List<ConditionData> conditionDataList) {
        return new SelectorData(selectorDO.getId(),
                selectorDO.getPluginId(),
                pluginName,
                selectorDO.getName(),
                selectorDO.getMatchMode(),
                selectorDO.getType(),
                selectorDO.getSort(),
                selectorDO.getEnabled(),
                selectorDO.getLoged(),
                selectorDO.getContinued(),
                selectorDO.getHandle(),
                conditionDataList);
    }
}
