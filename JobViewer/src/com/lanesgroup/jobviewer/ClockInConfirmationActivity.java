package com.lanesgroup.jobviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;

public class ClockInConfirmationActivity extends BaseActivity implements
		OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressStep, mShiftStartTime, mUserEmail, mDivider,
			mVehicleUsed, mMileage;
	private CheckBox mCheckBox;
	private Button mBack, mClockIn;
	private String mCallingActivity;

	private final String CALLING_ACTIVITY = "callingActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clock_in_no_screen);
		initUI();
	}

	private void initUI() {
		mCallingActivity = getIntent().getExtras().get(CALLING_ACTIVITY)
				.toString();
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mProgressStep = (TextView) findViewById(R.id.progress_step_text);
		mShiftStartTime = (TextView) findViewById(R.id.date_time_text);
		mUserEmail = (TextView) findViewById(R.id.user_email_text);
		mDivider = (TextView) findViewById(R.id.stroke_text2);
		mVehicleUsed = (TextView) findViewById(R.id.vehicle_used_text);
		mMileage = (TextView) findViewById(R.id.mileage_text);
		mProgress.setMax(8);
		mProgress.setProgress(7);
		mShiftStartTime.setText(Utils.getCurrentDateAndTime());
		Utils.checkOutObject.setJobStartedTime(Utils.getCurrentDateAndTime());
		User userProfile = JobViewerDBHandler.getUserProfile(this);

		mUserEmail.setText(userProfile.getEmail());
		if (mCallingActivity.equalsIgnoreCase("ClockInActivity")) {
			mProgressStep.setText(Utils.PROGRESS_2_TO_2);
			mDivider.setVisibility(View.GONE);
			mVehicleUsed.setVisibility(View.GONE);
			mMileage.setVisibility(View.GONE);
		} else if (mCallingActivity.equalsIgnoreCase("WelcomeActivity")) {
			mProgressStep.setText(Utils.PROGRESS_1_TO_1);

		} else {
			mProgressStep.setText(Utils.PROGRESS_2_TO_3);
			mVehicleUsed.setVisibility(View.VISIBLE);
			mMileage.setVisibility(View.VISIBLE);
			mMileage.setText(Utils.checkOutObject.getVehicleRegistration()
					+ "(mileage " + Utils.checkOutObject.getMilage() + ")");
			mDivider.setVisibility(View.VISIBLE);
		}
		mCheckBox = (CheckBox) findViewById(R.id.confirm_checkbox);
		mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				updatesOnChecked(isChecked);
			}
		});
		mBack = (Button) findViewById(R.id.back);
		mBack.setOnClickListener(this);
		mClockIn = (Button) findViewById(R.id.clockin);
	}

	private void updatesOnChecked(boolean isChecked) {
		if (isChecked) {
			mClockIn.setBackground(ResourcesCompat.getDrawable(getResources(),
					R.drawable.red_background, null));
			mClockIn.setOnClickListener(this);
			mProgress.setProgress(8);
		} else {
			mClockIn.setBackground(ResourcesCompat.getDrawable(getResources(),
					R.drawable.dark_grey_background, null));
			mClockIn.setOnClickListener(null);
			mProgress.setProgress(7);
		}
	}

	@Override
	public void onClick(View view) {
		if (view == mBack) {
			finish();
		} else if (view == mClockIn) {
			JobViewerDBHandler.saveCheckOutRemember(this, Utils.checkOutObject);
			Intent intent = new Intent(ClockInConfirmationActivity.this,
					ActivityPageActivity.class);
			startActivity(intent);
		}
	}
}