package com.timmy.service;

import java.util.List;

import com.timmy.entity.AccessDay;

public interface AccessDayService {
	int deleteByPrimaryKey(Integer id);

    int insert(AccessDay record);

    int insertSelective(AccessDay record);

   

    AccessDay selectByPrimaryKey(Integer id);

   

    int updateByPrimaryKeySelective(AccessDay record);

    int updateByPrimaryKey(AccessDay record);
    
 
    List<AccessDay> selectAll();

	void setAccessDay();
}
