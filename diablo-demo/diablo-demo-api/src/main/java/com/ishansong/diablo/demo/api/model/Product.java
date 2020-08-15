package com.ishansong.diablo.demo.api.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by jiangmin on 2019/8/5.
 */
@Data
public class Product implements Serializable {

    private String id;
    private String name;
}
