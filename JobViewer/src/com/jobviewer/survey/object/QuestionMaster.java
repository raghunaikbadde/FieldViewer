package com.jobviewer.survey.object;

public class QuestionMaster {
	private Screens screens;

	private String type;

	public Screens getScreens() {
		return screens;
	}

	public void setScreens(Screens screens) {
		this.screens = screens;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ClassPojo [screens = " + screens + ", type = " + type + "]";
	}
}
