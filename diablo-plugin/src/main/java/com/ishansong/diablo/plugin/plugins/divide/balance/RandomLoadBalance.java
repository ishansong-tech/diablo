package com.ishansong.diablo.plugin.plugins.divide.balance;

import com.ishansong.diablo.core.enums.LoadBalanceEnum;
import com.ishansong.diablo.core.model.selector.DivideUpstream;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance extends AbstractLoadBalance {

    private static final Random RANDOM = new Random();

    @Override
    public DivideUpstream doSelect(final List<DivideUpstream> upstreamList, final String ip) {
        // 总个数
        int length = upstreamList.size();
        // 总权重
        int totalWeight = 0;
        // 权重是否都一样
        boolean sameWeight = true;
        for (int i = 0; i < length; i++) {
            int weight = getWeight(upstreamList.get(i));
            // 累计总权重
            totalWeight += weight;
            if (sameWeight && i > 0
                    && weight != getWeight(upstreamList.get(i - 1))) {
                // 计算所有权重是否一样
                sameWeight = false;
            }
        }
        if (totalWeight > 0 && !sameWeight) {
            // 如果权重不相同且权重大于0则按总权重数随机
            int offset = RANDOM.nextInt(totalWeight);
            // 并确定随机值落在哪个片断上
            for (DivideUpstream divideUpstream : upstreamList) {
                offset -= getWeight(divideUpstream);
                if (offset < 0) {
                    return divideUpstream;
                }
            }
        }
        // 如果权重相同或权重为0则均等随机
        return upstreamList.get(RANDOM.nextInt(length));
    }

    @Override
    public String algorithm() {
        return LoadBalanceEnum.RANDOM.getName();
    }
}
