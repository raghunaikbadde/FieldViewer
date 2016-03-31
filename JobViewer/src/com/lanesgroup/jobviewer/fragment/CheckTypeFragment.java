package com.lanesgroup.jobviewer.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.R;

public class CheckTypeFragment extends Fragment implements OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressStep, questionTitle, question, screenTitle;
	CheckBox radio_yes;
	private Button mCancel, mNext;
	private View mRootView;
	Screen currentScreen;
	CheckOutObject checkOutRemember;

	public interface onClicksEnterJobNumber {
		public void onNextClick();

		public void onCancelClick();
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
		mRootView = inflater.inflate(R.layout.question_check_fragment,
				container, false);
		initUI();
		updateData();
		return mRootView;
	}

	private void updateData() {
		currentScreen = QuestionManager.getInstance().getCurrentScreen();
		questionTitle.setText(currentScreen.getTitle());
		question.setText(currentScreen.getText());
		radio_yes.setText(currentScreen.getCheckbox().getLabel());
		mProgress.setProgress(Integer.parseInt(currentScreen.get_progress()));
		mProgressStep.setText(currentScreen.get_progress() + "%");
		checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		if (ActivityConstants.EXCAVATION.equalsIgnoreCase(checkOutRemember
				.getAssessmentSelected())) {
			screenTitle.setText(getActivity().getResources().getString(
					R.string.excavation_risk_str));
		} else {
			screenTitle.setText(getActivity().getResources().getString(
					R.string.non_excavation_risk_assessment_str));
		}
		if (ActivityConstants.TRUE.equalsIgnoreCase(currentScreen.getCheckbox()
				.getRequired())) {
			enableNextButton(false);

		}
		radio_yes.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					enableNextButton(isChecked);
				} else {
					enableNextButton(false);
				}

			}
		});
		com.jobviewer.survey.object.Button[] buttons = currentScreen
				.getButtons().getButton();

		for (int i = 0; i < buttons.length; i++) {
			if (ActivityConstants.TRUE
					.equalsIgnoreCase(buttons[i].getDisplay())
					&& !"next".equalsIgnoreCase(buttons[i].getName())) {
				mCancel.setText(getResources().getString(
						Utils.getButtonText(buttons[i].getName())));
				break;
			}
		}
	}

	public void enableNextButton(boolean isEnable) {
		if (isEnable) {
			mNext.setEnabled(true);
			mNext.setBackgroundResource(R.drawable.red_background);
		} else {
			mNext.setEnabled(false);
			mNext.setBackgroundResource(R.drawable.dark_grey_background);
		}

	}

	private void initUI() {
		mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
		mProgressStep = (TextView) mRootView
				.findViewById(R.id.progress_step_text);
		mCancel = (Button) mRootView.findViewById(R.id.button1);
		mNext = (Button) mRootView.findViewById(R.id.button2);
		mNext.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		questionTitle = (TextView) mRootView.findViewById(R.id.questionTitle);
		question = (TextView) mRootView.findViewById(R.id.question);
		radio_yes = (CheckBox) mRootView.findViewById(R.id.radio_yes);
		screenTitle = (TextView) mRootView.findViewById(R.id.screenTitle);
	}

	@SuppressWarnings("static-access")
	@Override
	public void onClick(View view) {
		if (view == mCancel) {
			if ("save".equalsIgnoreCase(mCancel.getText().toString())) {
				QuestionManager.getInstance().saveAssessment(
						checkOutRemember.getAssessmentSelected());
				Intent intent = new Intent(view.getContext(),
						ActivityPageActivity.class);
				intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		} else if (view == mNext) {
			currentScreen.setAnswer(ActivityConstants.SELECTED);
			QuestionManager.getInstance().updateScreenOnQuestionMaster(
					currentScreen);
			QuestionManager.getInstance().loadNextFragment(
					currentScreen.getButtons().getButton()[2].getActions()
							.getClick().getOnClick());

		}
	}
}
