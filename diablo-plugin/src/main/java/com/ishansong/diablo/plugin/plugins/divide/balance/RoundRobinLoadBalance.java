package com.ishansong.diablo.plugin.plugins.divide.balance;

import com.ishansong.diablo.core.enums.LoadBalanceEnum;
import com.ishansong.diablo.core.model.selector.DivideUpstream;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private final static int recyclePeriod = 60000;

    private ConcurrentMap<String, ConcurrentMap<String, WeightedRoundRobin>> methodWeightMap = new ConcurrentHashMap<>(16);

    private AtomicBoolean updateLock = new AtomicBoolean();

    @Override
    public DivideUpstream doSelect(final List<DivideUpstream> upstreamList, final String ip) {
        String key = upstreamList.get(0).getUpstreamUrl();
        ConcurrentMap<String, WeightedRoundRobin> map = methodWeightMap.get(key);
        if (map == null) {
            methodWeightMap.putIfAbsent(key, new ConcurrentHashMap<>(16));
            map = methodWeightMap.get(key);
        }
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        long now = System.currentTimeMillis();
        DivideUpstream selectedInvoker = null;
        WeightedRoundRobin selectedWRR = null;
        for (DivideUpstream upstream : upstreamList) {
            String rKey = upstream.getUpstreamUrl();
            WeightedRoundRobin weightedRoundRobin = map.get(rKey);
            int weight = getWeight(upstream);
            if (weightedRoundRobin == null) {
                weightedRoundRobin = new WeightedRoundRobin();
                weightedRoundRobin.setWeight(weight);
                map.putIfAbsent(rKey, weightedRoundRobin);
            }
            if (weight != weightedRoundRobin.getWeight()) {
                //weight changed
                weightedRoundRobin.setWeight(weight);
            }
            long cur = weightedRoundRobin.increaseCurrent();
            weightedRoundRobin.setLastUpdate(now);
            if (cur > maxCurrent) {
                maxCurrent = cur;
                selectedInvoker = upstream;
                selectedWRR = weightedRoundRobin;
            }
            totalWeight += weight;
        }
        if (!updateLock.get() && upstreamList.size() != map.size()) {
            if (updateLock.compareAndSet(false, true)) {
                try {
                    // copy -> modify -> update reference
                    ConcurrentMap<String, WeightedRoundRobin> newMap = new ConcurrentHashMap<>(map);
                    newMap.entrySet().removeIf(item -> now - item.getValue().getLastUpdate() > recyclePeriod);
                    methodWeightMap.put(key, newMap);
                } finally {
                    updateLock.set(false);
                }
            }
        }
        if (selectedInvoker != null) {
            selectedWRR.sel(totalWeight);
            return selectedInvoker;
        }
        // should not happen here
        return upstreamList.get(0);
    }

    @Override
    public String algorithm() {
        return LoadBalanceEnum.ROUND_ROBIN.getName();
    }

    protected static class WeightedRoundRobin {

        private int weight;

        private AtomicLong current = new AtomicLong(0);

        private long lastUpdate;

        int getWeight() {
            return weight;
        }

        void setWeight(final int weight) {
            this.weight = weight;
            current.set(0);
        }

        long increaseCurrent() {
            return current.addAndGet(weight);
        }

        void sel(final int total) {
            current.addAndGet(-1 * (long)total);
        }

        long getLastUpdate() {
            return lastUpdate;
        }

        void setLastUpdate(final long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }

}
