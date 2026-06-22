package com.timmy.service;

import java.util.List;

import com.timmy.entity.AccessWeek;

public interface AccessWeekService {


    int deleteByPrimaryKey(Integer id);

    int insert(AccessWeek record);

    int insertSelective(AccessWeek record);
    AccessWeek selectByPrimaryKey(Integer id);
   int updateByPrimaryKeySelective(AccessWeek record);

    int updateByPrimaryKey(AccessWeek record);
    
    List<AccessWeek> selectAllAccessWeek();
    
    public void setAccessWeek();
}
