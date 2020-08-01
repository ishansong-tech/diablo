package com.ishansong.diablo.config.web;

import lombok.Data;

/**
 * Created by jiangmin on 2020/8/1.
 */
@Data
public class Disruptor {
    private int bufferSize=4096;
    private int threadSize=1;

}
