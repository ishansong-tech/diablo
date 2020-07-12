package com.ishansong.diablo.admin.service;

import com.ishansong.diablo.admin.dto.SelectorDTO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.query.SelectorQuery;
import com.ishansong.diablo.admin.vo.SelectorVO;
import com.ishansong.diablo.core.model.selector.SelectorData;

import java.util.List;

public interface SelectorService {

    int createOrUpdate(SelectorDTO selectorDTO);

    int delete(List<String> ids);

    SelectorVO findById(String id);

    CommonPager<SelectorVO> listByPage(SelectorQuery selectorQuery);

    List<SelectorData> findByPluginId(String pluginId);

    List<SelectorData> listAll();

}
