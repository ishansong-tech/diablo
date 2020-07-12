package com.ishansong.diablo.admin.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class SelectorPublishDO {

    private String id;

    private Timestamp datePublished;

    private Timestamp dateRollbacked;
}
