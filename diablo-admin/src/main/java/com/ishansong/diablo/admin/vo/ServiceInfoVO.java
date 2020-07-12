package com.ishansong.diablo.admin.vo;

import com.ishansong.diablo.admin.entity.ServiceInfoDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInfoVO implements Serializable {

    private String id;

    /**
     * 服务名称,order-api
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

    private String dateCreated;

    private String dateUpdated;

    public static ServiceInfoVO buildServiceInfoVO(final ServiceInfoDO serviceInfoDO) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
        return new ServiceInfoVO(
                serviceInfoDO.getId(),
                serviceInfoDO.getName(),
                serviceInfoDO.getEnv(),
                serviceInfoDO.getPort(),
                dateTimeFormatter.format(serviceInfoDO.getDateCreated().toLocalDateTime()),
                dateTimeFormatter.format(serviceInfoDO.getDateUpdated().toLocalDateTime())
        );
    }

}
