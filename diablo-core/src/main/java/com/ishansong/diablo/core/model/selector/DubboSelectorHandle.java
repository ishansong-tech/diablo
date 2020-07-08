package com.ishansong.diablo.core.model.selector;

import lombok.Data;

@Data
public class DubboSelectorHandle {

    private String registry;

    private String appName;

    private String protocol;
}
