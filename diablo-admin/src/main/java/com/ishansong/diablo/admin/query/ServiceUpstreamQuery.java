package com.ishansong.diablo.admin.query;

import com.ishansong.diablo.admin.page.PageParameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceUpstreamQuery implements Serializable {

    private String hostName;

    private String hostIp;

    private String serviceInfoId;

    private String env;

    private PageParameter pageParameter;
}
