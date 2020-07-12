package com.ishansong.diablo.admin.mapper.backup;

import com.ishansong.diablo.admin.entity.SelectorDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("rollBackupSelectorMapper")
public interface SelectorMapper {

    SelectorDO selectById(String id);

    List<SelectorDO> findByIds(@Param("ids") List<String> ids);

    int delete(String id);

    int insertSelective(SelectorDO selectorDO);
}
