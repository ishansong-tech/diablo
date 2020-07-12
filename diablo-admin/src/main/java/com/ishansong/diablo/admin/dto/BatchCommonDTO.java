package com.ishansong.diablo.admin.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BatchCommonDTO implements Serializable {

    private List<String> ids;

    private Boolean enabled;
}
