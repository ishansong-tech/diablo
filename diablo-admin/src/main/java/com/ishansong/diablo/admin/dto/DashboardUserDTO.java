package com.ishansong.diablo.admin.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DashboardUserDTO implements Serializable {

    private String id;

    private String userName;

    private String password;

    private Integer role;

    private Boolean enabled;
}
