package com.timmy.util;

import org.springframework.beans.factory.annotation.Autowired;

import com.timmy.service.AccessDayService;
import com.timmy.service.AccessWeekService;
import com.timmy.service.DeviceService;
import com.timmy.service.EnrollInfoService;
import com.timmy.service.LockGroupService;
import com.timmy.service.MachineCommandService;
import com.timmy.service.PersonService;
import com.timmy.service.RecordsService;
import com.timmy.service.UserLockService;

public class ControllerBase {

	@Autowired
	protected  AccessDayService accessaDayService;
	
    @Autowired
    protected  AccessWeekService accessWeekService;
    
    @Autowired
    protected   LockGroupService lockGroupService;
    
    @Autowired
    protected  UserLockService userLockService;
    
    @Autowired
    protected  EnrollInfoService enrollInfoService;
    
    @Autowired
    protected  PersonService personService;
    
    @Autowired
    protected  RecordsService recordService;
    
    @Autowired
    protected  DeviceService deviceService;
    
    @Autowired
    protected MachineCommandService machineComandService;
}
