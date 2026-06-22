package com.timmy.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.timmy.entity.AccessDay;
import com.timmy.entity.Device;
import com.timmy.entity.MachineCommand;
import com.timmy.mapper.AccessDayMapper;
import com.timmy.mapper.DeviceMapper;
import com.timmy.mapper.MachineCommandMapper;
import com.timmy.service.AccessDayService;
import com.timmy.websocket.WebSocketPool;

@Service
public class AccessDayServiceImpl implements AccessDayService {
	
	@Autowired
	AccessDayMapper accessDayMapper;
	
	@Autowired
	MachineCommandMapper machineCommandMapper;

	@Autowired
	DeviceMapper deviceMapper;
	
	@Override
	public int deleteByPrimaryKey(Integer id) {
		// TODO Auto-generated method stub
		return accessDayMapper.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(AccessDay record) {
		// TODO Auto-generated method stub
		return accessDayMapper.insert(record);
	}

	@Override
	public int insertSelective(AccessDay record) {
		// TODO Auto-generated method stub
		return accessDayMapper.insertSelective(record);
	}

	@Override
	public AccessDay selectByPrimaryKey(Integer id) {
		// TODO Auto-generated method stub
		return accessDayMapper.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(AccessDay record) {
		// TODO Auto-generated method stub
		return accessDayMapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(AccessDay record) {
		// TODO Auto-generated method stub
		return accessDayMapper.updateByPrimaryKey(record);
	}
	@Override
	public List<AccessDay> selectAll() {
		// TODO Auto-generated method stub
		return accessDayMapper.selectAll();
	}
	
	@Override
	public void setAccessDay() {
		// TODO Auto-generated method stub
		StringBuilder sb=new StringBuilder();
		sb.append("{\"cmd\":\"setdevlock\",\"dayzone\":[");
		List<AccessDay>accessDays=accessDayMapper.selectAll();
		List<AccessDay>accessDaysTemp=new ArrayList<AccessDay>();
	
		
         if(accessDays.size()<8){
        	 accessDaysTemp.addAll(accessDays);
        	 for (int i = accessDays.size(); i < 8; i++) {
        		 AccessDay accessDay1=new AccessDay();
        		 accessDay1.setId(i+1);
                
                 accessDay1.setStartTime1("00:00");
                 accessDay1.setEndTime1("00:00");
                 accessDay1.setStartTime2("00:00");
                 accessDay1.setEndTime2("00:00");
                 accessDay1.setStartTime3("00:00");
                 accessDay1.setEndTime3("00:00");
                 accessDay1.setStartTime4("00:00");
                 accessDay1.setEndTime4("00:00");
                 accessDay1.setStartTime5("00:00");
                 accessDay1.setEndTime5("00:00");
				accessDaysTemp.add(i, accessDay1);
				
			}
        	 
         }
		 System.out.println(accessDaysTemp.size());
		 for (int i = 0; i < accessDaysTemp.size(); i++) {
			System.out.println(accessDaysTemp.get(i));
			
		}
		 
		for (int i = 0; i < accessDaysTemp.size(); i++) {
			  sb.append("{\"day\":[");
              sb.append("{\"section\":\"" + accessDaysTemp.get(i).getStartTime1() + "~" + accessDaysTemp.get(i).getEndTime1() + "\"},");
              sb.append("{\"section\":\"" + accessDaysTemp.get(i).getStartTime2() + "~" + accessDaysTemp.get(i).getEndTime2() + "\"},");
              sb.append("{\"section\":\"" + accessDaysTemp.get(i).getStartTime3() + "~" + accessDaysTemp.get(i).getEndTime3() + "\"},");
              sb.append("{\"section\":\"" + accessDaysTemp.get(i).getStartTime4() + "~" + accessDaysTemp.get(i).getEndTime4() + "\"},");
              sb.append("{\"section\":\"" + accessDaysTemp.get(i).getStartTime5() + "~" + accessDaysTemp.get(i).getEndTime5() + "\"}");
              if (i != 7) {
                  sb.append("]},");
              } else {
                  sb.append("]}");
              }              
		}
		   sb.append("]}");
		  
		 // System.out.println("jshshhs"+sb.toString());
		String message=sb.toString();
		//System.out.println(message);
		//WebSocketPool.sendMessageToAllDeviceFree(message);
		List<Device>deviceList=deviceMapper.findAllDevice();
		for (int i = 0; i < deviceList.size(); i++) {
			
			MachineCommand machineCommand=new MachineCommand();
			machineCommand.setContent(message);
			machineCommand.setName("setdevlock");
			machineCommand.setStatus(0);
			machineCommand.setSendStatus(0);
			machineCommand.setErrCount(0);
			machineCommand.setSerial(deviceList.get(i).getSerialNum());
			machineCommand.setGmtCrate(new Date());
			machineCommand.setGmtModified(new Date());
			
		//	machineComandService.addMachineCommand(machineCommand);
			machineCommandMapper.insert(machineCommand);
		}
		
	}
	
}
