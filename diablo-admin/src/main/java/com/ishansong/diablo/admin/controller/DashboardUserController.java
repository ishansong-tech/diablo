package com.ishansong.diablo.admin.controller;

import com.ishansong.diablo.admin.dto.DashboardUserDTO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.service.DashboardUserService;
import com.ishansong.diablo.admin.query.DashboardUserQuery;
import com.ishansong.diablo.admin.vo.DashboardUserVO;
import com.ishansong.diablo.core.model.DiabloAdminResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/dashboardUser")
public class DashboardUserController {

    private final DashboardUserService dashboardUserService;

    @Autowired(required = false)
    public DashboardUserController(final DashboardUserService dashboardUserService) {
        this.dashboardUserService = dashboardUserService;
    }

    @GetMapping("")
    public DiabloAdminResult queryDashboardUsers(final String userName, final Integer currentPage, final Integer pageSize) {
        try {
            CommonPager<DashboardUserVO> commonPager = dashboardUserService.listByPage(new DashboardUserQuery(userName, new PageParameter(currentPage, pageSize)));
            return DiabloAdminResult.success("query dashboard users success", commonPager);
        } catch (Exception e) {
            return DiabloAdminResult.error("query dashboard users exception");
        }
    }

    @GetMapping("/{id}")
    public DiabloAdminResult detailDashboardUser(@PathVariable("id") final String id) {
        try {
            DashboardUserVO dashboardUserVO = dashboardUserService.findById(id);
            return DiabloAdminResult.success("detail dashboard user success", dashboardUserVO);
        } catch (Exception e) {
            return DiabloAdminResult.error("detail dashboard user exception");
        }
    }

    @PostMapping("")
    public DiabloAdminResult createDashboardUser(@RequestBody final DashboardUserDTO dashboardUserDTO) {
        try {
            Integer createCount = dashboardUserService.createOrUpdate(dashboardUserDTO);
            return DiabloAdminResult.success("create dashboard user success", createCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("create dashboard user exception");
        }
    }

    @PutMapping("/{id}")
    public DiabloAdminResult updateDashboardUser(@PathVariable("id") final String id, @RequestBody final DashboardUserDTO dashboardUserDTO) {
        try {
            Objects.requireNonNull(dashboardUserDTO);
            dashboardUserDTO.setId(id);
            Integer updateCount = dashboardUserService.createOrUpdate(dashboardUserDTO);
            return DiabloAdminResult.success("update dashboard user success", updateCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("update dashboard user exception");
        }
    }

    @DeleteMapping("/batch")
    public DiabloAdminResult deleteDashboardUser(@RequestBody final List<String> ids) {
        try {
            Integer deleteCount = dashboardUserService.delete(ids);
            return DiabloAdminResult.success("delete dashboard users success", deleteCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("delete dashboard users exception");
        }
    }
}
