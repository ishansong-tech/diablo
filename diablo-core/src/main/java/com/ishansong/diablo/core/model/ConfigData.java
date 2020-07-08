package com.ishansong.diablo.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ConfigData<T> implements Serializable {

    private String md5;

    private long lastModifyTime;

    private List<T> data;

}
