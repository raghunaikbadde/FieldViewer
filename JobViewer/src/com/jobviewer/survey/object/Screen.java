package com.jobviewer.survey.object;

public class Screen {
	private String _type;

	private String _number;

	private String text;

	private String title;

	private String required_images;

	private Checkbox checkbox;

	private String answer;

	private Images[] images;

	private Inputs[] inputs;

	private String _progress;

	private Buttons buttons;

	private Options options;

	private String input;

	private String time;

	private boolean allow_skip;
	
	private boolean timer_skipped;

	public String get_type() {
		return _type;
	}

	public void set_type(String _type) {
		this._type = _type;
	}

	public String get_number() {
		return _number;
	}

	public void set_number(String _number) {
		this._number = _number;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRequired_images() {
		return required_images;
	}

	public void setRequired_images(String required_images) {
		this.required_images = required_images;
	}

	public Checkbox getCheckbox() {
		return checkbox;
	}

	public void setCheckbox(Checkbox checkbox) {
		this.checkbox = checkbox;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public Images[] getImages() {
		return images;
	}

	public void setImages(Images[] images) {
		this.images = images;
	}

	public String get_progress() {
		return _progress;
	}

	public void set_progress(String _progress) {
		this._progress = _progress;
	}

	public Buttons getButtons() {
		return buttons;
	}

	public void setButtons(Buttons buttons) {
		this.buttons = buttons;
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	@Override
	public String toString() {
		return "ClassPojo [_type = " + _type + ", _number = " + _number
				+ ", text = " + text + ", title = " + title
				+ ", required_images = " + required_images + ", checkbox = "
				+ checkbox + ", answer = " + answer + ", images = " + images
				+ ", _progress = " + _progress + ", buttons = " + buttons
				+ ", options = " + options + "]";
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public boolean isAllow_skip() {
		return allow_skip;
	}

	public void setAllow_skip(boolean allow_skip) {
		this.allow_skip = allow_skip;
	}

	public Inputs[] getInputs() {
		return inputs;
	}

	public void setInputs(Inputs[] inputs) {
		this.inputs = inputs;
	}

	public boolean isTimer_skipped() {
		return timer_skipped;
	}

	public void setTimer_skipped(boolean timer_skipped) {
		this.timer_skipped = timer_skipped;
	}
}
