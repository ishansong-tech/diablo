package com.ishansong.diablo.admin.entity;

import com.ishansong.diablo.admin.dto.ServiceUpstreamDTO;
import com.ishansong.diablo.core.utils.UUIDUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

@Data
public class ServiceUpstreamDO extends BaseDO {

    private String id;

    private String serviceInfoId;

    private String hostName;

    private String env;

    private String hostIp;

    private Integer port;

    public ServiceUpstreamDO setPort(Integer port) {
        this.port = port;
        return this;
    }

    public static ServiceUpstreamDO buildServiceUpstreamDO(final ServiceUpstreamDTO serviceUpstreamDTO) {
        if (serviceUpstreamDTO != null) {
            ServiceUpstreamDO serviceUpstreamDO = new ServiceUpstreamDO();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (StringUtils.isEmpty(serviceUpstreamDTO.getId())) {
                serviceUpstreamDO.setId(UUIDUtils.generateShortUuid());
                serviceUpstreamDO.setDateCreated(currentTime);
            } else {
                serviceUpstreamDO.setId(serviceUpstreamDTO.getId());
            }

            serviceUpstreamDO.setEnv(serviceUpstreamDTO.getEnv());
            serviceUpstreamDO.setHostIp(serviceUpstreamDTO.getHostIp());
            serviceUpstreamDO.setHostName(serviceUpstreamDTO.getHostName());
            serviceUpstreamDO.setServiceInfoId(serviceUpstreamDTO.getServiceInfoId());

            return serviceUpstreamDO;
        }
        return null;
    }
}
