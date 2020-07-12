package com.ishansong.diablo.admin.vo;
import com.ishansong.diablo.admin.entity.ServiceUpstreamDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceUpstreamVO implements Serializable {

    private String id;

    private String serviceInfoId;

    private String hostName;

    private String env;

    private String hostIp;

    private String dateCreated;

    private String dateUpdated;

    public static ServiceUpstreamVO buildServiceUpstreamVO(final ServiceUpstreamDO serviceUpstreamDO) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
        return new ServiceUpstreamVO(
                serviceUpstreamDO.getId(),
                serviceUpstreamDO.getServiceInfoId(),
                serviceUpstreamDO.getHostName(),
                serviceUpstreamDO.getEnv(),
                serviceUpstreamDO.getHostIp(),
                dateTimeFormatter.format(serviceUpstreamDO.getDateCreated().toLocalDateTime()),
                dateTimeFormatter.format(serviceUpstreamDO.getDateUpdated().toLocalDateTime())
        );
    }

}
