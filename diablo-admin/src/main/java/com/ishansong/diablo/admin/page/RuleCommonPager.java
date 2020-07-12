package com.ishansong.diablo.admin.page;

import com.ishansong.diablo.admin.vo.ServiceAppVO;
import lombok.Data;

import java.util.List;

@Data
public class RuleCommonPager<T> extends CommonPager<T> {

    private List<ServiceAppVO> serviceApps;

    public RuleCommonPager(PageParameter page, List dataList, List<ServiceAppVO> serviceApps) {
        super(page, dataList);
        this.serviceApps = serviceApps;
    }
}
