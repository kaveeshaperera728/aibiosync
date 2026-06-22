package com.timmy.entity;

public class AccessDay {
    private Integer id;

    private String serial;

    private String name;

    private String startTime1;

    private String endTime1;

    private String startTime2;

    private String endTime2;

    private String startTime3;

    private String endTime3;

    private String startTime4;

    private String endTime4;

    private String startTime5;

    private String endTime5;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial == null ? null : serial.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getStartTime1() {
        return startTime1;
    }

    public void setStartTime1(String startTime1) {
        this.startTime1 = startTime1 == null ? null : startTime1.trim();
    }

    public String getEndTime1() {
        return endTime1;
    }

    public void setEndTime1(String endTime1) {
        this.endTime1 = endTime1 == null ? null : endTime1.trim();
    }

    public String getStartTime2() {
        return startTime2;
    }

    public void setStartTime2(String startTime2) {
        this.startTime2 = startTime2 == null ? null : startTime2.trim();
    }

    public String getEndTime2() {
        return endTime2;
    }

    public void setEndTime2(String endTime2) {
        this.endTime2 = endTime2 == null ? null : endTime2.trim();
    }

    public String getStartTime3() {
        return startTime3;
    }

    public void setStartTime3(String startTime3) {
        this.startTime3 = startTime3 == null ? null : startTime3.trim();
    }

    public String getEndTime3() {
        return endTime3;
    }

    public void setEndTime3(String endTime3) {
        this.endTime3 = endTime3 == null ? null : endTime3.trim();
    }

    public String getStartTime4() {
        return startTime4;
    }

    public void setStartTime4(String startTime4) {
        this.startTime4 = startTime4 == null ? null : startTime4.trim();
    }

    public String getEndTime4() {
        return endTime4;
    }

    public void setEndTime4(String endTime4) {
        this.endTime4 = endTime4 == null ? null : endTime4.trim();
    }

    public String getStartTime5() {
        return startTime5;
    }

    public void setStartTime5(String startTime5) {
        this.startTime5 = startTime5 == null ? null : startTime5.trim();
    }

    public String getEndTime5() {
        return endTime5;
    }

    public void setEndTime5(String endTime5) {
        this.endTime5 = endTime5 == null ? null : endTime5.trim();
    }

	@Override
	public String toString() {
		return "AccessDay [id=" + id + ", serial=" + serial + ", name=" + name
				+ ", startTime1=" + startTime1 + ", endTime1=" + endTime1
				+ ", startTime2=" + startTime2 + ", endTime2=" + endTime2
				+ ", startTime3=" + startTime3 + ", endTime3=" + endTime3
				+ ", startTime4=" + startTime4 + ", endTime4=" + endTime4
				+ ", startTime5=" + startTime5 + ", endTime5=" + endTime5 + "]";
	}
    
    
}