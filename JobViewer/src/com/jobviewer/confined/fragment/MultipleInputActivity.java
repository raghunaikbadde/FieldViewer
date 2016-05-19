package com.jobviewer.confined.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.confined.ConfinedQuestionManager;
import com.jobviewer.survey.object.Inputs;
import com.jobviewer.survey.object.Multipleinputs;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.R;

public class MultipleInputActivity extends BaseActivity implements
		View.OnClickListener, TextWatcher {
	ProgressBar progressBar;
	TextView progress_step_text, overhead_text, question_text, top_man_text,
			bottom_man1_text, bottom_man2_text, bottom_man3_text;
	EditText top_man_edittext, bottom_man1_edittext, bottom_man2_edittext,
			bottom_man3_edittext;
	Button saveBtn, nextBtn;
	Screen currentScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.confined_space_gas_monitor_readinng);

		initUI();
		updateData();
	}

	private void updateData() {
		currentScreen = ConfinedQuestionManager.getInstance()
				.getMultipleTypeScreen();
		progressBar.setProgress(Integer.parseInt(currentScreen.get_progress()));
		progress_step_text.setText(currentScreen.get_progress() + "%");
		overhead_text.setText(currentScreen.getTitle());
		question_text.setText(currentScreen.getText());
		setLevelOnTextView(currentScreen.getMultipleinputs()[0].getInputs());
		enableNextButton(false);

	}

	private void setLevelOnTextView(Inputs[] inputs) {
		top_man_text.setText(inputs[0].getLabel());
		bottom_man1_text.setText(inputs[1].getLabel());
		bottom_man2_text.setText(inputs[2].getLabel());
		bottom_man3_text.setText(inputs[3].getLabel());

	}

	public void enableNextButton(boolean isEnable) {
		if (isEnable) {
			nextBtn.setEnabled(true);
			nextBtn.setBackgroundResource(R.drawable.red_background);
		} else {
			nextBtn.setEnabled(false);
			nextBtn.setBackgroundResource(R.drawable.dark_grey_background);
		}

	}

	private void initUI() {
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progress_step_text = (TextView) findViewById(R.id.progress_step_text);
		overhead_text = (TextView) findViewById(R.id.overhead_text);
		question_text = (TextView) findViewById(R.id.question_text);
		top_man_text = (TextView) findViewById(R.id.top_man_text);
		bottom_man1_text = (TextView) findViewById(R.id.bottom_man1_text);
		bottom_man2_text = (TextView) findViewById(R.id.bottom_man2_text);
		bottom_man3_text = (TextView) findViewById(R.id.bottom_man3_text);

		top_man_edittext = (EditText) findViewById(R.id.top_man_edittext);
		bottom_man1_edittext = (EditText) findViewById(R.id.bottom_man1_edittext);
		bottom_man2_edittext = (EditText) findViewById(R.id.bottom_man2_edittext);
		bottom_man3_edittext = (EditText) findViewById(R.id.bottom_man3_edittext);

		top_man_edittext.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_DECIMAL);
		bottom_man1_edittext.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_DECIMAL);
		bottom_man2_edittext.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_DECIMAL);
		bottom_man3_edittext.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_DECIMAL);
		top_man_edittext.addTextChangedListener(this);
		bottom_man1_edittext.addTextChangedListener(this);
		bottom_man2_edittext.addTextChangedListener(this);
		bottom_man3_edittext.addTextChangedListener(this);

		saveBtn = (Button) findViewById(R.id.saveBtn);
		saveBtn.setOnClickListener(this);
		nextBtn = (Button) findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		Multipleinputs multipleinputs = currentScreen.getMultipleinputs()[0];
		if (Utils.isNullOrEmpty(multipleinputs.getInputs()[0].getAnswer())) {
			currentScreen.getMultipleinputs()[0].getInputs()[0]
					.setAnswer(top_man_edittext.getText().toString());
			currentScreen.getMultipleinputs()[0].getInputs()[1]
					.setAnswer(bottom_man1_edittext.getText().toString());
			currentScreen.getMultipleinputs()[0].getInputs()[2]
					.setAnswer(bottom_man2_edittext.getText().toString());
			currentScreen.getMultipleinputs()[0].getInputs()[3]
					.setAnswer(bottom_man3_edittext.getText().toString());
		} else {
			int length = currentScreen.getMultipleinputs().length;
			Multipleinputs[] multipleinputs2 = new Multipleinputs[length + 1];

			for (int i = 0; i < length; i++) {
				multipleinputs2[i] = currentScreen.getMultipleinputs()[i];
			}
			multipleinputs2[length] = new Multipleinputs();
			Inputs[] copyOf = new Inputs[4];
			copyOf[0] = new Inputs();
			copyOf[1] = new Inputs();
			copyOf[2] = new Inputs();
			copyOf[3] = new Inputs();
			multipleinputs2[length].setInputs(copyOf);

			currentScreen.setMultipleinputs(multipleinputs2);
			currentScreen.getMultipleinputs()[length].getInputs()[0]
					.setAnswer(top_man_edittext.getText().toString());
			currentScreen.getMultipleinputs()[length].getInputs()[1]
					.setAnswer(bottom_man1_edittext.getText().toString());
			currentScreen.getMultipleinputs()[length].getInputs()[2]
					.setAnswer(bottom_man2_edittext.getText().toString());
			currentScreen.getMultipleinputs()[length].getInputs()[3]
					.setAnswer(bottom_man3_edittext.getText().toString());
		}
		ConfinedQuestionManager.getInstance().UpdateMultipleScreen(
				currentScreen);
		finish();

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTextChanged(Editable s) {
		String text1 = top_man_edittext.getText().toString();
		String text2 = bottom_man1_edittext.getText().toString();
		String text3 = bottom_man2_edittext.getText().toString();
		String text4 = bottom_man3_edittext.getText().toString();
		if (!Utils.isNullOrEmpty(text1) && !Utils.isNullOrEmpty(text2)
				&& !Utils.isNullOrEmpty(text3) && !Utils.isNullOrEmpty(text4)) {
			enableNextButton(true);
		}

	}

}
