package com.ishansong.diablo.admin.vo;

import com.ishansong.diablo.admin.entity.DashboardUserDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardUserVO implements Serializable {

    private String id;

    private String userName;

    private String password;

    private Integer role;

    private Boolean enabled;

    private String dateCreated;

    private String dateUpdated;

    public static DashboardUserVO buildDashboardUserVO(final DashboardUserDO dashboardUserDO) {
        if (dashboardUserDO != null) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return new DashboardUserVO(dashboardUserDO.getId(), dashboardUserDO.getUserName(),
                    dashboardUserDO.getPassword(), dashboardUserDO.getRole(), dashboardUserDO.getEnabled(),
                    dateTimeFormatter.format(dashboardUserDO.getDateCreated().toLocalDateTime()),
                    dateTimeFormatter.format(dashboardUserDO.getDateUpdated().toLocalDateTime()));
        }
        return null;
    }
}
