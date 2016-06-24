package com.jobviewer.confined.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.jobviewer.confined.ConfinedQuestionManager;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.R;

public class ConfinedYesNoTypeFragment extends Fragment implements
		OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressStep, questionTitle, question, errorMessage,
			screenTitle;
	private RadioButton radio_yes, radio_no;
	private LinearLayout errorView;
	private Button mCancel, mNext;
	private View mRootView;
	private Screen currentScreen;
	private RadioGroup radioGroup1;
	private CheckOutObject checkOutRemember;

	public interface onClicksEnterJobNumber {
		void onNextClick();

		void onCancelClick();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mRootView = inflater.inflate(R.layout.question_yes_no_fragment,
				container, false);
		removePhoneKeypad();
		initUI();
		updateData();
		return mRootView;
	}

	public void removePhoneKeypad() {
		InputMethodManager inputManager = (InputMethodManager) mRootView
				.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

		IBinder binder = mRootView.getWindowToken();
		inputManager.hideSoftInputFromWindow(binder,
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void updateData() {
		currentScreen = ConfinedQuestionManager.getInstance()
				.getCurrentScreen();
		mProgressStep.setText(currentScreen.get_progress() + "%");
		screenTitle.setText(getResources().getString(
				R.string.confined_space_str));
		mProgress.setProgress(Integer.parseInt(currentScreen.get_progress()));
		questionTitle.setText(currentScreen.getTitle());
		question.setText(currentScreen.getText());
		radio_yes.setText(currentScreen.getOptions().getOption()[0].getLabel());
		radio_no.setText(currentScreen.getOptions().getOption()[1].getLabel());

		if (ActivityConstants.YES.equalsIgnoreCase(currentScreen.getAnswer())) {
			radio_yes.setChecked(true);
			mNext.setEnabled(true);
			mNext.setBackgroundResource(R.drawable.red_background);
		} else if (ActivityConstants.NO.equalsIgnoreCase(currentScreen
				.getAnswer())) {
			radio_no.setChecked(true);
			mNext.setEnabled(true);
			mNext.setBackgroundResource(R.drawable.red_background);
		} else {
			radio_yes.setChecked(false);
			radio_no.setChecked(false);
			mNext.setEnabled(false);
			mNext.setBackgroundResource(R.drawable.dark_grey_background);
		}

		radioGroup1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radio_yes) {
					showErrorAlert(0);
					currentScreen.setAnswer(ActivityConstants.YES);
				} else {
					showErrorAlert(1);
					currentScreen.setAnswer(ActivityConstants.NO);
				}
				mNext.setEnabled(true);
				mNext.setBackgroundResource(R.drawable.red_background);
			}

			private void showErrorAlert(int i) {
				if (currentScreen.getOptions().getOption()[i].getActions()
						.getClick() != null) {
					if (!Utils.isNullOrEmpty(currentScreen.getOptions()
							.getOption()[i].getActions().getClick().getText())) {
						errorMessage.setText(currentScreen.getOptions()
								.getOption()[i].getActions().getClick()
								.getText());
						errorView.setVisibility(View.VISIBLE);
					} else {
						errorView.setVisibility(View.GONE);
					}

					if (!Utils.isNullOrEmpty(currentScreen.getOptions()
							.getOption()[i].getActions().getClick()
							.getVibrate())) {
						Utils.shakeAnimation(getActivity(), errorView);
						Vibrator v = (Vibrator) getActivity().getSystemService(
								Context.VIBRATOR_SERVICE);
						// Vibrate for 500 milliseconds
						v.vibrate(500);
					}
				} else {
					errorView.setVisibility(View.GONE);
				}
			}
		});
		if(ConfinedQuestionManager.getInstance().isFirstScreen(currentScreen)){
			mCancel.setText(getResources().getString(R.string.cancel));
		}
	}

	private void initUI() {
		mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
		mProgressStep = (TextView) mRootView
				.findViewById(R.id.progress_step_text);
		mCancel = (Button) mRootView.findViewById(R.id.button1);
		mCancel.setOnClickListener(this);
		screenTitle = (TextView) mRootView.findViewById(R.id.screenTitle);
		mNext = (Button) mRootView.findViewById(R.id.button2);
		mNext.setOnClickListener(this);
		questionTitle = (TextView) mRootView.findViewById(R.id.questionTitle);
		question = (TextView) mRootView.findViewById(R.id.question);
		errorMessage = (TextView) mRootView.findViewById(R.id.errorMessage);
		errorView = (LinearLayout) mRootView.findViewById(R.id.errorView);
		radio_yes = (RadioButton) mRootView.findViewById(R.id.radio_yes);
		radio_no = (RadioButton) mRootView.findViewById(R.id.radio_no);
		radioGroup1 = (RadioGroup) mRootView.findViewById(R.id.radioGroup1);

	}

	@SuppressWarnings("static-access")
	@Override
	public void onClick(View view) {
		if (view == mCancel) {
			CheckOutObject checkOutRemember = JobViewerDBHandler.getCheckOutRemember(getActivity());
			ConfinedQuestionManager.getInstance().saveAssessment(
					checkOutRemember.getAssessmentSelected());
			Intent intent = new Intent(view.getContext(),
					ActivityPageActivity.class);
			intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} else if (view == mNext) {
			if (radioGroup1.getCheckedRadioButtonId() != -1) {
				int checkedRadioButtonId = radioGroup1
						.getCheckedRadioButtonId();
				if (R.id.radio_yes == checkedRadioButtonId) {
					loadFragment(0);
				} else {
					loadFragment(1);
				}
			}
		}
	}

	private void loadFragment(int i) {
		ConfinedQuestionManager.getInstance().updateScreenOnQuestionMaster(
				currentScreen);
		ConfinedQuestionManager.getInstance().loadNextFragment(
				currentScreen.getOptions().getOption()[i].getActions()
						.getSubmit().getOnClick());
	}
}
