package com.ishansong.diablo.core.model.dubbo;

import lombok.Data;

@Data
public class ParamMetasData {

    /**
     * 如果没有字段名 key则和type一致, key 示例: userId
     */
    private String key;

    /**
     * 参数类型 示例 java.lang.Long
     */
    private String type;

    private Integer keySort;
}
