package com.jobviewer.db.objects;

import java.util.Date;

public class BreakShiftTravelCall {
	private String isBreakStarted;
	private String breakStartedTime;		
	private String breakEndTime;
	
	private String isShiftStarted;
	private String shiftStartTime;
	private String shiftEndTime;
	
	private String isCallStarted;
	private String callStartTime;
	private String callEndTime;
	
	private int noOfBreaks;
	
	private String isTravelStarted;
	private String travelStartedTime;		
	private String travelEndTime;

	public String isBreakStarted() {
		return isBreakStarted;
	}
	public void setBreakStarted(String isBreakStarted) {
		this.isBreakStarted = isBreakStarted;
	}
	public String getBreakStartedTime() {
		return breakStartedTime;
	}
	public void setBreakStartedTime(String breakStartedTime) {
		this.breakStartedTime = breakStartedTime;
	}
	public String getBreakEndTime() {
		return breakEndTime;
	}
	public void setBreakEndTime(String breakEndTime) {
		this.breakEndTime = breakEndTime;
	}
	public String isShiftStarted() {
		return isShiftStarted;
	}
	public void setShiftStarted(String isShiftStarted) {
		this.isShiftStarted = isShiftStarted;
	}
	public String getShiftStartTime() {
		return shiftStartTime;
	}
	public void setShiftStartTime(String shiftStartTime) {
		this.shiftStartTime = shiftStartTime;
	}
	public String getShiftEndTime() {
		return shiftEndTime;
	}
	public void setShiftEndTime(String shiftEndTime) {
		this.shiftEndTime = shiftEndTime;
	}
	public String isCallStarted() {
		return isCallStarted;
	}
	public void setCallStarted(String isCallStarted) {
		this.isCallStarted = isCallStarted;
	}
	public String getCallStartTime() {
		return callStartTime;
	}
	public void setCallStartTime(String callStartTime) {
		this.callStartTime = callStartTime;
	}
	public String getCallEndTime() {
		return callEndTime;
	}
	public void setCallEndTime(String callEndTime) {
		this.callEndTime = callEndTime;
	}
	public int getNoOfBreaks() {
		return noOfBreaks;
	}
	public void setNoOfBreaks(int noOfBreaks) {
		this.noOfBreaks = noOfBreaks;
	}
	public String isTravelStarted() {
		return isTravelStarted;
	}
	public void setTravelStarted(String isTravelStarted) {
		this.isTravelStarted = isTravelStarted;
	}
	public String getTravelStartedTime() {
		return travelStartedTime;
	}
	public void setTravelStartedTime(String travelStartedTime) {
		this.travelStartedTime = travelStartedTime;
	}
	public String getTravelEndTime() {
		return travelEndTime;
	}
	public void setTravelEndTime(String travelEndTime) {
		this.travelEndTime = travelEndTime;
	}
}
