package com.jobviewer.survey.object;

public class Option {
	private String label;

	private String display;

	private Actions actions;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public Actions getActions() {
		return actions;
	}

	public void setActions(Actions actions) {
		this.actions = actions;
	}

	@Override
	public String toString() {
		return "ClassPojo [label = " + label + ", display = " + display
				+ ", actions = " + actions + "]";
	}
}
