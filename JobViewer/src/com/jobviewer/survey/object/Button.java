package com.jobviewer.survey.object;

public class Button {
	private String name;

	private String display;

	private Actions actions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	@Override
	public String toString() {
		return "ClassPojo [name = " + name + ", display = " + display + "]";
	}

	public Actions getActions() {
		return actions;
	}

	public void setActions(Actions actions) {
		this.actions = actions;
	}
}
