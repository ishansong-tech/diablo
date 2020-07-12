package com.ishansong.diablo.admin.pre.mapper;

import com.ishansong.diablo.admin.entity.RuleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("preRuleMapper")
public interface RuleMapper {

    List<RuleDO> findBySelectorId(@Param("selectorId") String selectorId);

    int deleteBySelectorId(@Param("selectorId") String selectorId);

    int insertBatch(@Param("rules") List<RuleDO> rules);

    List<RuleDO> selectByServiceId(String serviceInfoId);

    int updateByServiceInfoId(@Param("serviceInfoId") String serviceInfoId, @Param("upstreamHandle") String upstreamHandle);
}
