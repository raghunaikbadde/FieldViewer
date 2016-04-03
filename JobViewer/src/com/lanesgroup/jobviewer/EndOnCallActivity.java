package com.lanesgroup.jobviewer;

import java.text.NumberFormat;
import java.util.Locale;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EndOnCallActivity extends BaseActivity implements OnClickListener{

	Button mCancel,mEndOnCall;
	TextView mUserEmail,mVehileUsedHeading,mVehicleUsed,mMileage,mProgressText,mEndTime,mStrokeText;
	ProgressBar progressBar;
	CheckBox mConfirmCheckBox;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.end_on_call_layout);
		Log.d(Utils.LOG_TAG,"EndOnCallActivity onCreate");
		initUI();
		updateDetailsOnUI();
	}
	
	private void initUI(){
		mCancel = (Button)findViewById(R.id.cancel);
		mEndOnCall = (Button) findViewById(R.id.endoncall);
		mEndTime = (TextView)findViewById(R.id.date_time_text);
		mUserEmail = (TextView)findViewById(R.id.user_email_text);
		mStrokeText = (TextView)findViewById(R.id.stroke_text4);
		mVehileUsedHeading = (TextView)findViewById(R.id.vehicle_used);
		mProgressText = (TextView)findViewById(R.id.progress_step_text);
		mVehicleUsed = (TextView)findViewById(R.id.vehicle_used_text);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		mMileage = (TextView)findViewById(R.id.mileage_text);
		mConfirmCheckBox = (CheckBox)findViewById(R.id.confirm_checkbox);
		mCancel.setOnClickListener(this);
		mEndOnCall.setOnClickListener(this);
		mConfirmCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				enableNextButton(isChecked);
				if(isChecked){
					progressBar.setProgress(10);		
				} else {
					progressBar.setProgress(8);
				}
			}
		});
		//mProgressText.setText(getResources().getString(R.string.progress_step_end_on_call));
	}

	private void updateDetailsOnUI() {
		User user = JobViewerDBHandler.getUserProfile(EndOnCallActivity.this);
		String email = user.getEmail();
		CheckOutObject checkOutRemember = JobViewerDBHandler.getCheckOutRemember(EndOnCallActivity.this);
		String vehicleRegistrationNumber = checkOutRemember.getVehicleRegistration();
		Bundle bundle = getIntent().getExtras();
		String mileage = "";
		String progressStep = "";
		if(bundle!=null && bundle.containsKey("mileage")){
			mileage = bundle.getString("mileage");
		}
		Log.d(Utils.LOG_TAG,"EndOnCallActivity mileage"+mileage);
		if(bundle!=null && bundle.containsKey("progressStep")){
			progressStep = bundle.getString("progressStep");
		}
		Log.d(Utils.LOG_TAG,"EndOnCallActivity progressStep"+progressStep);
		//mProgressText.setText(progressStep);
		if(progressStep.contains("step 1 of 2")){
			
			progressBar.setMax(2);
			progressBar.setProgress(1);
		} else {
			progressBar.setMax(10);
			progressBar.setProgress(8);
		}
		if(!Utils.isNullOrEmpty(mileage)){
			NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
			mileage = numberFormat.format(Double.valueOf(mileage));
		}
		
		mUserEmail.setText(email);
		mVehicleUsed.setText(vehicleRegistrationNumber +" (mileage "+mileage+" )");
		mMileage.setText(mileage);
		mEndTime.setText(Utils.getCurrentDateAndTime());
		
		if(bundle != null && bundle.containsKey(Utils.CALLING_ACTIVITY)){
			if(bundle.getString(Utils.CALLING_ACTIVITY).contains("EndShiftReturnVehicleActivity")){
				mVehileUsedHeading.setVisibility(View.VISIBLE);
				mVehicleUsed.setVisibility(View.VISIBLE);
				mStrokeText.setVisibility(View.VISIBLE);
			}
		}
		
	}
	
	@Override
	public void onClick(View v) {
		if(v==mCancel){
			finish();
		} else if (v==mEndOnCall){
			
		}
	}
	
	private void executeEndOnCallService(){
		
	}
	
	private void closeAppOrGoOnCallActivity(){
		
	}
	
	public void enableNextButton(boolean isEnable) {
		if (isEnable) {
			mEndOnCall.setEnabled(isEnable);
			mEndOnCall.setBackgroundResource(R.drawable.red_background);
		} else {
			mEndOnCall.setEnabled(isEnable);
			mEndOnCall.setBackgroundResource(R.drawable.dark_grey_background);
		}
	}

}
