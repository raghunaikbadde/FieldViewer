package com.jobviewer.survey.object;

public class Checkbox {
	private String label;

	private String required;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getRequired() {
		return required;
	}

	public void setRequired(String required) {
		this.required = required;
	}

	@Override
	public String toString() {
		return "ClassPojo [label = " + label + ", required = " + required + "]";
	}
}
