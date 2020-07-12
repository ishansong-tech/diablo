package com.ishansong.diablo.admin.mapper;

import com.ishansong.diablo.admin.entity.RuleConditionDO;
import com.ishansong.diablo.admin.query.RuleConditionQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RuleConditionMapper {

    RuleConditionDO selectById(String id);

    List<RuleConditionDO> selectByQuery(RuleConditionQuery ruleConditionQuery);

    List<RuleConditionDO> findByRuleIds(@Param("ruleIds") List<String> ruleIds);

    int insert(RuleConditionDO ruleConditionDO);

    int insertSelective(RuleConditionDO ruleConditionDO);

    int update(RuleConditionDO ruleConditionDO);

    int updateSelective(RuleConditionDO ruleConditionDO);

    int delete(String id);

    int deleteByQuery(RuleConditionQuery ruleConditionQuery);

    int deleteBatch(@Param("ruleIds") List<String> ruleIds);

    int insertBatch(@Param("ruleConditions") List<RuleConditionDO> ruleConditions);
}
