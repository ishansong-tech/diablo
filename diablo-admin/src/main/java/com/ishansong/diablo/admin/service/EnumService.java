package com.ishansong.diablo.admin.service;

import com.ishansong.diablo.admin.vo.EnumVO;

import java.util.List;
import java.util.Map;

public interface EnumService {

    Map<String, List<EnumVO>> list();
}
