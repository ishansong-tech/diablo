package com.ishansong.diablo.admin.transfer;

import com.ishansong.diablo.admin.entity.SelectorDO;
import com.ishansong.diablo.core.model.selector.SelectorData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SelectorTransfer {

    SelectorTransfer INSTANCE = Mappers.getMapper(SelectorTransfer.class);

    SelectorData mapToData(SelectorDO selectorDO);


}
