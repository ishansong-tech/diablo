package com.ishansong.diablo.admin.entity;

import com.ishansong.diablo.admin.dto.SelectorConditionDTO;
import com.ishansong.diablo.core.utils.UUIDUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

@Data
public class SelectorConditionDO extends BaseDO {

    private String selectorId;

    private String paramType;

    private String operator;

    private String paramName;

    private String paramValue;

    private Timestamp datePublished;

    public static SelectorConditionDO buildSelectorConditionDO(final SelectorConditionDTO selectorConditionDTO) {
        if (selectorConditionDTO != null) {
            SelectorConditionDO selectorConditionDO = new SelectorConditionDO();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (StringUtils.isEmpty(selectorConditionDTO.getId())) {
                selectorConditionDO.setId(UUIDUtils.generateShortUuid());
                selectorConditionDO.setDateCreated(currentTime);
            } else {
                selectorConditionDO.setId(selectorConditionDTO.getId());
            }

            selectorConditionDO.setParamType(selectorConditionDTO.getParamType());
            selectorConditionDO.setSelectorId(selectorConditionDTO.getSelectorId());
            selectorConditionDO.setOperator(selectorConditionDTO.getOperator());
            selectorConditionDO.setParamName(selectorConditionDTO.getParamName());
            selectorConditionDO.setParamValue(selectorConditionDTO.getParamValue());
            selectorConditionDO.setDateUpdated(currentTime);
            return selectorConditionDO;
        }
        return null;
    }
}
