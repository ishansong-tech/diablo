package com.ishansong.diablo.admin.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonPager<T> implements Serializable {

    private static final long serialVersionUID = -1220101004792874251L;

    private PageParameter page;

    private List<T> dataList;
}
