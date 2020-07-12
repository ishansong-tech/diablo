package com.ishansong.diablo.admin.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectorConditionQuery implements Serializable {

    private String selectorId;
}
