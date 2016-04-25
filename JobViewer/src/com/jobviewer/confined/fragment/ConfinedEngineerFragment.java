package com.jobviewer.confined.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.confined.ConfinedQuestionManager;
import com.jobviewer.survey.object.Inputs;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.R;

public class ConfinedEngineerFragment extends Fragment implements
		OnClickListener,TextWatcher {
	private View mRootView;
	Screen currentScreen;
	ProgressBar progressBar;
	TextView progress_step_text, overhead_text, question_text, top_man_text,
			bottom_man1_text, bottom_man2_text, bottom_man3_text;
	EditText top_man_edittext, bottom_man1_edittext, bottom_man2_edittext,
			bottom_man3_edittext;
	Button saveBtn, nextBtn;
	public static String engineerName1,engineerName2,engineerName3;
	public static String gasMonitorReading1,gasMonitorReading2,gasMonitorReading3,gasMonitorReading4;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mRootView = inflater.inflate(R.layout.confined_space_engineer_screen,
				container, false);
		removePhoneKeypad();
		initUI();
		updateData();
		
		return mRootView;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		switch (newConfig.keyboardHidden) {
		case Configuration.KEYBOARDHIDDEN_YES:
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView()
					.getWindowToken(), 0);
			break;
		case Configuration.KEYBOARDHIDDEN_NO:
			break;
		}
	}
	
	public void removePhoneKeypad() {
	    InputMethodManager inputManager = (InputMethodManager) mRootView
	            .getContext()
	            .getSystemService(Context.INPUT_METHOD_SERVICE);

	    IBinder binder = mRootView.getWindowToken();
	    inputManager.hideSoftInputFromWindow(binder,
	            InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void updateData() {
		currentScreen = ConfinedQuestionManager.getInstance()
				.getCurrentScreen();
		progressBar.setProgress(Integer.parseInt(currentScreen.get_progress()));
		progress_step_text.setText(currentScreen.get_progress() + "%");
		overhead_text.setText(currentScreen.getTitle());
		question_text.setText(currentScreen.getText());

		if (currentScreen.getInputs().length < 4) {
			bottom_man3_text.setVisibility(View.GONE);
			bottom_man3_edittext.setVisibility(View.GONE);
			if(engineerName1 !=null && engineerName2!=null && engineerName3!=null){
				top_man_edittext.setText(engineerName1);
				bottom_man1_edittext.setText(engineerName2);
				bottom_man2_edittext.setText(engineerName3);
				enableNextButton(true);
			} else{
				enableNextButton(false);
			}
			top_man_edittext.addTextChangedListener(this);
			bottom_man1_edittext.addTextChangedListener(this);
			bottom_man2_edittext.addTextChangedListener(this);
			
		} else {
			bottom_man3_text.setVisibility(View.VISIBLE);
			bottom_man3_edittext.setVisibility(View.VISIBLE);
			if(gasMonitorReading1 !=null && gasMonitorReading2!=null && gasMonitorReading3!=null && gasMonitorReading4!=null){
				top_man_edittext.setText(gasMonitorReading1);
				bottom_man1_edittext.setText(gasMonitorReading2);
				bottom_man2_edittext.setText(gasMonitorReading3);
				bottom_man3_edittext.setText(gasMonitorReading4);				
			} 
			enableNextButton(true);
		}
		setDataInEditText();
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
		progressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar);
		progress_step_text = (TextView) mRootView
				.findViewById(R.id.progress_step_text);
		overhead_text = (TextView) mRootView.findViewById(R.id.overhead_text);
		question_text = (TextView) mRootView.findViewById(R.id.question_text);
		top_man_text = (TextView) mRootView.findViewById(R.id.top_man_text);
		bottom_man1_text = (TextView) mRootView
				.findViewById(R.id.bottom_man1_text);
		bottom_man2_text = (TextView) mRootView
				.findViewById(R.id.bottom_man2_text);
		bottom_man3_text = (TextView) mRootView
				.findViewById(R.id.bottom_man3_text);

		top_man_edittext = (EditText) mRootView
				.findViewById(R.id.top_man_edittext);
		bottom_man1_edittext = (EditText) mRootView
				.findViewById(R.id.bottom_man1_edittext);
		bottom_man2_edittext = (EditText) mRootView
				.findViewById(R.id.bottom_man2_edittext);
		bottom_man3_edittext = (EditText) mRootView
				.findViewById(R.id.bottom_man3_edittext);
		saveBtn = (Button) mRootView.findViewById(R.id.saveBtn);
		saveBtn.setOnClickListener(this);
		nextBtn = (Button) mRootView.findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(this);
	}

	private void setDataInEditText() {
		Inputs[] inputs = currentScreen.getInputs();
		for (int i = 0; i < inputs.length; i++) {
			if (i == 0) {
				top_man_text.setText(inputs[i].getLabel());
				top_man_edittext.setHint(inputs[i].getPlaceholder());
				top_man_edittext.setTag(i);
				if ("number".equalsIgnoreCase(inputs[i].getType())) {
					top_man_edittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);					
				}
			} else if (i == 1) {
				bottom_man1_text.setText(inputs[i].getLabel());
				bottom_man1_edittext.setHint(inputs[i].getPlaceholder());
				bottom_man1_edittext.setTag(i);
				if ("number".equalsIgnoreCase(inputs[i].getType())) {
					bottom_man1_edittext
							.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
				}
			} else if (i == 2) {
				bottom_man2_text.setText(inputs[i].getLabel());
				bottom_man2_edittext.setHint(inputs[i].getPlaceholder());
				bottom_man2_edittext.setTag(i);
				if ("number".equalsIgnoreCase(inputs[i].getType())) {
					bottom_man2_edittext
							.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
				}
			} else if (i == 3) {
				bottom_man3_text.setText(inputs[i].getLabel());
				bottom_man3_edittext.setHint(inputs[i].getPlaceholder());
				bottom_man3_edittext.setTag(i);
				if ("number".equalsIgnoreCase(inputs[i].getType())) {
					bottom_man3_edittext
							.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
				}
			}
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nextBtn:
			if (validUserInputs()) {
				setAnswerForInputs();
				saveEngineerNames();
				
				try{  
					 View view = getActivity().getCurrentFocus();
					 if(view!=null){
						 InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						 imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
					 }
				}catch(Exception e){
					
				}
				ConfinedQuestionManager.getInstance()
						.updateScreenOnQuestionMaster(currentScreen);
				ConfinedQuestionManager.getInstance().loadNextFragment(
						currentScreen.getButtons().getButton()[2].getActions()
								.getClick().getOnClick());
			} 
			break;
		case R.id.saveBtn:
			saveEngineerNames();
			ConfinedQuestionManager.getInstance().saveAssessment("Confined");
			((BaseActivity) getActivity()).finish();
			
			break;
		default:
			break;
		}

	}

	private void saveEngineerNames() {
		if (currentScreen.getInputs().length < 4) {
		engineerName1 = top_man_edittext.getText().toString();
		engineerName2 = bottom_man1_edittext.getText().toString();
		engineerName3 = bottom_man2_edittext.getText().toString();
		}
	}

	private void saveGasMonitorReadings(){
		if (currentScreen.getInputs().length >= 4) {
			gasMonitorReading1 = top_man_edittext.getText().toString();
			gasMonitorReading2 = bottom_man1_edittext.getText().toString();
			gasMonitorReading3 = bottom_man2_edittext.getText().toString();
			gasMonitorReading4 = bottom_man3_edittext.getText().toString();
		}
	}
	private void setAnswerForInputs() {
		for (int i = 0; i < currentScreen.getInputs().length; i++) {
			if (i == 0) {
				currentScreen.getInputs()[i].setAnswer(top_man_edittext
						.getText().toString());
			} else if (i == 1) {
				currentScreen.getInputs()[i].setAnswer(bottom_man1_edittext
						.getText().toString());
			} else if (i == 2) {
				currentScreen.getInputs()[i].setAnswer(bottom_man2_edittext
						.getText().toString());
			} else if (i == 3) {
				currentScreen.getInputs()[i].setAnswer(bottom_man3_edittext
						.getText().toString());
			}
		}

	}

	private boolean validUserInputs() {
		boolean isValid = true;
		Inputs[] inputs = currentScreen.getInputs();
		for (int i = 0; i < inputs.length; i++) {
			if (i == 0) {
				if (currentScreen.getInputs()[0].getRequired()
						&& Utils.isNullOrEmpty(top_man_edittext.getText()
								.toString())) {
					top_man_edittext.setError(currentScreen.getInputs()[0]
							.getLabel()
							+ " "
							+ getResources().getString(
									R.string.topManEditTextError));
					top_man_edittext.requestFocus();
					isValid = false;
					break;
				} else {
					isValid = isValidGasLevel(currentScreen.getInputs()[0]
							.getLabel());
					if (!isValid) {
						break;
					}
				}
			} else if (i == 1) {
				if (currentScreen.getInputs()[1].getRequired()
						&& Utils.isNullOrEmpty(bottom_man1_edittext.getText()
								.toString())) {
					bottom_man1_edittext.setError(currentScreen.getInputs()[1]
							.getLabel()
							+ " "
							+ getResources().getString(
									R.string.topManEditTextError));
					bottom_man1_edittext.requestFocus();
					isValid = false;
					break;
				} else {
					isValid = isValidGasLevel(currentScreen.getInputs()[1]
							.getLabel());
					if (!isValid) {
						break;
					}
				}
			} else if (i == 2) {
				if (currentScreen.getInputs()[2].getRequired()
						&& Utils.isNullOrEmpty(bottom_man2_edittext.getText()
								.toString())) {
					bottom_man2_edittext.setError(currentScreen.getInputs()[2]
							.getLabel()
							+ " "
							+ getResources().getString(
									R.string.topManEditTextError));
					bottom_man2_edittext.requestFocus();
					isValid = false;
					break;
				} else {
					isValid = isValidGasLevel(currentScreen.getInputs()[2]
							.getLabel());
					if (!isValid) {						
						break;
					}
				}
			} else if (i == 3) {
				if (currentScreen.getInputs()[3].getRequired()
						&& Utils.isNullOrEmpty(bottom_man3_edittext.getText()
								.toString())) {
					bottom_man3_edittext.setError(currentScreen.getInputs()[3]
							.getLabel()
							+ " "
							+ getResources().getString(
									R.string.topManEditTextError));
					bottom_man3_edittext.requestFocus();
					isValid = false;
				}
			} else {
				isValid = isValidGasLevel(currentScreen.getInputs()[3]
						.getLabel());
				if (!isValid) {
					
					break;
				}
			}
		}
		return isValid;
	}

	private boolean isValidGasLevel(String label) {
		boolean validGasLevel = true;
		if ("CH4".equalsIgnoreCase(label)) {
			double ch4Level = Double.parseDouble(top_man_edittext.getText()
					.toString());
			if (ch4Level <= 9.9) {
				return true;
			} else {
				//top_man_edittext.setError("CH4 should be 9.9 or below.");
				callStopFragment();
				return false;
			}
		}
		if ("O2".equalsIgnoreCase(label)) {
			double ch4Level = Double.parseDouble(bottom_man1_edittext.getText()
					.toString());
			if (ch4Level >= 19.0) {
				return true;
			} else {
				//bottom_man1_edittext.setError("O2 should be 19.0 or above.");
				callStopFragment();
				return false;
			}
		}
		if ("CO".equalsIgnoreCase(label)) {
			double ch4Level = Double.parseDouble(bottom_man2_edittext.getText()
					.toString());
			if (ch4Level <= 29.9) {
				return true;
			} else {
				//bottom_man2_edittext.setError("CO should be 29.9 or below.");
				callStopFragment();
				return false;
			}
		}
		if ("H2S".equalsIgnoreCase(label)) {
			double ch4Level = Double.parseDouble(bottom_man3_edittext.getText()
					.toString());
			if (ch4Level <= 4.9) {
				return true;
			} else {
				//bottom_man3_edittext.setError("H2S should be 4.9 or below.");
				callStopFragment();
				return false;
			}
		}

		return validGasLevel;
	}

	private void callStopFragment() {
		
		try{  
			 View view = getActivity().getCurrentFocus();
			 if(view!=null){
				 InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				 imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			 }
		}catch(Exception e){
			
		}
		saveGasMonitorReadings();
		ConfinedQuestionManager.getInstance()
		.updateScreenOnQuestionMaster(currentScreen);
		ConfinedQuestionManager.getInstance().loadNextFragment(ActivityConstants.STOP_SCREEN);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if(!Utils.isNullOrEmpty(top_man_edittext.getText().toString()) 
				&& !Utils.isNullOrEmpty(bottom_man1_edittext.getText().toString())
				&& !Utils.isNullOrEmpty(bottom_man2_edittext.getText().toString())){
			enableNextButton(true);
		} else {
			enableNextButton(false);
		}
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		
		
	}

}
