package com.ishansong.diablo.core.utils;

import com.google.common.base.Strings;
import com.ishansong.diablo.core.constant.Constants;
import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UrlUtils {

    private static final Pattern PATTERN = Pattern
            .compile("(http:\\/\\/|https:\\/\\/)?(?:(?:[0,1]?\\d?\\d|2[0-4]\\d|25[0-5])\\.){3}(?:[0,1]?\\d?\\d|2[0-4]\\d|25[0-5]):\\d{0,5}");

    private static final String HTTP = "http";

    public static boolean checkUrl(final String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        if (checkIP(url)) {
            String[] hostPort;
            if (url.startsWith(HTTP)) {
                final String[] http = StringUtils.split(url, "\\/\\/");
                hostPort = StringUtils.split(http[1], Constants.COLONS);
            } else {
                hostPort = StringUtils.split(url, Constants.COLONS);
            }
            return isHostConnector(hostPort[0], Integer.parseInt(hostPort[1]));
        } else {
            return isHostReachable(url);
        }
    }

    private static boolean checkIP(final String url) {
        return PATTERN.matcher(url).matches();
    }

    private static boolean isHostConnector(final String host, final int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static boolean isHostReachable(final String host) {
        try {
            return InetAddress.getByName(host).isReachable(1000);
        } catch (IOException ignored) {
        }
        return false;
    }

    public static Map<String, Object> transQueryStringToMap(String queryString, String separator, String pairSeparator) {
        if(Strings.isNullOrEmpty(queryString) || Strings.isNullOrEmpty(separator) || Strings.isNullOrEmpty(pairSeparator)){
            throw new RuntimeException("Please check the request parameters");
        }
        String[] strings = queryString.trim().split(separator);
        Map<String, Object> map = new HashMap<>(1024);
        for(String entry :  strings){
            if(Strings.isNullOrEmpty(entry) || Strings.isNullOrEmpty(entry.trim())){
                continue;
            }
            String[] kv = entry.trim().split(pairSeparator);
            map.put(kv.length >= 1 ? kv[0] : null, kv.length >= 2 ? kv[1] : null);
        }
        return map;
    }

    public static Map<String, Object> transQueryStringToMap(String queryString) {
        return transQueryStringToMap(queryString , "&" , "=");
    }

    public static String getQueryString(String queryString , String key) {
        String value = null;
        if(!Strings.isNullOrEmpty(queryString) && !Strings.isNullOrEmpty(queryString)){
            Map<String, Object>  params = UrlUtils.transQueryStringToMap(queryString);
            Object entryValue = params.get(key);
            value = (entryValue == null ? null : (String) entryValue);
        }
        return value;
    }

}
