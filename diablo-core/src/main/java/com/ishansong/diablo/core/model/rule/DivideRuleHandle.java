package com.ishansong.diablo.core.model.rule;

import lombok.Data;

import java.io.Serializable;

@Data
public class DivideRuleHandle implements Serializable {

    private String loadBalance;

    private int retry;

    private int timeout;

}
