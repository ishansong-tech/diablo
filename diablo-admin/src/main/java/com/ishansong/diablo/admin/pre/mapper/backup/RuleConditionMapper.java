package com.ishansong.diablo.admin.pre.mapper.backup;

import com.ishansong.diablo.admin.entity.RuleConditionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Mapper
@Repository("backupRuleConditionMapper")
public interface RuleConditionMapper {

    List<RuleConditionDO> findByRuleIds(@Param("ruleIds") List<String> ruleIds);

    int deleteBatch(@Param("ruleIds") List<String> ruleIds);

    int insertBatch(@Param("ruleConditions") List<RuleConditionDO> ruleConditions);

    List<RuleConditionDO> findByRuleIdsAndDatePublished(@Param("ruleIds") List<String> ruleIds, @Param("datePublished") Timestamp datePublished);
}
