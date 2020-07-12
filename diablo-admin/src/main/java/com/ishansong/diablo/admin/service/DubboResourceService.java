package com.ishansong.diablo.admin.service;

import com.ishansong.diablo.admin.dto.DubboResourceDTO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.query.DubboResourceQuery;
import com.ishansong.diablo.admin.vo.DubboResourceDetailVO;
import com.ishansong.diablo.admin.vo.DubboResourceVO;
import com.ishansong.diablo.core.model.dubbo.DubboResourceData;

import java.util.List;

public interface DubboResourceService {

    List<DubboResourceData> listAll();

    CommonPager<DubboResourceVO> listByPage(DubboResourceQuery dubboResourceQuery);

    DubboResourceDetailVO findById(String id);

    int save(DubboResourceDTO dubboResourceDto);

    int update(DubboResourceDTO dubboResourceDto);

    int delete(List<String> ids);
}
