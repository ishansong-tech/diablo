package com.ishansong.diablo.admin.pre.mapper.backup;

import com.ishansong.diablo.admin.entity.SelectorDO;
import com.ishansong.diablo.admin.query.SelectorVersionQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Mapper
@Repository("backupSelectorMapper")
public interface SelectorMapper {

    SelectorDO selectById(String id);

    List<SelectorDO> findByIds(@Param("ids") List<String> ids);

    int delete(String id);

    int insertSelective(SelectorDO selectorDO);

    SelectorDO findByIdAndDatePublished(@Param("id") String id, @Param("datePublished") Timestamp datePublished);

    List<SelectorDO> selectByQuery(SelectorVersionQuery selectorVersionQuery);
}
