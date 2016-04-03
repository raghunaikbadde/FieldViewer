package com.jobviewer.db.objects;

import java.util.Date;

public class BreakShiftTravelCall {
	private boolean isBreakStarted;
	private String breakStartedTime;		
	private String breakEndTime;
	
	private boolean isShiftStarted;
	private String shiftStartTime;
	private String shiftEndTime;
	
	private boolean isCallStarted;
	private String callStartTime;
	private String callEndTime;
	
	private int noOfBreaks;
	
	private boolean isTravelStarted;
	private String travelStartedTime;		
	private String travelEndTime;

	public boolean isBreakStarted() {
		return isBreakStarted;
	}
	public void setBreakStarted(boolean isBreakStarted) {
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
	public boolean isShiftStarted() {
		return isShiftStarted;
	}
	public void setShiftStarted(boolean isShiftStarted) {
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
	public boolean isCallStarted() {
		return isCallStarted;
	}
	public void setCallStarted(boolean isCallStarted) {
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
	public boolean isTravelStarted() {
		return isTravelStarted;
	}
	public void setTravelStarted(boolean isTravelStarted) {
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
