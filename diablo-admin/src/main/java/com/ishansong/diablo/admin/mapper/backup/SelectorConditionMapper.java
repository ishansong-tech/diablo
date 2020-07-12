package com.ishansong.diablo.admin.mapper.backup;

import com.ishansong.diablo.admin.entity.SelectorConditionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("rollBackupSelectorConditionMapper")
public interface SelectorConditionMapper {

    List<SelectorConditionDO> selectBySelectorId(@Param("selectorId") String selectorId);

    int deleteBySelectorId(@Param("selectorId") String selectorId);

    int insertBatch(@Param("selectorConditions") List<SelectorConditionDO> selectorConditions);
}
