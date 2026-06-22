package com.timmy.mapper;

import com.timmy.entity.AccessDay;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AccessDayMapper {
  
    int deleteByPrimaryKey(Integer id);

    int insert(AccessDay record);

    int insertSelective(AccessDay record);

   

    AccessDay selectByPrimaryKey(Integer id);

   

    int updateByPrimaryKeySelective(AccessDay record);

    int updateByPrimaryKey(AccessDay record);
    
    List<AccessDay> selectAll();
}