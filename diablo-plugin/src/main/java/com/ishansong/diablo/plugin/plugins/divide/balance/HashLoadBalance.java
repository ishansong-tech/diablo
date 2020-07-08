package com.ishansong.diablo.plugin.plugins.divide.balance;

import com.ishansong.diablo.core.enums.LoadBalanceEnum;
import com.ishansong.diablo.core.exception.DiabloException;
import com.ishansong.diablo.core.model.selector.DivideUpstream;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class HashLoadBalance extends AbstractLoadBalance {

    private static final int VIRTUAL_NODE_NUM = 5;

    @Override
    public DivideUpstream doSelect(final List<DivideUpstream> upstreamList, final String ip) {
        final TreeMap<Long, DivideUpstream> treeMap = new TreeMap<>();
        for (DivideUpstream address : upstreamList) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                long addressHash = hash("DIABLO-" + address.getUpstreamUrl() + "-HASH-" + i);
                treeMap.put(addressHash, address);
            }
        }
        long hash = hash(String.valueOf(ip));
        SortedMap<Long, DivideUpstream> lastRing = treeMap.tailMap(hash);
        if (!lastRing.isEmpty()) {
            return lastRing.get(lastRing.firstKey());
        }
        return treeMap.firstEntry().getValue();
    }

    @Override
    public String algorithm() {
        return LoadBalanceEnum.HASH.getName();
    }

    private static long hash(final String key) {
        // md5 byte
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new DiabloException("MD5 not supported", e);
        }
        md5.reset();
        byte[] keyBytes;
        try {
            keyBytes = key.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new DiabloException("Unknown string :" + key, e);
        }

        md5.update(keyBytes);
        byte[] digest = md5.digest();

        // hash code, Truncate to 32-bits
        long hashCode = (long) (digest[3] & 0xFF) << 24
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | (digest[0] & 0xFF);
        return hashCode & 0xffffffffL;
    }

}
