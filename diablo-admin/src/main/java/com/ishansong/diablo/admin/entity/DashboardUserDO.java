package com.ishansong.diablo.admin.entity;

import com.ishansong.diablo.admin.dto.DashboardUserDTO;
import com.ishansong.diablo.core.utils.UUIDUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

@Data
public class DashboardUserDO extends BaseDO {

    private String userName;

    private String password;

    private Integer role;

    private Boolean enabled;

    public static DashboardUserDO buildDashboardUserDO(final DashboardUserDTO dashboardUserDTO) {
        if (dashboardUserDTO != null) {
            DashboardUserDO dashboardUserDO = new DashboardUserDO();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (StringUtils.isEmpty(dashboardUserDTO.getId())) {
                dashboardUserDO.setId(UUIDUtils.generateShortUuid());
                dashboardUserDO.setEnabled(true);
                dashboardUserDO.setDateCreated(currentTime);
            } else {
                dashboardUserDO.setId(dashboardUserDTO.getId());
                dashboardUserDO.setEnabled(dashboardUserDTO.getEnabled());
            }
            dashboardUserDO.setUserName(dashboardUserDTO.getUserName());
            dashboardUserDO.setPassword(dashboardUserDTO.getPassword());
            dashboardUserDO.setRole(dashboardUserDTO.getRole());
            dashboardUserDO.setDateUpdated(currentTime);
            return dashboardUserDO;
        }
        return null;
    }
}
