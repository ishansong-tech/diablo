package com.ishansong.diablo.core.model.rule;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FailbackData {

    private String ruleId;

    private String upstreamHost;
}
