package com.ishansong.diablo.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RemoteServiceUpstreamDTO implements Serializable {

    private Integer code;

    private RemoteServiceUpstreamResult result;

    @Data
    public static class RemoteServiceUpstreamResult {
        private String message;
        private RemoteServiceUpstreamResultData data;
    }

    @Data
    public static class RemoteServiceUpstreamResultData {
        @JsonProperty("res_list")
        private List<RemoteServiceUpstreamEntity> resList;
    }

    @Data
    public static class RemoteServiceUpstreamEntity {

        @JsonProperty("host_name")
        private String hostName;

        @JsonProperty("ip")
        private String hostIp;

        @JsonProperty("service_name")
        private String serviceName;

        private String env;

    }
}
