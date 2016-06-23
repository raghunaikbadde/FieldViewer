package com.jobviewer.confined.fragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.confined.ConfinedQuestionManager;
import com.jobviewer.confined.CountdownTimer;
import com.jobviewer.survey.object.Screen;
import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.R;

public class ConfinedTimerFragment extends Fragment implements OnClickListener {
	private View mRootView;
	private ProgressBar progressBar;
	private TextView screenTitle, progress_step_text, overhead_text,
			question_text, next_update_text, timer_text;
	private LinearLayout skip_timer;
	private Button saveBtn;
	private static Button nextBtn;
	private Screen currentScreen;
	private String time;
	private CountdownTimer timer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mRootView = inflater.inflate(R.layout.confined_space_manhole_screen,
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
		progressBar.setProgress(Integer.parseInt(currentScreen.get_progress()));
		progress_step_text.setText(currentScreen.get_progress() + "%");
		overhead_text.setText(currentScreen.getTitle());
		question_text.setText(currentScreen.getText());
		time = currentScreen.getTime();
		if (currentScreen.isAllow_skip()) {
			skip_timer.setOnClickListener(this);
		} else {
			skip_timer.setOnClickListener(null);
		}
		long startTime = getStartTimeForTimer();
		timer = new CountdownTimer(startTime, 1000, timer_text, "timer");
		timer.start();
	}

	public static void enableNextButton(boolean isEnable) {
		if (isEnable) {
			nextBtn.setEnabled(true);
			nextBtn.setBackgroundResource(R.drawable.red_background);
		} else {
			nextBtn.setEnabled(false);
			nextBtn.setBackgroundResource(R.drawable.dark_grey_background);
		}

	}

	private long getStartTimeForTimer() {
		Pattern pattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");
		Matcher matcher = pattern.matcher(currentScreen.getTime());
		if (matcher.matches()) {
			return Long.parseLong(matcher.group(1)) * 3600000L
					+ Long.parseLong(matcher.group(2)) * 60000
					+ Long.parseLong(matcher.group(3)) * 1000;
		}
		return 0;
	}

	private void initUI() {
		screenTitle = (TextView) mRootView.findViewById(R.id.risk_assess_text);
		progressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar);
		progress_step_text = (TextView) mRootView
				.findViewById(R.id.progress_step_text);
		overhead_text = (TextView) mRootView.findViewById(R.id.overhead_text);
		question_text = (TextView) mRootView.findViewById(R.id.question_text);
		next_update_text = (TextView) mRootView
				.findViewById(R.id.next_update_text);
		timer_text = (TextView) mRootView.findViewById(R.id.timer_text);
		skip_timer = (LinearLayout) mRootView.findViewById(R.id.skip_timer);
		saveBtn = (Button) mRootView.findViewById(R.id.saveBtn);
		saveBtn.setOnClickListener(this);
		nextBtn = (Button) mRootView.findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nextBtn:
			timer.cancel();
			ConfinedQuestionManager.getInstance().updateScreenOnQuestionMaster(
					currentScreen);
			ConfinedQuestionManager.getInstance().loadNextFragment(
					currentScreen.getButtons().getButton()[2].getActions()
							.getClick().getOnClick());
			break;
		case R.id.saveBtn:
			timer.cancel();
			ConfinedQuestionManager.getInstance().saveAssessment("Confined");
			((BaseActivity) getActivity()).finish();
			break;
		case R.id.skip_timer:
			if (currentScreen.isAllow_skip()) {
				currentScreen.setTimer_skipped(true);
				timer.cancel();
				enableNextButton(true);
			}
			break;
		default:
			break;
		}

	}
}
