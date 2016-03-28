package com.jobviewer.survey.object;

public class Actions {
	private Submit submit;

	private Click click;

	public Submit getSubmit() {
		return submit;
	}

	public void setSubmit(Submit submit) {
		this.submit = submit;
	}

	public Click getClick() {
		return click;
	}

	public void setClick(Click click) {
		this.click = click;
	}

	@Override
	public String toString() {
		return "ClassPojo [submit = " + submit + ", click = " + click + "]";
	}
}
