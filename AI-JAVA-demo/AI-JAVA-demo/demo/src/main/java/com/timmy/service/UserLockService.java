package com.timmy.service;

import com.timmy.entity.UserLock;

public interface UserLockService {

	
	void setUserLock(UserLock userLock,String starTime,String endTime);

}
