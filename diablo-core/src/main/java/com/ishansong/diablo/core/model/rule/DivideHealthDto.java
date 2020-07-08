package com.ishansong.diablo.core.model.rule;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class DivideHealthDto implements Serializable {

    private boolean healthStatus;

    private String healthUri;

    private String serviceName;

}
