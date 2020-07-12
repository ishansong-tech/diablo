package com.ishansong.diablo.admin.controller;

import com.ishansong.diablo.admin.dto.ServiceUpstreamDTO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.query.ServiceUpstreamQuery;
import com.ishansong.diablo.admin.service.ServiceUpstreamService;
import com.ishansong.diablo.admin.vo.ServiceUpstreamVO;
import com.ishansong.diablo.core.model.DiabloAdminResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/serviceUpstream")
public class ServiceUpstreamController {

    private final ServiceUpstreamService serviceUpstreamService;

    @Autowired(required = false)
    public ServiceUpstreamController(final ServiceUpstreamService serviceUpstreamService) {
        this.serviceUpstreamService = serviceUpstreamService;
    }

    @GetMapping("")
    public DiabloAdminResult query( @RequestParam(value = "hostName",defaultValue = "") String hostName,
                                           final Integer currentPage, final Integer pageSize) {
        try {
            if(StringUtils.isEmpty(hostName)){
                hostName=null;
            }
            ServiceUpstreamQuery serviceUpstreamQuery= new ServiceUpstreamQuery();
            serviceUpstreamQuery.setHostName(hostName);
            serviceUpstreamQuery.setPageParameter(new PageParameter(currentPage, pageSize));
            CommonPager<ServiceUpstreamVO> commonPager = serviceUpstreamService.listByPage(serviceUpstreamQuery);
            return DiabloAdminResult.success("query serviceUpstream success", commonPager);
        } catch (Exception e) {
            return DiabloAdminResult.error("query serviceUpstream exception");
        }
    }

    @GetMapping("/{id}")
    public DiabloAdminResult detail(@PathVariable("id") final String id) {
        try {
            ServiceUpstreamVO vo = serviceUpstreamService.findById(id);
            return DiabloAdminResult.success("detail serviceUpstream success", vo);
        } catch (Exception e) {
            return DiabloAdminResult.error("detail serviceUpstream exception");
        }
    }

    @PostMapping("")
    public DiabloAdminResult create(@RequestBody final ServiceUpstreamDTO dto) {
        try {
            Integer createCount = serviceUpstreamService.createOrUpdate(dto);
            return DiabloAdminResult.success("create serviceUpstream success", createCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("create serviceUpstream exception");
        }
    }

    @PutMapping("/{id}")
    public DiabloAdminResult update(@PathVariable("id") final String id, @RequestBody final ServiceUpstreamDTO dto) {
        try {
            Objects.requireNonNull(dto);
            dto.setId(id);
            Integer updateCount = serviceUpstreamService.createOrUpdate(dto);
            return DiabloAdminResult.success("update serviceUpstream success", updateCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("update serviceUpstream exception");
        }
    }

    @DeleteMapping("/batch")
    public DiabloAdminResult delete(@RequestBody final List<String> ids) {
        try {
            final int result = serviceUpstreamService.delete(ids);
            if (result<=0) {
                return DiabloAdminResult.error("批量删除应用主机失败！");
            }
            return DiabloAdminResult.success("delete serviceUpstream success");
        } catch (Exception e) {
            return DiabloAdminResult.error("delete serviceUpstream exception");
        }
    }

    @GetMapping("/sync")
    public DiabloAdminResult sync(@RequestParam(value = "env",defaultValue = "") String env,
                             @RequestParam(value = "serviceName",defaultValue = "") String serviceName) {
        try {
            if(StringUtils.isEmpty(env)){
                return DiabloAdminResult.error("env is null");
            }
            return DiabloAdminResult.success("sync success", this.serviceUpstreamService.sync(env,serviceName));
        } catch (Exception e) {
            return DiabloAdminResult.error("sync exception");
        }
    }

}
