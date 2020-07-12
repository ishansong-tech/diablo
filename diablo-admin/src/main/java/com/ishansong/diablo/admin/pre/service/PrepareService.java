package com.ishansong.diablo.admin.pre.service;

import com.ishansong.diablo.admin.vo.SelectorBackupVO;
import com.ishansong.diablo.admin.vo.SelectorBackupVersionVO;

import java.util.List;
import java.util.Map;

public interface PrepareService {

    Map<String, String> publish(List<String> ids, String remark);

    Map<String, String> publish(List<String> ids, String remark, Boolean autoSync);

    Map<String, String> rollback(String id, Long timestamp);

    SelectorBackupVO queryBackupByIdAndTimestamp(String id, Long timestamp);

    List<SelectorBackupVersionVO> queryBackupVersionById(String selectorId, Integer currentPage, Integer pageSize);

    Map<String, String> syncRuleHost(String serviceInfoId);
}
