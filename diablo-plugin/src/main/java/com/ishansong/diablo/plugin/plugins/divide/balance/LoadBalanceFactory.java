package com.ishansong.diablo.plugin.plugins.divide.balance;

import com.ishansong.diablo.core.utils.SpiLoadFactory;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LoadBalanceFactory {


    private final static Map<String, LoadBalance> loaders = StreamSupport.stream(SpiLoadFactory.loadAll(LoadBalance.class).spliterator(), false)
                                                                         .collect(Collectors.toMap(LoadBalance::algorithm, e -> e));

    private final static LoadBalance defaultBalance = new RandomLoadBalance();

    public static LoadBalance of(final String algorithm) {
        return loaders.getOrDefault(algorithm, defaultBalance);
    }
}
