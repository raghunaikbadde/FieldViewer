package com.jobviewer.db.objects;

public class SurveyJson {
	private String workType;
	private String questionJson;
	private String backStack;

	public String getWorkType() {
		return workType;
	}

	public void setWorkType(String workType) {
		this.workType = workType;
	}

	public String getQuestionJson() {
		return questionJson;
	}

	public void setQuestionJson(String questionJson) {
		this.questionJson = questionJson;
	}

	public String getBackStack() {
		return backStack;
	}

	public void setBackStack(String backStack) {
		this.backStack = backStack;
	}

}
