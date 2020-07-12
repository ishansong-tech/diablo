package com.ishansong.diablo.admin.listener;

import lombok.Data;

@Data
public final class DiabloDomain {

    private static final DiabloDomain DIABLO_DOMAIN = new DiabloDomain();

    private String httpPath;

    private DiabloDomain() {
    }

    public static DiabloDomain getInstance() {
        return DIABLO_DOMAIN;
    }

}
