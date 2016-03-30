package com.jobviewer.db.objects;

public class ShoutAboutSafetyObject {
	private String optionSelected = "";
	private String questionSet = "";
	private String StartedAt="";

	public String getOptionSelected() {
		return optionSelected;
	}

	public void setOptionSelected(String optionSelected) {
		this.optionSelected = optionSelected;
	}

	public String getQuestionSet() {
		return questionSet;
	}

	public void setQuestionSet(String questionSet) {
		this.questionSet = questionSet;
	}

	public String getStartedAt() {
		return StartedAt;
	}

	public void setStartedAt(String startedAt) {
		StartedAt = startedAt;
	}
}
