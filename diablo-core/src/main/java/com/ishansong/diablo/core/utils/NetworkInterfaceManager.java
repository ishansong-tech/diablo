package com.ishansong.diablo.core.utils;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.*;

@Slf4j
public enum NetworkInterfaceManager {
    INSTANCE;

    NetworkInterfaceManager() {
        load();
    }

    private InetAddress local;
    private InetAddress localhost;

    public String getLocalHostAddress() {
        return local.getHostAddress();
    }

    public String getLocalHostName() {

        try {
            if (null == localhost) {
                localhost = InetAddress.getLocalHost();
            }

            return localhost.getHostName();
        } catch (UnknownHostException e) {

            return local.getHostName();
        }
    }

    private void load() {

        String ip = getProperty("host.ip");

        if (ip != null) {
            try {
                local = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                log.error("NetworkInterfaceManager getByName Failed to load, cause={}", Throwables.getStackTraceAsString(e));
            }
        }

        InetAddress local = null;
        List<InetAddress> address = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            List<NetworkInterface> nis = interfaces == null ? Collections.emptyList() : Collections.list(interfaces);

            nis.sort(Comparator.comparing(NetworkInterface::getIndex));

            for (Iterator<NetworkInterface> iterator = nis.iterator(); iterator.hasNext(); ) {
                NetworkInterface networkInterface = iterator.next();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    address.addAll(Collections.list(networkInterface.getInetAddresses()));
                }
            }

            local = findValidateIp(address);

            if (local != null) {
                this.local = local;

                return;
            }
        } catch (Exception e) {
            log.error("NetworkInterfaceManager interfaces Failed to load, cause={}", Throwables.getStackTraceAsString(e));
        }

        this.local = InetAddress.getLoopbackAddress();
    }

    private InetAddress findValidateIp(List<InetAddress> addresses) {

        InetAddress local = null;

        int maxWeight = -1;

        for (Iterator<InetAddress> iterator = addresses.iterator(); iterator.hasNext(); ) {
            InetAddress address = iterator.next();

            if (!(address instanceof Inet4Address)) {
                continue;
            }

            int weight = 0;

            if (address.isSiteLocalAddress()) {
                weight += 8;
            }

            if (address.isLinkLocalAddress()) {
                weight += 4;
            }

            if (address.isLoopbackAddress()) {
                weight += 2;
            }

            if (weight > maxWeight) {
                maxWeight = weight;
                local = address;
            }
        }

        return local;
    }

    private String getProperty(String name) {
        String property = System.getProperty(name);

        if (property == null) {
            property = System.getenv(name);
        }

        return property;
    }


}
