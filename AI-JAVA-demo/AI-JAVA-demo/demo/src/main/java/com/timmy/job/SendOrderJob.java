package com.timmy.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.timmy.entity.Device;
import com.timmy.entity.DeviceStatus;
import com.timmy.entity.MachineCommand;
import com.timmy.mapper.DeviceMapper;
import com.timmy.mapper.MachineCommandMapper;
import com.timmy.service.DeviceService;
import com.timmy.service.MachineCommandService;
import com.timmy.websocket.WebSocketPool;

public class SendOrderJob extends Thread{
	
    @Autowired
    MachineCommandMapper machineCommandMapper;
    
    @Autowired
    DeviceMapper deviceMapper;
	
	//List<Device>deviceList=deviceService.
	Map<String,DeviceStatus>wdList=WebSocketPool.wsDevice;
	
	private boolean stop=true;
	
	public void startThread() {
		this.stop=false;
		this.start();
	}
	
	public void stopThread() {
		this.stop=true;
		this.stop();
	}
	public void run() {
		
		while (!stop) {
			Iterator<Entry<String, DeviceStatus>>entries=wdList.entrySet().iterator();		
			try {
				
			
			while(entries.hasNext()){		
				Entry<String,DeviceStatus>entry=entries.next();
			//	System.out.println("数据"+entry);
				List<MachineCommand>inSending=machineCommandMapper.findPendingCommand(0, entry.getKey());
				//System.out.println("数据"+inSending);
			    if(inSending.size()>0) {
			    	List<MachineCommand>pendingCommand=machineCommandMapper.findPendingCommand(1, entry.getKey());
			    	if (pendingCommand.size()<=0) {
			    		
			    		entry.getValue().getWebSocket().send(inSending.get(0).getContent());
			    		machineCommandMapper.updateCommandStatus(0, 1,new Date(), inSending.get(0).getId());
					}else if(pendingCommand.size()==1){
						if(System.currentTimeMillis()-(pendingCommand.get(0).getRunTime()).getTime()>20*1000) {
						//	System.out.println("数据"+pendingCommand);
							if(pendingCommand.get(0).getErrCount()<3) {
								System.out.println("数据"+pendingCommand);
							//entry.getValue().getWebSocket().send(pendingCommand.get(0).getContent());
							MachineCommand machineCommand=pendingCommand.get(0);
							machineCommand.setErrCount(machineCommand.getErrCount()+1);
							machineCommand.setRunTime(new Date());
							machineCommandMapper.updateByPrimaryKey(machineCommand);
					    	Device device=deviceMapper.selectDeviceBySerialNum(pendingCommand.get(0).getSerial());
							if (device.getStatus()!=0) {
								entry.getValue().getWebSocket().send(pendingCommand.get(0).getContent());
								
							}
							}else {
								MachineCommand machineCommand=pendingCommand.get(0);
								machineCommand.setErrCount(machineCommand.getErrCount()+1);
								machineCommandMapper.updateByPrimaryKey(machineCommand);
							}
						}
					}
			    				    	
			    }else {
			    	List<MachineCommand>pendingCommand=machineCommandMapper.findPendingCommand(1, entry.getKey());
			    	if(pendingCommand.size()!=0){
						if(System.currentTimeMillis()-(pendingCommand.get(0).getRunTime()).getTime()>20*1000) {
						//	System.out.println("数据"+pendingCommand);
							if(pendingCommand.get(0).getErrCount()<3) {
								System.out.println("数据"+pendingCommand);
							//entry.getValue().getWebSocket().send(pendingCommand.get(0).getContent());
							MachineCommand machineCommand=pendingCommand.get(0);
							machineCommand.setErrCount(machineCommand.getErrCount()+1);
							machineCommand.setRunTime(new Date());
							machineCommandMapper.updateByPrimaryKey(machineCommand);
					    	Device device=deviceMapper.selectDeviceBySerialNum(pendingCommand.get(0).getSerial());
							if (device.getStatus()!=0) {
								entry.getValue().getWebSocket().send(pendingCommand.get(0).getContent());
								
							}
							}else {
								MachineCommand machineCommand=pendingCommand.get(0);
								machineCommand.setErrCount(machineCommand.getErrCount()+1);
								machineCommandMapper.updateByPrimaryKey(machineCommand);
							}
						}
			    }
				
			}
			}
			} catch (Exception e) {
				
			}
		}
		
	}

}
