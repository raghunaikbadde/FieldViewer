package com.jobviewer.db.objects;

import com.jobviwer.request.object.TimeSheetRequest;

public class TimeSheet {
	private TimeSheetRequest timeSheetRequest;
	private String api_url;

	public String getApi_url() {
		return api_url;
	}

	public void setApi_url(String api_url) {
		this.api_url = api_url;
	}

	public TimeSheetRequest getTimeSheetRequest() {
		return timeSheetRequest;
	}

	public void setTimeSheetRequest(TimeSheetRequest timeSheetRequest) {
		this.timeSheetRequest = timeSheetRequest;
	}
}
