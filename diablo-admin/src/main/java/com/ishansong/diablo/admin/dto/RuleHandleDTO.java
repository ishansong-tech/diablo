package com.ishansong.diablo.admin.dto;

import lombok.Data;

@Data
public class RuleHandleDTO {

    private String loadBalance;

    private int timeout;

    private int retry;
}
