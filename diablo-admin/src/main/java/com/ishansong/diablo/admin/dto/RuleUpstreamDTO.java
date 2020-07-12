package com.ishansong.diablo.admin.dto;

import com.ishansong.diablo.admin.entity.ServiceUpstreamDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleUpstreamDTO {

    private String upstreamHost;

    private String protocol;

    private String upstreamUrl;

    private int weight;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleUpstreamDTO that = (RuleUpstreamDTO) o;
        return weight == that.weight &&
                Objects.equals(upstreamHost, that.upstreamHost) &&
                Objects.equals(upstreamUrl, that.upstreamUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(upstreamHost, upstreamUrl, weight);
    }

    public static RuleUpstreamDTO buildRuleUpstream(ServiceUpstreamDO serviceUpstreamDO) {

        return new RuleUpstreamDTO(
                serviceUpstreamDO.getHostName(),
                "",
                serviceUpstreamDO.getHostIp() + ":" + serviceUpstreamDO.getPort(),
                100
        );
    }
}
