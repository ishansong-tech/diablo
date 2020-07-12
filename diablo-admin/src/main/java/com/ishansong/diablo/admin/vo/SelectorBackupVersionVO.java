package com.ishansong.diablo.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectorBackupVersionVO {

    private String id;
    
    private Long timestamp;

    private String rollbackDate;

    private String remark;

}
