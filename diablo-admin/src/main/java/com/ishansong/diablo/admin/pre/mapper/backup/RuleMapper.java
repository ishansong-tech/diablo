package com.ishansong.diablo.admin.pre.mapper.backup;

import com.ishansong.diablo.admin.entity.RuleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Mapper
@Repository("backupRuleMapper")
public interface RuleMapper {

    List<RuleDO> findBySelectorId(@Param("selectorId") String selectorId);

    int deleteBySelectorId(@Param("selectorId") String selectorId);

    int insertBatch(@Param("rules") List<RuleDO> rules);

    List<RuleDO> findBySelectorIdAndDatePublished(@Param("selectorId") String selectorId, @Param("datePublished") Timestamp datePublished);
}
