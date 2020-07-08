package com.ishansong.diablo.core.model.rule;

import com.ishansong.diablo.core.constant.Constants;
import lombok.Data;

@Data
public class DubboRuleHandler {

    private String version;

    private String group;

    private Integer retries = 0;

    private String loadBalance;

    private Integer timeout = Constants.TIME_OUT;
}
