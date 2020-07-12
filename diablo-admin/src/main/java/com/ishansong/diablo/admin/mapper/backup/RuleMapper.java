package com.ishansong.diablo.admin.mapper.backup;

import com.ishansong.diablo.admin.entity.RuleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("rollBackupRuleMapper")
public interface RuleMapper {

    List<RuleDO> findBySelectorId(@Param("selectorId") String selectorId);

    int deleteBySelectorId(@Param("selectorId") String selectorId);

    int insertBatch(@Param("rules") List<RuleDO> rules);
}
