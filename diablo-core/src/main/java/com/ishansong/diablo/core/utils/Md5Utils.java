package com.ishansong.diablo.core.utils;

import java.security.MessageDigest;

public class Md5Utils {

    private static String md5(String src, String charset) {
        MessageDigest md5;
        StringBuilder hexValue = new StringBuilder(32);
        try {
            md5 = MessageDigest.getInstance("MD5");

            byte[] byteArray;
            byteArray = src.getBytes(charset);

            byte[] md5Bytes = md5.digest(byteArray);

            for (byte md5Byte : md5Bytes) {
                int val = ((int) md5Byte) & 0xff;
                if (val < 16) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(val));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return hexValue.toString();
    }

    public static String md5(String src) {
        return md5(src, "UTF-8");
    }

}
