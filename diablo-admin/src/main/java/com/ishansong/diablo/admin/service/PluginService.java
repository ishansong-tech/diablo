package com.ishansong.diablo.admin.service;

import com.ishansong.diablo.admin.dto.PluginDTO;
import com.ishansong.diablo.admin.query.PluginQuery;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.vo.PluginVO;
import com.ishansong.diablo.core.model.plugin.PluginData;

import java.util.List;

public interface PluginService {

    String createOrUpdate(PluginDTO pluginDTO);

    String delete(List<String> ids);

    PluginVO findById(String id);

    CommonPager<PluginVO> listByPage(PluginQuery pluginQuery);

    List<PluginData> listAll();

    String enabled(List<String> ids, Boolean enabled);
}
