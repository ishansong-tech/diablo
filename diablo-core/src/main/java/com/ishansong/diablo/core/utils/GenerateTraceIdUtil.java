package com.ishansong.diablo.core.utils;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
public enum GenerateTraceIdUtil {
    INSTANCE;

    GenerateTraceIdUtil() {
        idPrefix = initIdPrefix();
    }

    private String idPrefix;

    private final Splitter IP_SPLITTER = Splitter.on(".").omitEmptyStrings().trimResults();
    private final Splitter MULTI_IP_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
    private final Splitter HOST_SPLITTER = Splitter.on(":").omitEmptyStrings().trimResults();
    private final Random random = new Random();

    private String initIdPrefix() {

        return "trace-" + generateIpHex(NetworkInterfaceManager.INSTANCE.getLocalHostAddress()) + "-";
    }

    public String getNextId(ServerHttpRequest request, String upstreamHost) {

        StringBuilder builder = new StringBuilder();

        int digit = random.nextInt(100);
        try {
            String routeIp = HOST_SPLITTER.split(upstreamHost).iterator().next();

            builder.append(idPrefix).append(System.currentTimeMillis()).append("-")
                   .append(generateClientIpHex(request)).append("-").append(digit).append("-").append(generateIpHex(routeIp));
        } catch (Exception e) {
            log.warn("GenerateTraceIdUtil general trace id fail, routeIp={}, cause={}", upstreamHost, Throwables.getStackTraceAsString(e));
        }

        return builder.toString();
    }

    TimeBasedGenerator generator = Generators.timeBasedGenerator();

    public String getRequestTraceId(ServerHttpRequest request) {


        try {
            String traceId = "trace-" + generator.generate().toString() + "-" + generateClientIpHex(request);

            return traceId;
        } catch (Exception e) {
            log.warn("GenerateTraceIdUtil.getRequestTraceId trace id fail, cause={}", Throwables.getStackTraceAsString(e));
        }

        return "trace-null";
    }

    private String generateIpHex(String ip) {

        List<String> items = IP_SPLITTER.splitToList(ip);
        byte[] bytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) Integer.parseInt(items.get(i));
        }

        StringBuilder builder = new StringBuilder(bytes.length / 2);
        for (byte b : bytes) {
            builder.append(Integer.toHexString((b >> 4) & 0x0F));
            builder.append(Integer.toHexString(b & 0x0F));
        }

        return builder.toString();
    }

    private String generateTraceIdHex(List<Integer> items) {

        byte[] bytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) items.get(i).intValue();
        }

        StringBuilder builder = new StringBuilder(bytes.length / 2);
        for (byte b : bytes) {
            builder.append(Integer.toHexString((b >> 4) & 0x0F));
            builder.append(Integer.toHexString(b & 0x0F));
        }

        return builder.toString();
    }

    private String generateClientIpHex(ServerHttpRequest request) {

        String forwardedFor = request.getHeaders().getFirst("X-FORWARDED-FOR");

        String ip;
        if (!Strings.isNullOrEmpty(forwardedFor)) {
            ip = forwardedFor;

            if (forwardedFor.contains(",")) {
                ip = MULTI_IP_SPLITTER.split(forwardedFor).iterator().next();
            }
        } else {
            ip = Optional.ofNullable(request.getRemoteAddress()).map(InetSocketAddress::getAddress).map(InetAddress::getHostAddress).orElse(null);
        }

        if (Strings.isNullOrEmpty(ip)) {
            return "nl";
        }

        return ip;
    }
}
