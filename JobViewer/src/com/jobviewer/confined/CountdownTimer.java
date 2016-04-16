package com.jobviewer.confined;

import com.jobviewer.confined.fragment.ConfinedTimerFragment;

import android.os.CountDownTimer;
import android.widget.TextView;

public class CountdownTimer extends CountDownTimer {
	TextView textView;
	String calledFrom;

	public CountdownTimer(long startTime, long interval, TextView textView,
			String calledFrom) {
		super(startTime, interval);
		this.textView = textView;
		this.calledFrom = calledFrom;
	}

	@Override
	public void onFinish() {
		textView.setText("00:00:00");
		if (calledFrom.equalsIgnoreCase("timer")) {
			ConfinedTimerFragment.enableNextButton(true);
		}
		// ExamActivity.this.submitresult();
	}

	@Override
	public void onTick(long millisUntilFinished) {

		long millis = millisUntilFinished;

		int seconds = (int) (millis / 1000) % 60;
		int minutes = (int) ((millis / (1000 * 60)) % 60);
		int hours = (int) ((millis / (1000 * 60 * 60)) % 24);

		String ms = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		textView.setText(ms);
	}
}
