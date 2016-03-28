package com.jobviewer.survey.object;

public class Click {
	private String text;

	private String vibrate;

	private String type;
	
	private String onClick;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getVibrate() {
		return vibrate;
	}

	public void setVibrate(String vibrate) {
		this.vibrate = vibrate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ClassPojo [text = " + text + ", vibrate = " + vibrate
				+ ", type = " + type + "]";
	}

	public String getOnClick() {
		return onClick;
	}

	public void setOnClick(String onClick) {
		this.onClick = onClick;
	}
}
