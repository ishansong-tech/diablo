package com.ishansong.diablo.admin.entity;
import com.ishansong.diablo.admin.dto.ServiceInfoDTO;
import com.ishansong.diablo.core.utils.UUIDUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

@Data
public class ServiceInfoDO extends BaseDO {

    private String id;

    /**
     * 服务名称,courier-api
     */
    private String name;

    /**
     * 服务所属环境
     */
    private String env;

    /**
     * 服务启动端口
     */
    private Integer port;

    public static ServiceInfoDO buildServiceInfoDO(final ServiceInfoDTO serviceInfoDTO) {
        if (serviceInfoDTO != null) {
            ServiceInfoDO serviceInfoDO = new ServiceInfoDO();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (StringUtils.isEmpty(serviceInfoDTO.getId())) {
                serviceInfoDO.setId(UUIDUtils.generateShortUuid());
                serviceInfoDO.setDateCreated(currentTime);
            } else {
                serviceInfoDO.setId(serviceInfoDTO.getId());
            }

            serviceInfoDO.setEnv(serviceInfoDTO.getEnv());
            serviceInfoDO.setName(serviceInfoDTO.getName());
            serviceInfoDO.setPort(serviceInfoDTO.getPort());

            return serviceInfoDO;
        }
        return null;
    }
}
