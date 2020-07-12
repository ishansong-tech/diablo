package com.ishansong.diablo.admin.mapper;

import com.ishansong.diablo.admin.entity.RuleDO;
import com.ishansong.diablo.admin.query.RuleQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RuleMapper {

    RuleDO selectById(String id);

    List<RuleDO> selectByQuery(RuleQuery ruleQuery);

    List<RuleDO> findBySelectorId(String selectorId);

    Integer countByQuery(RuleQuery ruleQuery);

    int insert(RuleDO ruleDO);

    int insertSelective(RuleDO ruleDO);

    int update(RuleDO ruleDO);

    int updateSelective(RuleDO ruleDO);

    int delete(String id);

    List<RuleDO> selectAll();

    int deleteBySelectorId(String selectorId);

    int insertBatch(@Param("rules") List<RuleDO> rules);

    List<RuleDO> selectByServiceId(String serviceInfoId);

    int updateByServiceInfoId(@Param("serviceInfoId") String serviceInfoId, @Param("upstreamHandle") String upstreamHandle);
}
