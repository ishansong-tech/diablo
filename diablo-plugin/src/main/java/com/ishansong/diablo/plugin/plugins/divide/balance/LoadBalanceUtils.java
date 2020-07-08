package com.ishansong.diablo.plugin.plugins.divide.balance;

import com.ishansong.diablo.core.model.selector.DivideUpstream;

import java.util.List;

public class LoadBalanceUtils {

    public static DivideUpstream selector(final List<DivideUpstream> upstreamList, final String rule, final String ip) {
        return LoadBalanceFactory.of(rule).select(upstreamList, ip);
    }

}
