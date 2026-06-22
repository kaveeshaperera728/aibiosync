package com.timmy.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.UUID;

import javax.websocket.OnError;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.web.context.ContextLoader;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timmy.entity.Device;
import com.timmy.entity.DeviceStatus;
import com.timmy.entity.EnrollInfo;
import com.timmy.entity.MachineCommand;
import com.timmy.entity.Person;
import com.timmy.entity.Records;
import com.timmy.entity.SetUserReturnInfo;
import com.timmy.entity.UserTemp;
import com.timmy.mapper.DeviceMapper;
import com.timmy.mapper.MachineCommandMapper;
import com.timmy.service.DeviceService;
import com.timmy.service.EnrollInfoService;
import com.timmy.service.PersonService;
import com.timmy.service.RecordsService;
import com.timmy.service.UserLockService;
import com.timmy.util.ImageProcess;
import com.timmy.util.RestTemplateUtil;



public class WSServer extends WebSocketServer{


	@Autowired
	DeviceService deviceService;
	
	@Autowired
	RecordsService recordsService;
	
	@Autowired
	PersonService personService;
	
	@Autowired
	EnrollInfoService enrollInfoService;
	
	@Autowired
	UserLockService userLockService;
	
	@Autowired
	MachineCommandMapper machineCommandMapper;
	
	int j=0;
    int h=0;
    int e=0;
   static int l;
   Long timeStamp=0L;
   Long timeStamp2=0L;
   public static boolean setUserResult;
   public static Logger logger = LoggerFactory.getLogger(WSServer.class);
  
 //  private Timer timer;
	  public WSServer(InetSocketAddress address) {
	        super(address);
	        logger.info("地址" + address);
	      
	        
	    }

	    public WSServer(int port) throws UnknownHostException {
	        super(new InetSocketAddress(port));
	        logger.info("端口" + port);
	    }
	  
	  
	@Override
	public void onOpen(org.java_websocket.WebSocket conn,
			ClientHandshake handshake) {
		// TODO Auto-generated method stub
	//	deviceService=(DeviceService)ContextLoader.getCurrentWebApplicationContext().getBean(DeviceService.class);
		  System.out.println("有人连接Socket conn:" + conn);
	      //  l++;
		 logger.info("有人连接Socket conn:" + conn.getRemoteSocketAddress());
		 l++;
		
	}

	
	
	@Override
	public void onClose(org.java_websocket.WebSocket conn, int code,
			String reason, boolean remote) {
		// TODO Auto-generated method stub
		    String sn=WebSocketPool.removeDeviceByWebsocket(conn);
			//deviceService.updateStatusByPrimarykey(id, status)
			Device d1=deviceService.selectDeviceBySerialNum(sn);
			deviceService.updateStatusByPrimaryKey(d1.getId(), 0);
		  logger.info("onClose:" + conn.getRemoteSocketAddress());
	}

	@Override
	public void onMessage(org.java_websocket.WebSocket conn, String message) {
		System.out.println("上传人员信息-----------------"+message);
	        ObjectMapper objectMapper = new ObjectMapper();
	        String ret;
	       
	            try {
	            	//Thread.sleep(7000);
	    	        String msg = message.replaceAll(",]", "]");
	    	        
					JsonNode jsonNode = (JsonNode)objectMapper.readValue(msg, JsonNode.class);
				//	System.out.println("数据"+jsonNode);
					if (jsonNode.has("cmd")) {
						ret=jsonNode.get("cmd").asText();
						if ("reg".equals(ret)) {
							System.out.println("设备信息"+jsonNode);
							try {
								
							this.getDeviceInfo(jsonNode, conn);
								
								
							} catch (Exception e) {
								// TODO Auto-generated catch block
								conn.send("{\"ret\":\"reg\",\"result\":false,\"reason\":1}");
								e.printStackTrace();
							}
						}else if("sendlog".equals(ret)){
							SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
							try {
								this.getAttandence(jsonNode,conn);
								
							} catch (Exception e) {
								// TODO Auto-generated catch block
								conn.send("{\"ret\":\"sendlog\",\"result\":false,\"reason\":1}");
								e.printStackTrace();
							}
						}else if("sendqrcode".equals(ret)){
							SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
							try {
								//this.getAttandence(jsonNode,conn);
								conn.send("{\"ret\":\"sendqrcode\",\"result\":true,\"access\":1,\"enrollid\":10,\"username\":\"test\"}");								
								System.out.println("QR-----"+"{\"ret\":\"sendqrcode\",\"result\":true,\"access\":1,\"enrollid\":10,\"username\":\"test\"}");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								conn.send("{\"ret\":\"sendqrcode\",\"result\":false,\"reason\":1}");
								e.printStackTrace();
							}
						}else if("senduser".endsWith(ret)){
							System.out.println(jsonNode);
						
							
							try {
								this.getEnrollInfo(jsonNode,conn);
							
							} catch (Exception e) {
								// TODO Auto-generated catch block
								conn.send("{\"ret\":\"senduser\",\"result\":false,\"reason\":1}");
								e.printStackTrace();
							}
						}
						
					}else if(jsonNode.has("ret")){
						ret=jsonNode.get("ret").asText();
						//boolean result;
						if("getuserlist".equals(ret)){
						//	System.out.println(jsonNode);
						//	System.out.println("数据"+message);
							this.getUserList(jsonNode,conn);
							
						}else if("getuserinfo".equals(ret)){
							//System.out.println("json数据"+jsonNode);
							this.getUserInfo(jsonNode,conn);
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);							
						}else if("setuserinfo".equals(ret)){
							Boolean result=jsonNode.get("result").asBoolean();
							//WebSocketPool.setUserResult(result);
							//setUserResult=result;
						//	System.out.println();
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);
						    updateCommandStatus(sn, "setuserinfo");
							System.out.println("下发数据"+jsonNode);
						}else if("getalllog".equals(ret)){
							
							System.out.println("获取所有打卡记录"+jsonNode);
							try {
								this.getAllLog(jsonNode,conn);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								
							}
							
						}else if("getnewlog".equals(ret)){
							
							System.out.println("获取所有打卡记录"+jsonNode);
							try {
								this.getnewLog(jsonNode,conn);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								
							}
							
						}
						else if("deleteuser".equals(ret)){
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);
							System.out.println("删除人员"+jsonNode);
							updateCommandStatus(sn, "deleteuser");
						}else if("initsys".equals(ret)){
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);
							System.out.println("初始化系统"+jsonNode);
							updateCommandStatus(sn, "initsys");
						}else if("setdevlock".equals(ret)){
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);
							System.out.println("设置天时间段"+jsonNode);
							updateCommandStatus(sn, "setdevlock");
						}else if("setuserlock".equals(ret)){
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);
							System.out.println("门禁授权"+jsonNode);
							updateCommandStatus(sn, "setuserlock");
						}else if("getdevinfo".equals(ret)){
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);
							System.out.println(new Date()+"设备信息"+jsonNode);
							updateCommandStatus(sn, "getdevinfo");
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							
							JSONObject jObj=new JSONObject();
							jObj.put("SN", sn);
							jObj.put("currentTime", System.currentTimeMillis());
						//	RestTemplateUtil.postDeviceInfo(jObj);
						}else if("setusername".equals(ret)){
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);
							System.out.println(new Date()+"下发姓名"+jsonNode);
							updateCommandStatus(sn, "setusername");
						}else if("reboot".equals(ret)){
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);
						    updateCommandStatus(sn, "reboot");
						}else if("getdevlock".equals(ret)) {
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);
						    updateCommandStatus(sn, "getdevlock");
						}else if("getuserlock".equals(ret)) {
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);
						    updateCommandStatus(sn, "getuserlock");
						}else {
							String sn=jsonNode.get("sn").asText();
							DeviceStatus deviceStatus=new DeviceStatus();
							deviceStatus.setWebSocket(conn);
						    deviceStatus.setDeviceSn(sn);
						    deviceStatus.setStatus(1);
						    updateDevice(sn, deviceStatus);
						    updateCommandStatus(sn, ret);
						}
				
					}
			        
					//Thread.sleep(40000);
					//conn.close();
					
			/* if(System.currentTimeMillis()) */
					
				}  catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
	}

	


	

	/*websocket链接发生错误的时候*/
	@Override
	public void onError(org.java_websocket.WebSocket conn, Exception ex) {
		// TODO Auto-generated method stub
		ex.printStackTrace();
		if(conn!=null){
			conn.close();
			WebSocketPool.removeDeviceByWebsocket(conn);
		}
	//	System.out.println("socket断开了");
		System.out.println("socket连接断开了"+conn);
	}

	public void onStart() {
		
		// TODO Auto-generated method stub
		
	}
	
	 
	
	public void updateCommandStatus(String serial,String commandType) {
		List<MachineCommand>machineCommand=machineCommandMapper.findPendingCommand(1, serial);
		if(machineCommand.size()>0&&machineCommand.get(0).getName().equals(commandType)) {
			
			machineCommandMapper.updateCommandStatus(1, 0,new Date(), machineCommand.get(0).getId());
			
		}
	}
	
	
	
	   //获得连接设备信息
	public void getDeviceInfo(JsonNode jsonNode,org.java_websocket.WebSocket args1){
		String sn=jsonNode.get("sn").asText();
		System.out.println("序列号"+sn);
		DeviceStatus deviceStatus=new DeviceStatus();
		if(sn!=null){
		
			Device d1=deviceService.selectDeviceBySerialNum(sn);
			
			if(d1==null){
				int i=	deviceService.insert(sn, 1);
				System.out.println(i);
			}else{
				//deviceService.updateByPrimaryKey()
				deviceService.updateStatusByPrimaryKey(d1.getId(), 1);
			}
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			 //long l=System.currentTimeMillis();
	      
		    args1.send("{\"ret\":\"reg\",\"result\":true,\"cloudtime\":\"" + sdf.format(new Date()) + "\"}");	
		   // ",\"cloudtime\":\""
		  //  args1.send("{\"ret\":\"reg\",\"result\":true,\"cloudtime\":\"" + sdf.format(new Date())+"\",\"nosenduser\":" +true+ "}");
		 //   System.out.println("{\"ret\":\"reg\",\"result\":true,\"cloudtime\":\"" + sdf.format(new Date())+"\",\"nosenduser\":" +true+ "}");
		    deviceStatus.setWebSocket(args1);
		    deviceStatus.setStatus(1);
		    deviceStatus.setDeviceSn(sn);
		    updateDevice(sn, deviceStatus);
		    System.out.println(WebSocketPool.getDeviceStatus(sn));
		    JSONObject jObj=new JSONObject();
			jObj.put("SN", sn);
			jObj.put("currentTime", System.currentTimeMillis());
		//    RestTemplateUtil.postDeviceInfo(jObj);
	        
		}else{
			 args1.send("{\"ret\":\"reg\",\"result\":false,\"reason\":1}");
			    deviceStatus.setWebSocket(args1);
			    deviceStatus.setDeviceSn(sn);
			    deviceStatus.setStatus(1);
			    updateDevice(sn, deviceStatus);
		}
		
	  	timeStamp=System.currentTimeMillis();
	  	timeStamp2=timeStamp;
	}
    
	 public void updateDevice(String sn,DeviceStatus deviceStatus) {
		if(WebSocketPool.getDeviceStatus(sn)!=null){
			//WebSocketPool.removeDeviceStatus(sn);
			WebSocketPool.addDeviceAndStatus(sn, deviceStatus);
		}else{
			WebSocketPool.addDeviceAndStatus(sn, deviceStatus);
		}
	}
	

   //获得打卡记录，包括机器号
	private void getAttandence(JsonNode jsonNode,
			org.java_websocket.WebSocket conn) {
		// TODO Auto-generated method stub
		//System.out.println("打卡记录-----------"+jsonNode);
		String sn=jsonNode.get("sn").asText();
		int count=jsonNode.get("count").asInt();
		//int logindex=jsonNode.get("logindex").asInt();
		int logindex=-1;
		if(jsonNode.get("logindex")!=null) {
			logindex=jsonNode.get("logindex").asInt();
		}
	//	int logindex=jsonNode.get("logindex").asInt();
		List<Records>recordAll=new ArrayList<Records>();
		//System.out.println(jsonNode);
		JsonNode records=jsonNode.get("record");
		DeviceStatus deviceStatus=new DeviceStatus();
		Boolean flag=false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		if (count>0) {
			Iterator iterator=records.elements();
			while(iterator.hasNext()){
				JsonNode type=(JsonNode)iterator.next();
				JSONObject obj=new JSONObject();
				Long enrollId=type.get("enrollid").asLong();
				String timeStr=type.get("time").asText();
				int mode=type.get("mode").asInt();
				int inOut=type.get("inout").asInt();
				int event=type.get("event").asInt();
				Double temperature=(double) 0;
				if(type.get("temp")!=null) {
					 temperature=type.get("temp").asDouble();
					 temperature=temperature/10;
					 temperature=(double) Math.round(temperature * 10) / 10;
					System.out.println("温度值"+temperature);
					 obj.put("temperature", String.valueOf(temperature));
				}
				Records record=new Records();
				record.setDeviceSerialNum(sn);
				record.setEnrollId(enrollId);
				record.setEvent(event);
				record.setIntout(inOut);
				record.setMode(mode);
				record.setRecordsTime(timeStr);
				record.setTemperature(temperature);
				if (enrollId==99999999) {
					 obj.put("resultStatus", 0);
				}else {
					 obj.put("resultStatus", 1);
				}
			    obj.put("IdentifyType", "0");
			    obj.put("Mac_addr", "");
			    obj.put("SN", sn);
			    obj.put("address", "");
			    obj.put("birthday", "");
			    obj.put("depart", "");
			    obj.put("devicename", "");
			    obj.put("employee_number", "");
			    
				if (type.get("image")!=null) {
					String picName=UUID.randomUUID().toString();
					obj.put("face_base64", type.get("image").asText());
				  	flag=ImageProcess.base64toImage(type.get("image").asText(), picName);
				  	if(flag) {
				  	record.setImage(picName+".jpg");
				  	}
				}
				recordAll.add(record);	
				  obj.put("icNum", "");
				  obj.put("id",sn);
				  obj.put("idNum", "");
				  obj.put("idissue", "");
				  obj.put("inout", inOut);
				  obj.put("location", "");
				  obj.put("name", "");
				  obj.put("nation", "");
				  
				 
				  obj.put("sex", "");
				  obj.put("telephone", "");
				  obj.put("templatePhoto", "");
				  obj.put("time", timeStr);
				  obj.put("userid",String.valueOf(enrollId));
				  obj.put("validEnd", "");
				  obj.put("validStart", "");
				  
				  //  RestTemplateUtil.postLog(obj);
			}
			
			if (logindex>=0) {
				conn.send("{\"ret\":\"sendlog\",\"result\":true"+",\"count\":"+count+",\"logindex\":"+logindex+",\"cloudtime\":\""
						+ sdf.format(new Date()) + "\"}");
			}else if(logindex<0){
				conn.send("{\"ret\":\"sendlog\",\"result\":true"+",\"cloudtime\":\""
						+ sdf.format(new Date()) + "\"}");
			}
			/*
			 * conn.send("{\"ret\":\"sendlog\",\"result\":true"+",\"count\":"+count+
			 * ",\"logindex\":"+logindex+",\"cloudtime\":\"" + sdf.format(new Date()) +
			 * "\"}");
			 */
			deviceStatus.setWebSocket(conn);
		    deviceStatus.setStatus(1);
		    deviceStatus.setDeviceSn(sn);
		    updateDevice(sn, deviceStatus);
		    
		  
		}else if(count==0){
			conn.send("{\"ret\":\"\"sendlog\"\",\"result\":false,\"reason\":1}");
			deviceStatus.setWebSocket(conn);
		    deviceStatus.setStatus(1);
		    deviceStatus.setDeviceSn(sn);
		    updateDevice(sn, deviceStatus);
		}
	    
	    System.out.println(recordAll);
		for (int i = 0; i < recordAll.size(); i++) {
			Records recordsTemp=recordAll.get(i);	
			recordsService.insert(recordsTemp);
		}
		
		timeStamp2=System.currentTimeMillis();
		
	}

	//获取机器推送注册信息
	private void getEnrollInfo(JsonNode jsonNode,
			org.java_websocket.WebSocket conn) {
		// TODO Auto-generated method stub
		//System.out.println("连接数据类型"+(conn.getData()).getClass());
		//int enrollId=jsonNode.get("enrollid").asInt();
	//	System.out.println("json"+json);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sn=jsonNode.get("sn").asText();
		String signatures1=jsonNode.get("record").asText();
		Boolean flag=false;
		DeviceStatus deviceStatus=new DeviceStatus();
		 if (signatures1==null) {
			 conn.send("{\"ret\":\"senduser\",\"result\":false,\"reason\":1}");
			 deviceStatus.setWebSocket(conn);
			    deviceStatus.setStatus(1);
			    deviceStatus.setDeviceSn(sn);
			    updateDevice(sn, deviceStatus);
		}else{
			int backupnum=jsonNode.get("backupnum").asInt();
		//	if(backupnum!=10&&backupnum!=11){
			    Long enrollId=jsonNode.get("enrollid").asLong();
				String name=jsonNode.get("name").asText();
				int rollId=jsonNode.get("admin").asInt();
				String signatures=jsonNode.get("record").asText();
				Person person=new Person();
				person.setId(enrollId);
				person.setName(name);
				person.setRollId(rollId);
			//	System.out.println("员工号"+enrollId);
				personService.selectByPrimaryKey(enrollId);
				//System.out.println("人员信息"+personService.selectByPrimaryKey(enrollId));
				System.out.println("获取人员数据"+personService.selectByPrimaryKey(enrollId));
				if (personService.selectByPrimaryKey(enrollId)==null) {
					personService.insert(person);
				}
				EnrollInfo enrollInfo=new EnrollInfo();
				enrollInfo.setEnrollId(enrollId);
				enrollInfo.setBackupnum(backupnum);
				enrollInfo.setSignatures(signatures);
				if (backupnum==50) {
     				
     				String picName=UUID.randomUUID().toString();
				  	flag=ImageProcess.base64toImage(jsonNode.get("record").asText(), picName);
				  	enrollInfo.setImagePath(picName+".jpg");
					}
                if (enrollInfoService.selectByBackupnum(enrollId, backupnum)==null) {
					/*
					 * if (backupnum==50) {
					 * 
					 * String picName=UUID.randomUUID().toString();
					 * flag=ImageProcess.base64toImage(jsonNode.get("record").asText(), picName);
					 * enrollInfo.setImagePath(picName+".jpg"); }
					 */
                	enrollInfoService.insertSelective(enrollInfo);
				}
				
				conn.send("{\"ret\":\"senduser\",\"result\":true,\"cloudtime\":\"" + sdf.format(new Date()) + "\"}");
				deviceStatus.setWebSocket(conn);
			    deviceStatus.setStatus(1);
			    deviceStatus.setDeviceSn(sn);
			    updateDevice(sn, deviceStatus);
			/*}else{
				System.out.println("卡号或者密码的情况"+jsonNode);
				
				 conn.send("{\"ret\":\"senduser\",\"result\":true,\"cloudtime\":\"" + sdf.format(new Date()) + "\"}");
				 deviceStatus.setWebSocket(conn);
				    deviceStatus.setStatus(1);
				    deviceStatus.setDeviceSn(sn);
				    updateDevice(sn, deviceStatus);
				}*/
		}
		 timeStamp2=System.currentTimeMillis();
	}

       //获取用户列表，服务器主动发出请求
     	private void getUserList(JsonNode jsonNode,
			org.java_websocket.WebSocket conn) {
		    List<UserTemp>userTemps=new ArrayList<UserTemp>();
     		boolean result=jsonNode.get("result").asBoolean();
     		
     		int count;
     		JsonNode records=jsonNode.get("record");
     	//	System.out.println("用户列表"+records);
     		String sn=jsonNode.get("sn").asText();
     		DeviceStatus deviceStatus=new DeviceStatus();
     		if(result){
     			count=jsonNode.get("count").asInt();
     			//System.out.println("用户数："+count);
     			if (count>0) {
     				 Iterator iterator=records.elements();
     	     		  while (iterator.hasNext()) {
     	     			JsonNode type=(JsonNode)iterator.next();
     	     			Long enrollId=type.get("enrollid").asLong();
     					//int enrollId=Integer.valueOf(enrollId1);
     					int admin=type.get("admin").asInt();
     					int backupnum=type.get("backupnum").asInt();
     					UserTemp userTemp=new UserTemp();
     					userTemp.setEnrollId(enrollId);
     					userTemp.setBackupnum(backupnum);
     					userTemp.setAdmin(admin);
     					userTemps.add(userTemp);
     					
     					
     				}
     	     		conn.send("{\"cmd\":\"getuserlist\",\"stn\":false}");
     	     		//DeviceStatus deviceStatus=new DeviceStatus();
					deviceStatus.setWebSocket(conn);
				    deviceStatus.setStatus(1);
				    deviceStatus.setDeviceSn(sn);
				    updateDevice(sn, deviceStatus); 
				   
				}
     		 
     			 
     		}
     		for (int i = 0; i < userTemps.size(); i++) {
				UserTemp uTemp=new UserTemp();
				uTemp=userTemps.get(i);
				if(personService.selectByPrimaryKey(uTemp.getEnrollId())==null){
					Person personTemp=new Person();
					personTemp.setId(uTemp.getEnrollId());
					personTemp.setName("");
					personTemp.setRollId(uTemp.getAdmin());
					personService.insert(personTemp);
					
					
				}
			}
     		
     		for (int i = 0; i < userTemps.size(); i++) {
				UserTemp uTemp1=new UserTemp();
				uTemp1=userTemps.get(i);
				if(enrollInfoService.selectByBackupnum(uTemp1.getEnrollId(), uTemp1.getBackupnum())==null){
					EnrollInfo enrollInfo=new EnrollInfo();
					enrollInfo.setEnrollId(uTemp1.getEnrollId());
					enrollInfo.setBackupnum(uTemp1.getBackupnum());
				 	enrollInfoService.insert(enrollInfo.getEnrollId(), enrollInfo.getBackupnum(), enrollInfo.getImagePath(), enrollInfo.getSignatures());
				//en
				}
			}
     		 updateCommandStatus(sn, "getuserlist");
     		
	     }


//     	获得用户详细信息
     	private void getUserInfo(JsonNode jsonNode,
    			org.java_websocket.WebSocket conn) {
    		// TODO Auto-generated method stub
     		System.out.println(jsonNode);
     		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     	//	System.out.println("用户具体信息"+jsonNode);
     		Boolean result=jsonNode.get("result").asBoolean();
     		
     		String sn=jsonNode.get("sn").asText();
     	//	System.out.println("sn数据"+jsonNode);
     	
     		System.out.println(jsonNode);
     	//	DeviceStatus deviceStatus=new DeviceStatus();
     		Boolean flag=false;
     		if(result){
     			int backupnum=jsonNode.get("backupnum").asInt();
     			String signatures1=jsonNode.get("record").asText();
     		//	if(backupnum!=10&&backupnum!=11){
     			Long enrollId=jsonNode.get("enrollid").asLong();
     				String name=jsonNode.get("name").asText();
     				int admin=jsonNode.get("admin").asInt();
     				String signatures=jsonNode.get("record").asText();
     				
     				Person person=new Person();
     				person.setId(enrollId);
     				person.setName(name);
     				person.setRollId(admin);
     				EnrollInfo enrollInfo=enrollInfoService.selectByBackupnum(enrollId, backupnum);
     				if (backupnum==50) {
         				
         				String picName=UUID.randomUUID().toString();
    				  	flag=ImageProcess.base64toImage(jsonNode.get("record").asText(), picName);
    				  	enrollInfo.setImagePath(picName+".jpg");
    					}
     				if (personService.selectByPrimaryKey(enrollId)==null) {
    					personService.insert(person);
    				}else if(personService.selectByPrimaryKey(enrollId)!=null){
    					personService.updateByPrimaryKey(person);
    				}
                    if (enrollInfo==null) {
                 //   	enrollInfoService.insert(enrollId, backupnum, signatures);
                    	enrollInfoService.insert(enrollInfo.getEnrollId(), enrollInfo.getBackupnum(), enrollInfo.getImagePath(), enrollInfo.getSignatures());
    				}else if(enrollInfo!=null){
    				//	enrollInfoService.updateByEnrollIdAndBackupNum(signatures, enrollId, backupnum);
    					enrollInfo.setSignatures(signatures);
    					enrollInfoService.updateByPrimaryKeyWithBLOBs(enrollInfo);
    				}
            
                    
     			}
     	//	}
     		  updateCommandStatus(sn, "getuserinfo");
    	
    	}
     	
     	//获取全部打卡记录
     	private void getAllLog(JsonNode jsonNode, WebSocket conn) {
    	
     		Boolean result=jsonNode.get("result").asBoolean();
     		List<Records>recordAll=new ArrayList<Records>();
    	//	System.out.println("记录"+jsonNode);
     		String sn=jsonNode.get("sn").asText();
    		JsonNode records=jsonNode.get("record");
    		DeviceStatus deviceStatus=new DeviceStatus();
     		int count;
     		boolean flag=false;
     		if (result) {
				count=jsonNode.get("count").asInt();
				if(count>0){
					Iterator iterator=records.elements();
					while(iterator.hasNext()){
						JsonNode type=(JsonNode)iterator.next();
						Long enrollId=type.get("enrollid").asLong();
						String timeStr=type.get("time").asText();
						int mode=type.get("mode").asInt();
						int inOut=type.get("inout").asInt();
						int event=type.get("event").asInt();
						Double temperature=(double) 0;
						if(type.get("temp")!=null) {
							 temperature=type.get("temp").asDouble();
							 temperature=temperature/100;
							 temperature=(double) Math.round(temperature * 10) / 10;
							System.out.println("温度值"+temperature);
						}
						Records record=new Records();
					//	record.setDeviceSerialNum(sn);
						record.setEnrollId(enrollId);
						record.setEvent(event);
						record.setIntout(inOut);
						record.setMode(mode);
						record.setRecordsTime(timeStr);
						record.setDeviceSerialNum(sn);
						record.setTemperature(temperature);
					
						recordAll.add(record);	      
					}
					  conn.send("{\"cmd\":\"getalllog\",\"stn\":false}");
					  deviceStatus.setWebSocket(conn);
	 				  deviceStatus.setStatus(1);
	 				    deviceStatus.setDeviceSn(sn);
	 			      updateDevice(sn, deviceStatus);
				}
			}
     	//	 System.out.println(recordAll);
     		for (int i = 0; i < recordAll.size(); i++) {
     			Records recordsTemp=recordAll.get(i);	
     			recordsService.insert(recordsTemp);
     		}
     		 updateCommandStatus(sn, "getalllog");
    		
    	}
     	
     	//获取全部打卡记录
     	private void getnewLog(JsonNode jsonNode, WebSocket conn) {
    	
     		Boolean result=jsonNode.get("result").asBoolean();
     		List<Records>recordAll=new ArrayList<Records>();
    		System.out.println("记录"+jsonNode);
     		String sn=jsonNode.get("sn").asText();
    		JsonNode records=jsonNode.get("record");
    		DeviceStatus deviceStatus=new DeviceStatus();
    		boolean flag=false;
     		int count;
     		if (result) {
				count=jsonNode.get("count").asInt();
				if(count>0){
					Iterator iterator=records.elements();
					while(iterator.hasNext()){
						JsonNode type=(JsonNode)iterator.next();
						Long enrollId=type.get("enrollid").asLong();
						String timeStr=type.get("time").asText();
						int mode=type.get("mode").asInt();
						int inOut=type.get("inout").asInt();
						int event=type.get("event").asInt();
						Double temperature=(double) 0;
						if(type.get("temp")!=null) {
							 temperature=type.get("temp").asDouble();
							 temperature=temperature/100;
							 temperature=(double) Math.round(temperature * 10) / 10;
							System.out.println("温度值"+temperature);
						}
						Records record=new Records();
					//	record.setDeviceSerialNum(sn);
						record.setEnrollId(enrollId);
						record.setEvent(event);
						record.setIntout(inOut);
						record.setMode(mode);
						record.setRecordsTime(timeStr);
						record.setDeviceSerialNum(sn);
						record.setTemperature(temperature);
						recordAll.add(record);	      
					}
					  conn.send("{\"cmd\":\"getnewlog\",\"stn\":false}");
					  deviceStatus.setWebSocket(conn);
	 				  deviceStatus.setStatus(1);
	 				    deviceStatus.setDeviceSn(sn);
	 			      updateDevice(sn, deviceStatus);
				}
			}
     	//	 System.out.println(recordAll);
     		for (int i = 0; i < recordAll.size(); i++) {
     			Records recordsTemp=recordAll.get(i);	
     			recordsService.insert(recordsTemp);
     		}
     		updateCommandStatus(sn, "getnewlog");
    		
    	}
}

