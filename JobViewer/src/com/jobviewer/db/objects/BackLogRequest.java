package com.jobviewer.db.objects;

public class BackLogRequest {
	private String requestType;
	private String requestJson;
	private String requestApi;
	private String requestClassName;

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getRequestJson() {
		return requestJson;
	}

	public void setRequestJson(String requestJson) {
		this.requestJson = requestJson;
	}

	public String getRequestApi() {
		return requestApi;
	}

	public void setRequestApi(String requestApi) {
		this.requestApi = requestApi;
	}

	public String getRequestClassName() {
		return requestClassName;
	}

	public void setRequestClassName(String requestClassName) {
		this.requestClassName = requestClassName;
	}
}
