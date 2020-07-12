package com.ishansong.diablo.admin.vo;

import com.ishansong.diablo.admin.dto.RuleUpstreamDTO;
import com.ishansong.diablo.admin.entity.ServiceInfoDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAppVO {

    private String id;

    private String name;

    private List<RuleUpstreamDTO> ruleUpstreams;


    public ServiceAppVO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static ServiceAppVO buildServiceAppVO(ServiceInfoDO serviceInfoDO) {

        return new ServiceAppVO(serviceInfoDO.getId(), serviceInfoDO.getEnv() + "#" + serviceInfoDO.getName());
    }
}
