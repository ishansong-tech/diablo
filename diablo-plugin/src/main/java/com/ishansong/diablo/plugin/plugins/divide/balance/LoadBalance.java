package com.ishansong.diablo.plugin.plugins.divide.balance;

import com.ishansong.diablo.core.model.selector.DivideUpstream;

import java.util.List;

public interface LoadBalance {

    DivideUpstream select(List<DivideUpstream> upstreamList, String ip);

    String algorithm();
}
