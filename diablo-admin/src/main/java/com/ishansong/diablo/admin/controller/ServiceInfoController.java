package com.ishansong.diablo.admin.controller;
import com.ishansong.diablo.admin.dto.ServiceInfoDTO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.query.ServiceInfoQuery;
import com.ishansong.diablo.admin.service.ServiceInfoService;
import com.ishansong.diablo.admin.vo.ServiceInfoVO;
import com.ishansong.diablo.core.model.DiabloAdminResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/serviceInfo")
public class ServiceInfoController {

    private final ServiceInfoService serviceInfoService;

    @Autowired(required = false)
    public ServiceInfoController(final ServiceInfoService serviceInfoService) {
        this.serviceInfoService = serviceInfoService;
    }

    @GetMapping("")
    public DiabloAdminResult queryServiceInfos( @RequestParam(value = "name",defaultValue = "") String name,
                                           final Integer currentPage, final Integer pageSize) {
        try {
            if(StringUtils.isEmpty(name)){
                name=null;
            }
            ServiceInfoQuery serviceInfoQuery= new ServiceInfoQuery();
            serviceInfoQuery.setName(name);
            serviceInfoQuery.setPageParameter(new PageParameter(currentPage, pageSize));
            CommonPager<ServiceInfoVO> commonPager = serviceInfoService.listByPage(serviceInfoQuery);
            return DiabloAdminResult.success("query serviceInfo success", commonPager);
        } catch (Exception e) {
            return DiabloAdminResult.error("query serviceInfo exception");
        }
    }

    @GetMapping("/{id}")
    public DiabloAdminResult detail(@PathVariable("id") final String id) {
        try {
            ServiceInfoVO vo = serviceInfoService.findById(id);
            return DiabloAdminResult.success("detail serviceInfo success", vo);
        } catch (Exception e) {
            e.printStackTrace();
            return DiabloAdminResult.error("detail serviceInfo exception");
        }
    }

    @PostMapping("")
    public DiabloAdminResult create(@RequestBody final ServiceInfoDTO dto) {
        try {
            Integer createCount = serviceInfoService.createOrUpdate(dto);
            return DiabloAdminResult.success("create serviceInfo success", createCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("create serviceInfo exception");
        }
    }

    @PutMapping("/{id}")
    public DiabloAdminResult update(@PathVariable("id") final String id, @RequestBody final ServiceInfoDTO dto) {
        try {
            Objects.requireNonNull(dto);
            dto.setId(id);
            Integer updateCount = serviceInfoService.createOrUpdate(dto);
            return DiabloAdminResult.success("update serviceInfo success", updateCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("update serviceInfo exception");
        }
    }

    @DeleteMapping("/batch")
    public DiabloAdminResult deleteServiceInfos(@RequestBody final List<String> ids) {
        try {
            final int result = serviceInfoService.delete(ids);
            if (result<=0) {
                return DiabloAdminResult.error("批量删除应用失败！");
            }
            return DiabloAdminResult.success("delete plugins success");
        } catch (Exception e) {
            return DiabloAdminResult.error("delete plugins exception");
        }
    }

}
