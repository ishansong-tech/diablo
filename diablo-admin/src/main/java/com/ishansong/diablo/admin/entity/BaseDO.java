package com.ishansong.diablo.admin.entity;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class BaseDO implements Serializable {

    private String id;

    private Timestamp dateCreated;

    private Timestamp dateUpdated;
}
