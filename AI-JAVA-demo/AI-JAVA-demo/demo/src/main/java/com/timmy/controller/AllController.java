package com.timmy.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.ls.LSInput;

import com.fasterxml.jackson.annotation.JacksonInject.Value;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mysql.fabric.xmlrpc.base.Data;
import com.timmy.entity.AccessDay;
import com.timmy.entity.AccessWeek;
import com.timmy.entity.Device;
import com.timmy.entity.DeviceStatus;
import com.timmy.entity.EnrollInfo;
import com.timmy.entity.LockGroup;
import com.timmy.entity.MachineCommand;
import com.timmy.entity.Msg;
import com.timmy.entity.Person;
import com.timmy.entity.PersonTemp;
import com.timmy.entity.Records;
import com.timmy.entity.SetUserReturnInfo;
import com.timmy.entity.UserInfo;
import com.timmy.entity.UserLock;
import com.timmy.entity.UserTemp;
import com.timmy.mapper.DeviceMapper;
import com.timmy.mapper.EnrollInfoMapper;
import com.timmy.service.AccessDayService;
import com.timmy.service.AccessWeekService;
import com.timmy.service.DeviceService;
import com.timmy.service.EnrollInfoService;
import com.timmy.service.LockGroupService;
import com.timmy.service.PersonService;
import com.timmy.service.RecordsService;
import com.timmy.service.UserLockService;
import com.timmy.util.ControllerBase;
import com.timmy.util.ImageProcess;
import com.timmy.websocket.WSServer;
import com.timmy.websocket.WebSocketPool;




@Controller

public class AllController extends ControllerBase {
	
	/*@Autowired
	EnrollInfoService enrollInfoService;
	*/
	
	
	
	@RequestMapping("/hello1")
	public String hello() {
		return "hello";
	}
    
	
	
	/*获取所有考勤机*/
	@ResponseBody
	@RequestMapping(value="/device",method=RequestMethod.GET)
	public Msg getAllDevice() {				
		List<Device>deviceList=deviceService.findAllDevice();		
		return Msg.success().add("device", deviceList);
	}
	
	/*获取所有考勤机*/
	@ResponseBody
	@RequestMapping(value="/enrollInfo",method=RequestMethod.GET)
	public Msg getAllEnrollInfo() {				
		List<Person>enrollInfo=personService.selectAll();
	
		return Msg.success().add("enrollInfo", enrollInfo);
	}
	
	
	/*采集所有的用户*/
	@ResponseBody
    @RequestMapping(value="/sendWs",method = RequestMethod.GET)
    public Msg sendWs(@RequestParam("deviceSn")String deviceSn) {
		String  message="{\"cmd\":\"getuserlist\",\"stn\":true}";
       
		System.out.println("sss"+deviceSn);
	
		//WebSocketPool.sendMessageToDeviceStatus(deviceSn, message);
		List<Device>deviceList=deviceService.findAllDevice();
		for (int i = 0; i < deviceList.size(); i++) {
			MachineCommand machineCommand=new MachineCommand();
			machineCommand.setContent(message);
			machineCommand.setName("getuserlist");
			machineCommand.setStatus(0);
			machineCommand.setSendStatus(0);
			machineCommand.setErrCount(0);
			machineCommand.setSerial(deviceList.get(i).getSerialNum());
			machineCommand.setGmtCrate(new Date());
			machineCommand.setGmtModified(new Date());
			machineCommand.setContent(message);
			machineComandService.addMachineCommand(machineCommand);
		}

        return  Msg.success();
    }
	
	
	@ResponseBody
	@RequestMapping(value="addPerson",method=RequestMethod.POST)
	public Msg addPerson(PersonTemp personTemp,MultipartFile pic,HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse) throws Exception {
		String path="C:/dynamicface/picture/";
	    System.out.println("图片真实路径"+path);
	    System.out.println("新增人员信息==================="+personTemp);
	    String photoName="";
	    String newName="";
	 //   EnrollInfo enrollInfo=new EnrollInfo();
	    if(pic!=null){
	    	if (pic.getOriginalFilename()!=null&&!("").equals(pic.getOriginalFilename())) {
	    		 photoName=pic.getOriginalFilename();	
	    		 newName=UUID.randomUUID().toString()+photoName.substring(photoName.lastIndexOf("."));
	    		 File photoFile=new File(path, newName);
	    			if (!photoFile.exists()) {
	    				photoFile.mkdirs();
	    			}
	    			pic.transferTo(photoFile);
			
			}
			
		}
	    Person person=new Person();
	    person.setId(personTemp.getUserId());
	    person.setName(personTemp.getName());
	    person.setRollId(personTemp.getPrivilege());
	    Person person2=personService.selectByPrimaryKey(personTemp.getUserId());
	    if(person2==null) {
	    	personService.insert(person);
	    }
	    if(personTemp.getPassword()!=null&&!personTemp.getPassword().equals("")) {
	    	EnrollInfo enrollInfoTemp2=new EnrollInfo();
	    	enrollInfoTemp2.setBackupnum(10);
	    	enrollInfoTemp2.setEnrollId(personTemp.getUserId());
	    	enrollInfoTemp2.setSignatures(personTemp.getPassword());
	    	enrollInfoService.insertSelective(enrollInfoTemp2);
	    }
	    if(personTemp.getCardNum()!=null&&!personTemp.getCardNum().equals("")) {
	    	EnrollInfo enrollInfoTemp3=new EnrollInfo();
	    	enrollInfoTemp3.setBackupnum(11);
	    	enrollInfoTemp3.setEnrollId(personTemp.getUserId());
	    	enrollInfoTemp3.setSignatures(personTemp.getCardNum());
	    	enrollInfoService.insertSelective(enrollInfoTemp3);
	    }
	    
	
	    if(newName!=null&&!newName.equals("")) {
	    	EnrollInfo enrollInfoTemp=new EnrollInfo();
	    	enrollInfoTemp.setBackupnum(50);
	    	enrollInfoTemp.setEnrollId(personTemp.getUserId());
	    	String base64Str=ImageProcess.imageToBase64Str("C:/dynamicface/picture/"+newName);
	    	enrollInfoTemp.setImagePath(newName);
	    	enrollInfoTemp.setSignatures(base64Str);
	    	System.out.println("图片数据长度"+base64Str.length());
	    	enrollInfoService.insertSelective(enrollInfoTemp);
	    }
	
		return  Msg.success();
		
	}
	
	
	
	@ResponseBody
	@RequestMapping(value="getUserInfo",method=RequestMethod.GET)
	public Msg getUserInfo(@RequestParam("deviceSn")String deviceSn) {
		System.out.println("进入controller");
		List<Person>person=personService.selectAll();
		List<EnrollInfo>enrollsPrepared=new ArrayList<EnrollInfo>();
        for (int i = 0; i < person.size(); i++) {
        	Long enrollId2=person.get(i).getId();
			List<EnrollInfo>enrollInfos=enrollInfoService.selectByEnrollId(enrollId2);		
			for (int j = 0; j < enrollInfos.size(); j++) {
				if(enrollInfos.get(j).getEnrollId()!=null&&enrollInfos.get(j).getBackupnum()!=null){
				enrollsPrepared.add(enrollInfos.get(j));
			}
			}
		}
        System.out.println("采集用户数据"+enrollsPrepared);
        personService.getSignature2(enrollsPrepared, deviceSn);
        
		return  Msg.success();
	}
	
	
	/*获取单个用户*/
	@ResponseBody
    @RequestMapping("sendGetUserInfo")
    public Msg sendGetUserInfo(@RequestParam("enrollId")int enrollId,@RequestParam("backupNum")int backupNum,@RequestParam("deviceSn")String deviceSn) {
		
		
		
		List<Device>deviceList=deviceService.findAllDevice();
		System.out.println("设备信息"+deviceList);
		
		String message="{\"cmd\":\"getuserinfo\",\"enrollid\":"+enrollId+",\"backupnum\":"+ backupNum+"}";
		
	
		machineComandService.addGetOneUserCommand(enrollId, backupNum, deviceSn);
		
		return Msg.success();
    }
	
/*	下发所有用户，面向选中考勤机*/
	@ResponseBody
	@RequestMapping(value="/setPersonToDevice",method = RequestMethod.GET)
	public Msg sendSetUserInfo(@RequestParam("deviceSn")String deviceSn){
	    
		personService.setUserToDevice2(deviceSn);
		return Msg.success();
		
	}
	
	
	@ResponseBody
	@RequestMapping(value="setUsernameToDevice",method=RequestMethod.GET)
	public Msg setUsernameToDevice(@RequestParam("deviceSn")String deviceSn) {
		personService.setUsernameToDevice(deviceSn);
		return Msg.success();
	}
	
	
	@ResponseBody
	@RequestMapping(value="/getDeviceInfo",method=RequestMethod.GET)
	public Msg getDeviceInfo(@RequestParam("deviceSn") String deviceSn){		
		    String message="{\"cmd\":\"getdevinfo\"}";	
		 
			MachineCommand machineCommand=new MachineCommand();
			machineCommand.setContent(message);
			machineCommand.setName("getdevinfo");
			machineCommand.setStatus(0);
			machineCommand.setSendStatus(0);
			machineCommand.setErrCount(0);
			machineCommand.setSerial(deviceSn);
			machineCommand.setGmtCrate(new Date());
			machineCommand.setGmtModified(new Date());
			
			machineComandService.addMachineCommand(machineCommand);
	     return Msg.success();	
	}
	
	
	
	
	/*下发单个用户到机器，对选中考勤机*/
	@ResponseBody
	@RequestMapping(value="/setOneUser",method = RequestMethod.GET)
	public Msg setOneUserTo(@RequestParam("enrollId")Long enrollId,@RequestParam("backupNum")int backupNum,@RequestParam("deviceSn")String deviceSn) {
		Person person=new Person();
		person=personService.selectByPrimaryKey(enrollId);
		EnrollInfo enrollInfo=new EnrollInfo();
		System.out.println("ba"+backupNum);
		enrollInfo=enrollInfoService.selectByBackupnum(enrollId, backupNum);
		if(enrollInfo!=null){
			personService.setUserToDevice(enrollId, person.getName(), backupNum, person.getRollId(), enrollInfo.getSignatures(),deviceSn);
			return Msg.success();
		}else if(backupNum==-1) {
			personService.setUserToDevice(enrollId, person.getName(), backupNum, 0, "",deviceSn);
			return Msg.success();
		}else{
			return Msg.fail();
		}
		
	}
	
	/*从考勤机删除用户*/
	@ResponseBody
	@RequestMapping(value="/deletePersonFromDevice",method = RequestMethod.GET)
	public Msg deleteDeviceUserInfo(@RequestParam("enrollId")Long enrollId,@RequestParam("deviceSn")String deviceSn){	
		
		System.out.println("删除用户devicesn==================="+deviceSn);
		personService.deleteUserInfoFromDevice(enrollId, deviceSn);
	//	personService.deleteByPrimaryKey(enrollId);
		return Msg.success();
	}
	
	
	/*初始化考勤机*/
	@ResponseBody
	@RequestMapping(value="/initSystem",method = RequestMethod.GET)
	public Msg initSystem(@RequestParam("deviceSn")String deviceSn) {
		System.out.println("初始化请求");
		String  message="{\"cmd\":\"enabledevice\"}";
		String  message2="{\"cmd\":\"settime\",\"cloudtime\":\"2020-12-23 13:49:30\"}";
		String s4="{\"cmd\":\"settime\",\"cloudtime\":\"2016-03-25 13:49:30\"}";
		String s2="{\"cmd\":\"setdevinfo\",\"deviceid\":1,\"language\":0,\"volume\":0,\"screensaver\":0,\"verifymode\":0,\"sleep\":0,\"userfpnum\":3,\"loghint\":1000,\"reverifytime\":0}";
		String s3="{\"cmd\":\"setdevlock\",\"opendelay\":5,\"doorsensor\":0,\"alarmdelay\":0,\"threat\":0,\"InputAlarm\":0,\"antpass\":0,\"interlock\":0,\"mutiopen\":0,\"tryalarm\":0,\"tamper\":0,\"wgformat\":0,\"wgoutput\":0,\"cardoutput\":0,\"dayzone\":[{\"day\":[{\"section\":\"01:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"}]},{\"day\":[{\"section\":\"02:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"}]},{\"day\":[{\"section\":\"03:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"}]},{\"day\":[{\"section\":\"04:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"}]},{\"day\":[{\"section\":\"05:00~00:0\n" + 
				"0\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"}]},{\"day\":[{\"section\":\"06:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"}]},{\"day\":[{\"section\":\"07:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"}]},{\"day\":[{\"section\":\"08:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"},{\"section\":\"00:00~00:00\"}]}],\"weekzone\":[{\"week\":[{\"day\":0},{\"day\":1},{\"day\":2},{\"day\":3},{\"day\":4},{\"day\":5},{\"day\":6}]},{\"week\":[{\"day\":10},{\"day\":11},{\"day\":12},{\"day\":13},{\"day\":14},{\"day\":15},{\"day\":16}]},{\"week\":[{\"day\":20},{\"day\":21},{\"day\":22},{\"day\":23},{\"day\":24},{\"day\":25},{\"day\":26}]},\n" + 
				"{\"week\":[{\"day\":30},{\"day\":31},{\"day\":32},{\"day\":33},{\"day\":34},{\"day\":35},{\"day\":36}]},{\"week\":[{\"day\":40},{\"day\":41},{\"day\":42},{\"day\":43},{\"day\":44},{\"day\":45},{\"day\":46}]},{\"week\":[{\"day\":50},{\"day\":51},{\"day\":52},{\"day\":53},{\"day\":54},{\"day\":55},{\"day\":56}]},{\"week\":[{\"day\":60},{\"day\":61},{\"day\":62},{\"day\":63},{\"day\":64},{\"day\":65},{\"day\":66}]},{\"week\":[{\"day\":70},{\"day\":71},{\"day\":72},{\"day\":73},{\"day\":74},{\"day\":75},{\"day\":76}]}],\"lockgroup\":[{\"group\":0},{\"group\":1},{\"group\":2},{\"group\":3},{\"group\":4}],\"nopentime\":[{\"day\":0},{\"day\":0},{\"day\":0},{\"day\":0},{\"day\":0},{\"day\":0},{\"day\":0}]}\n" + 
				"";
		
		String messageTemp="{\"cmd\":\"setuserlock\",\"count\":1,\"record\":[{\"enrollid\":1,\"weekzone\":1,\"weekzone2\":3,\"group\":1,\"starttime\":\"2010-11-11 00:00:00\",\"endtime\":\"2030-11-11 00:00:00\"}]}";
		String s5="{\"cmd\":\"enableuser\",\"enrollid\":1,\"enflag\":0}";
		String s6="{\"cmd\":\"getusername\",\"enrollid\":1}";
		String  message22="{\"cmd\":\"initsys\"}";
		
		MachineCommand machineCommand=new MachineCommand();
		machineCommand.setContent(message22);
		machineCommand.setName("initsys");
		machineCommand.setStatus(0);
		machineCommand.setErrCount(0);
		machineCommand.setSendStatus(0);
		machineCommand.setSerial(deviceSn);
		machineCommand.setGmtCrate(new Date());
		machineCommand.setGmtModified(new Date());
		
		machineComandService.addMachineCommand(machineCommand);
		 
		
		return Msg.success();
	}
	
	
	/*采集所有的考勤记录，面向所有机器*/
	@ResponseBody
	@RequestMapping(value="/getAllLog",method = RequestMethod.GET)
	public Msg getAllLog(@RequestParam("deviceSn")String deviceSn) {	
		String  message="{\"cmd\":\"getalllog\",\"stn\":true}";
	//	String messageTemp="{\"cmd\":\"getalllog\",\"stn\":true,\"from\":\"2020-12-03\",\"to\":\"2020-12-30\"}";
		MachineCommand machineCommand=new MachineCommand();
		machineCommand.setContent(message);
		machineCommand.setName("getalllog");
		machineCommand.setStatus(0);
		machineCommand.setSendStatus(0);
		machineCommand.setErrCount(0);
		machineCommand.setSerial(deviceSn);
		machineCommand.setGmtCrate(new Date());
		machineCommand.setGmtModified(new Date());
		
		machineComandService.addMachineCommand(machineCommand);
		return Msg.success();
		
		
	}
	
	/*采集所有的考勤记录，面向所有机器*/
	@ResponseBody
	@RequestMapping(value="/getNewLog",method = RequestMethod.GET)
	public Msg getNewLog(@RequestParam("deviceSn")String deviceSn) {	
		String  message="{\"cmd\":\"getnewlog\",\"stn\":true}";
	//	String messageTemp="{\"cmd\":\"getalllog\",\"stn\":true,\"from\":\"2020-12-03\",\"to\":\"2020-12-30\"}";
		System.out.println(message);
		MachineCommand machineCommand=new MachineCommand();
		machineCommand.setContent(message);
		machineCommand.setName("getnewlog");
		machineCommand.setStatus(0);
		machineCommand.setSendStatus(0);
		machineCommand.setErrCount(0);
		machineCommand.setSerial(deviceSn);
		machineCommand.setGmtCrate(new Date());
		machineCommand.setGmtModified(new Date());
		
		machineComandService.addMachineCommand(machineCommand);
		return Msg.success();
		
		
	}
	
	/*添加天时段,面向全部考勤机*/
	@ResponseBody
	@RequestMapping(value="/setAccessDay",method = RequestMethod.POST)
	public Msg setAccessDay(@ModelAttribute AccessDay accessDay) {
		if(accessaDayService.selectByPrimaryKey(accessDay.getId())!=null){
			return Msg.fail();
		}
		accessaDayService.insert(accessDay);
		
		accessaDayService.setAccessDay();
		return Msg.success();
	}
	
	
	/*添加周时段，面向全部考勤机*/
	@ResponseBody
	@RequestMapping(value="/setAccessWeek",method = RequestMethod.POST)
	public Msg setAccessWeek(@ModelAttribute AccessWeek accessWeek) {	
	//	accessWeek.set
		if(accessWeekService.selectByPrimaryKey(accessWeek.getId())!=null){
			return Msg.fail();
		}
		accessWeekService.insert(accessWeek);
		accessWeekService.setAccessWeek();	
		return Msg.success();
		
	}
	
	
	/*设置锁组合*/
	@ResponseBody
	@RequestMapping(value="/setLocckGroup",method = RequestMethod.POST)
	public Msg setLockGroup(@ModelAttribute LockGroup lockGroup) {		
		lockGroupService.setLockGroup(lockGroup);	
		return Msg.success();
	}
	
	/*设置用户锁权限*/
	@ResponseBody
	@RequestMapping(value="/setUserLock",method=RequestMethod.POST)
	public Msg setUserLock(@ModelAttribute UserLock userLock) {
		
		userLockService.setUserLock(userLock, userLock.getStarttime(), userLock.getEndtime());
		return Msg.success();
	}
	
	
	/*显示员工列表*/
	@RequestMapping(value="/emps")
	@ResponseBody
	public Msg getAllPersonFromDB(@RequestParam(value="pn",defaultValue="1") Integer pn) {
		// 引入 PageHelper 分页插件
				/**
				 * 在查询之前只需要调用，传入要显示的页码，以及每页显示的数量 startPage 后紧跟的查询就是分页查询
				 */
	//	PageHelper.startPage(pn, 8);
		List<Person>personList=personService.selectAll();
		List<EnrollInfo>enrollList=enrollInfoService.selectAll();
		List<UserInfo>emps=new ArrayList<UserInfo>();
		for (int i = 0; i < personList.size(); i++) {
			UserInfo userInfo=new UserInfo();
			userInfo.setEnrollId(personList.get(i).getId());
			userInfo.setAdmin(personList.get(i).getRollId());
			userInfo.setName(personList.get(i).getName());
			for (int j = 0; j < enrollList.size(); j++) {
				if(personList.get(i).getId().longValue()==enrollList.get(j).getEnrollId().longValue()) {
				
					if (enrollList.get(j).getBackupnum()==50) {
						userInfo.setImagePath(enrollList.get(j).getImagePath());
					}
				}
			}
			emps.add(userInfo);
		}
		PageHelper.startPage(pn, emps.size());
	
		PageInfo page= new PageInfo(emps,5);
//		System.out.println("用户数量"+enrollList);
		return Msg.success().add("pageInfo", page);
		
	}
	
	
	

	
	
	/*显示所有的打卡记录*/
	@RequestMapping(value="/records")
	@ResponseBody
	public Msg getAllLogFromDB(@RequestParam(value="pn",defaultValue="1") Integer pn){
		PageHelper.startPage(pn, 8);
		
		List<Records>records=recordService.selectAllRecords();
		
		PageInfo page=new PageInfo(records, 5);

		return Msg.success().add("pageInfo", page);
		
	}
	
	
	
	/*设置周时间段*/
	@RequestMapping(value="/accessDays",method = RequestMethod.GET)
	@ResponseBody
	public Msg getAccessDayFromDB() {
		List<AccessDay>accessDays=accessaDayService.selectAll();
		return Msg.success().add("accessdays", accessDays);
		
	}
	
	
	
	public Msg uploadUserToDevice(@RequestParam("enrollId")Long enrollId) {
		
		Person person=personService.selectByPrimaryKey(enrollId);
		
		return Msg.success();
	}
	
	@RequestMapping(value="/openDoor",method = RequestMethod.GET)
	@ResponseBody
	public Msg openDoor(@RequestParam("doorNum")int doorNum,@RequestParam("deviceSn")String deviceSn) {
		 String message="{\"cmd\":\"opendoor\""+",\"doornum\":"+doorNum+"}";
		   
		 MachineCommand machineCommand=new MachineCommand();
			machineCommand.setContent(message);
			machineCommand.setName("opendoor");
			machineCommand.setStatus(0);
			machineCommand.setSendStatus(0);
			machineCommand.setErrCount(0);
			machineCommand.setSerial(deviceSn);
			machineCommand.setGmtCrate(new Date());
			machineCommand.setGmtModified(new Date());
			
			machineComandService.addMachineCommand(machineCommand);
		return Msg.success();
	}
	
	@RequestMapping(value="/getDevLock",method = RequestMethod.GET)
	@ResponseBody
	public Msg getDevLock(@RequestParam("deviceSn")String deviceSn) {
		String message="{\"cmd\":\"getdevlock\"}";
		   
				 MachineCommand machineCommand=new MachineCommand();
					machineCommand.setContent(message);
					machineCommand.setName("getdevlock");
					machineCommand.setStatus(0);
					machineCommand.setSendStatus(0);
					machineCommand.setErrCount(0);
					machineCommand.setSerial(deviceSn);
					machineCommand.setGmtCrate(new Date());
					machineCommand.setGmtModified(new Date());
					
					machineComandService.addMachineCommand(machineCommand);
					return Msg.success();
		
	}
	
	
	@RequestMapping(value="/geUSerLock",method = RequestMethod.GET)
	@ResponseBody
	public Msg getUserLock(@RequestParam("enrollId")Integer enrollId,@RequestParam("deviceSn")String deviceSn) {
		
		String message="{\"cmd\":\"getuserlock\",\"enrollid\":"+enrollId+"}";
		 MachineCommand machineCommand=new MachineCommand();
			machineCommand.setContent(message);
			machineCommand.setName("getuserlock");
			machineCommand.setStatus(0);
			machineCommand.setSendStatus(0);
			machineCommand.setErrCount(0);
			machineCommand.setSerial(deviceSn);
			machineCommand.setGmtCrate(new Date());
			machineCommand.setGmtModified(new Date());
			
			machineComandService.addMachineCommand(machineCommand);
		
		return Msg.success();		
	}
	
	@RequestMapping(value="/cleanAdmin",method = RequestMethod.GET)
	@ResponseBody
	public Msg cleanAdmin(@RequestParam("deviceSn")String deviceSn) {
		String message="{\"cmd\":\"cleanadmin\"}";
		   
				 MachineCommand machineCommand=new MachineCommand();
					machineCommand.setContent(message);
					machineCommand.setName("cleanadmin");
					machineCommand.setStatus(0);
					machineCommand.setSendStatus(0);
					machineCommand.setErrCount(0);
					machineCommand.setSerial(deviceSn);
					machineCommand.setGmtCrate(new Date());                         
					machineCommand.setGmtModified(new Date());
					
					machineComandService.addMachineCommand(machineCommand);
					return Msg.success();
		
	}
	
}
