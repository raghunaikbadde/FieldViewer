package com.jobviewer.db.objects;

public class StartTrainingObject {
	private String isTrainingStarted = "";
	private String trainingStartTime = "";
	private String trainingEndTime="";

	public String getIsTrainingStarted() {
		return isTrainingStarted;
	}

	public void setIsTrainingStarted(String trainingStarted) {
		this.isTrainingStarted = trainingStarted;
	}

	public String getStartTime() {
		return trainingStartTime;
	}

	public void setStartTime(String startTime) {
		this.trainingStartTime = startTime;
	}

	public String getEndTime() {
		return trainingEndTime;
	}

	public void setEndTime(String EndTime) {
		trainingEndTime = EndTime;
	}
}
