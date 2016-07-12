package com.lanesgroup.jobviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;

public class WorkSuccessActivity extends BaseActivity implements
		OnClickListener {

	private Button mDoneButton;
	private TextView mVistecText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.success_screen);
		initUI();
	}

	private void initUI() {
		mDoneButton = (Button) findViewById(R.id.done_button);
		mVistecText = (TextView) findViewById(R.id.vistec_number_text);
		mDoneButton.setOnClickListener(this);
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(this);
		mVistecText.setText(checkOutRemember.getVistecId());
		JobViewerDBHandler
				.deleteAllAddCardSavedImages(WorkSuccessActivity.this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == mDoneButton.getId()) {
			Intent appPageActivityIntent = new Intent(WorkSuccessActivity.this,
					ActivityPageActivity.class);
			ClockInConfirmationActivity.initiateAlarm();
			ClockInConfirmationActivity.setAlarmForOverTime();
			JobViewerDBHandler.deleteQuestionSet(WorkSuccessActivity.this);
			finish();
			startActivity(appPageActivityIntent);
		}
	}

	@Override
	public void onBackPressed() {
		Intent appPageActivityIntent = new Intent(WorkSuccessActivity.this,
				ActivityPageActivity.class);
		ClockInConfirmationActivity.initiateAlarm();
		ClockInConfirmationActivity.setAlarmForOverTime();
		JobViewerDBHandler.deleteQuestionSet(WorkSuccessActivity.this);
		finish();
		startActivity(appPageActivityIntent);
	}
}
