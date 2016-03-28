package com.jobviewer.survey.object;

public class Options {
	private Option[] option;

	public Option[] getOption() {
		return option;
	}

	public void setOption(Option[] option) {
		this.option = option;
	}

	@Override
	public String toString() {
		return "ClassPojo [option = " + option + "]";
	}
}
