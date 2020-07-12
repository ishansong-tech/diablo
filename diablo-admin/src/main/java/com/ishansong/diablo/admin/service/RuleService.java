package com.ishansong.diablo.admin.service;

import com.ishansong.diablo.admin.dto.RuleDTO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.RuleCommonPager;
import com.ishansong.diablo.admin.query.RuleQuery;
import com.ishansong.diablo.admin.vo.RuleVO;
import com.ishansong.diablo.core.model.rule.RuleData;

import java.util.List;

public interface RuleService {

    int createOrUpdate(RuleDTO ruleDTO);

    int delete(List<String> ids);

    RuleVO findById(String id);

    RuleCommonPager<RuleVO> listByPage(RuleQuery ruleQuery);

    List<RuleData> listAll();

    List<RuleData> findBySelectorId(String selectorId);

    int updateByServiceInfoId(String serviceInfoId, String upstreamHandle);

}
