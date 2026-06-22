package com.timmy.entity;

public class UserLock {

	private int enrollId;
	private int weekZone;
	private int group;
	
	private String starttime;
	private String endtime;
	public int getEnrollId() {
		return enrollId;
	}
	public void setEnrollId(int enrollId) {
		this.enrollId = enrollId;
	}
	public int getWeekZone() {
		return weekZone;
	}
	public void setWeekZone(int weekZone) {
		this.weekZone = weekZone;
	}
	public int getGroup() {
		return group;
	}
	public void setGroup(int group) {
		this.group = group;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	
	
}
