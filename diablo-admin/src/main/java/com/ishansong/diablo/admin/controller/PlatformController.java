package com.ishansong.diablo.admin.controller;

import com.ishansong.diablo.admin.service.DashboardUserService;
import com.ishansong.diablo.admin.service.EnumService;
import com.ishansong.diablo.admin.vo.DashboardUserVO;
import com.ishansong.diablo.core.constant.AdminConstants;
import com.ishansong.diablo.core.model.DiabloAdminResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/platform")
public class PlatformController {

    private final DashboardUserService dashboardUserService;

    private final EnumService enumService;

    @Autowired(required = false)
    public PlatformController(final DashboardUserService dashboardUserService, final EnumService enumService) {
        this.dashboardUserService = dashboardUserService;
        this.enumService = enumService;
    }

    @GetMapping("/login")
    public DiabloAdminResult loginDashboardUser(final String userName, final String password, HttpServletRequest request) {
        try {
            DashboardUserVO dashboardUserVO = dashboardUserService.findByQuery(userName, password);

            HttpSession session = request.getSession();
            Object attribute = session.getAttribute(AdminConstants.SESSION_LOGIN_NAME);
            if (attribute != null) {
                session.removeAttribute(AdminConstants.SESSION_LOGIN_NAME);
            }

            session.setAttribute(AdminConstants.SESSION_LOGIN_NAME, userName);
            return DiabloAdminResult.success("login dashboard user success", dashboardUserVO);
        } catch (Exception e) {
            return DiabloAdminResult.error("login dashboard user exception");
        }
    }

    @GetMapping("/enum")
    public DiabloAdminResult queryEnums() {
        return DiabloAdminResult.success(enumService.list());
    }
}
