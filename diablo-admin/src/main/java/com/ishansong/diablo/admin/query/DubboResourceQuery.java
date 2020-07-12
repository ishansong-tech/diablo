package com.ishansong.diablo.admin.query;

import com.ishansong.diablo.admin.page.PageParameter;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DubboResourceQuery {

    private String serviceName;

    private String namespace;

    private PageParameter pageParameter;
}
