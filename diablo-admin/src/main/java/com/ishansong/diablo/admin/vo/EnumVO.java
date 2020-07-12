package com.ishansong.diablo.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnumVO implements Serializable {

    private Object code;

    private String name;

    private Boolean support;
}
