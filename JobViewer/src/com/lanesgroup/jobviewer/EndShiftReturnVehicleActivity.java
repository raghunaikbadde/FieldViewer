package com.lanesgroup.jobviewer;

import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EndShiftReturnVehicleActivity extends BaseActivity implements
		OnClickListener {

	Button mCancel, mNext;
	EditText mMileage;
	TextView mVehicleRegNo;
	ProgressBar mProgressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.end_shift_layout);
		initUI();

	}

	private void initUI() {
		mCancel = (Button) findViewById(R.id.cancel_button);
		mNext = (Button) findViewById(R.id.next_button);
		mMileage = (EditText)findViewById(R.id.enter_mileage_edittext);
		mVehicleRegNo = (TextView)findViewById(R.id.vehicle_registration_text_value);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mProgressBar.setMax(2);
		mProgressBar.setProgress(1);
		mMileage.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(s.toString().length()>0){
					enableNextButton(true);
				} else {
					enableNextButton(false);
				}
			}
		});
		mCancel.setOnClickListener(this);
		mVehicleRegNo.setText(JobViewerDBHandler.getCheckOutRemember(EndShiftReturnVehicleActivity.this).getVehicleRegistration());
	}

	@Override
	public void onClick(View v) {

		if (v == mCancel) {
			finish();
		} else if (v == mNext) {
			if (Utils.isInternetAvailable(EndShiftReturnVehicleActivity.this)) {
				executeCheckInVehicleService();
			} else {
				// save time sheet in DB
				launchEndOnCallActivity();
			}
		}
	}

	private void executeCheckInVehicleService() {
		launchEndOnCallActivity();
	}

	private void launchEndOnCallActivity() {

		Intent endOnCallActivity = new Intent(
				EndShiftReturnVehicleActivity.this, EndOnCallActivity.class);
		endOnCallActivity.putExtra("mileage",mMileage.getText().toString());
		endOnCallActivity.putExtra(Utils.CALLING_ACTIVITY,EndShiftReturnVehicleActivity.this.getClass().getSimpleName());
		startActivity(endOnCallActivity);
	}
	public void enableNextButton(boolean isEnable) {
		if (isEnable) {
			mNext.setEnabled(isEnable);
			mNext.setBackgroundResource(R.drawable.red_background);
			mNext.setOnClickListener(this);
		} else {
			mNext.setEnabled(isEnable);
			mNext.setBackgroundResource(R.drawable.dark_grey_background);
			mNext.setOnClickListener(null);
		}
	}
}
