package com.lanesgroup.jobviewer;

import java.text.NumberFormat;
import java.util.Locale;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.ChangeTimeDialog;
import com.jobviewer.util.Constants;
import com.jobviewer.util.OverrideReasoneDialog;
import com.jobviewer.util.Utils;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.User;
import com.vehicle.communicator.HttpConnection;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EndOnCallActivity extends BaseActivity implements OnClickListener{

	Button mCancel,mEndOnCall;
	TextView mUserEmail,mVehileUsedHeading,mVehicleUsed,mMileage,mProgressText,mEndTime,mStrokeText,mHeading, mOverrideStartTime;
	ProgressBar progressBar;
	private ImageView mEditTime;
	CheckBox mConfirmCheckBox;
	CheckOutObject checkOutRemember;
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
		mHeading = (TextView)findViewById(R.id.clockin_text);
		mEndTime = (TextView)findViewById(R.id.date_time_text);
		mUserEmail = (TextView)findViewById(R.id.user_email_text);
		mStrokeText = (TextView)findViewById(R.id.stroke_text4);
		mEditTime = (ImageView) findViewById(R.id.edit_date);
		mEditTime.setOnClickListener(this);
		mOverrideStartTime = (TextView) findViewById(R.id.overrided_date_time_text);
		mVehileUsedHeading = (TextView)findViewById(R.id.vehicle_used);
		mProgressText = (TextView)findViewById(R.id.progress_step_text);
		mVehicleUsed = (TextView)findViewById(R.id.vehicle_used_text);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		mMileage = (TextView)findViewById(R.id.mileage_text);
		mConfirmCheckBox = (CheckBox)findViewById(R.id.confirm_checkbox);
		mCancel.setOnClickListener(this);
		
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
		checkOutRemember = JobViewerDBHandler.getCheckOutRemember(EndOnCallActivity.this);
		String vehicleRegistrationNumber = checkOutRemember.getVehicleRegistration();
		Bundle bundle = getIntent().getExtras();
		String mileage = "";
		String progressStep = "";
		if(checkOutRemember.getJobSelected().contains("shift")){
			mHeading.setText(getResources().getString(R.string.end_shift_screen_title));
			mEndOnCall.setText(getResources().getString(R.string.end_shift_screen_title));
		}
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
			Utils.startProgress(EndOnCallActivity.this);
			if(Utils.isInternetAvailable(EndOnCallActivity.this)){
				executeEndShiftOrCallService();
			} else{
				saveEndCallOrEndShiftInBackLogDB();
				Utils.StopProgress();
				callEndCallShiftActivity();
			}
			
			
		} else if(v == mEditTime){
			Intent intent = new Intent(EndOnCallActivity.this, ChangeTimeDialog.class);
			if(checkOutRemember.getJobSelected().contains("shift")){
			
				intent.putExtra("eventType", "EndShift");			
				intent.putExtra("eventType1", "EndShift");
				Utils.endShiftRequest.setStarted_at(Utils
						.getCurrentDateAndTime());
				Utils.endShiftRequest.setIs_overriden("true");
				User userProfile = JobViewerDBHandler.getUserProfile(this);
				CheckOutObject checkOutRemember = JobViewerDBHandler
						.getCheckOutRemember(this);
				Utils.endShiftRequest.setUser_id(userProfile
						.getEmail());
				Utils.endShiftRequest
				.setReference_id(checkOutRemember.getVistecId());
				Utils.endShiftRequest.setRecord_for(userProfile
						.getEmail());
				
			} else{
				intent.putExtra("eventType", "EndOnCall");			
				intent.putExtra("eventType1", "EndOnCall");
				Utils.callEndTimeRequest.setStarted_at(Utils
						.getCurrentDateAndTime());
				Utils.callEndTimeRequest.setIs_overriden("true");
				User userProfile = JobViewerDBHandler.getUserProfile(this);
				CheckOutObject checkOutRemember = JobViewerDBHandler
						.getCheckOutRemember(this);
				Utils.callEndTimeRequest.setUser_id(userProfile
						.getEmail());
				Utils.callEndTimeRequest
				.setReference_id(checkOutRemember.getVistecId());
				Utils.callEndTimeRequest.setRecord_for(userProfile
						.getEmail());
			}
			
			(EndOnCallActivity.this).startActivityForResult(intent,
					Constants.RESULT_CODE_CHANGE_TIME);
		}
	}

	private void executeEndShiftOrCallService() {
		if(checkOutRemember.getJobSelected().contains("shift")){
			executeShiftEndService();
		} else{
			executeEndOnCallService();
		}
	}
	
	private void saveEndCallOrEndShiftInBackLogDB(){
		if(checkOutRemember.getJobSelected().contains("shift")){
			insertShiftEndTimeIntoHoursCalculator();
			Utils.endShiftRequest.setStarted_at(Utils
					.getCurrentDateAndTime());

			User userProfile = JobViewerDBHandler.getUserProfile(this);
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(this);
			Utils.endShiftRequest.setUser_id(userProfile
					.getEmail());
			Utils.endShiftRequest
			.setReference_id(checkOutRemember.getVistecId());
			Utils.endShiftRequest.setRecord_for(userProfile
					.getEmail());
			JobViewerDBHandler.saveTimeSheet(this, Utils.endShiftRequest,					
					CommsConstant.HOST + CommsConstant.END_SHIFT_API);	
			Utils.saveTimeSheetInBackLogTable(this, Utils.endShiftRequest, CommsConstant.END_SHIFT_API, "TimeSheetServiceRequests");
		} else{
			insertCallEndTimeIntoHoursCalculator();
			Utils.callEndTimeRequest.setStarted_at(Utils
					.getCurrentDateAndTime());

			User userProfile = JobViewerDBHandler.getUserProfile(this);
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(this);
			Utils.callEndTimeRequest.setUser_id(userProfile
					.getEmail());
			Utils.callEndTimeRequest
			.setReference_id(checkOutRemember.getVistecId());
			Utils.callEndTimeRequest.setRecord_for(userProfile
					.getEmail());
			JobViewerDBHandler.saveTimeSheet(this, Utils.callEndTimeRequest,
					CommsConstant.HOST + CommsConstant.END_ON_CALL_API);	
			Utils.saveTimeSheetInBackLogTable(this, Utils.callEndTimeRequest, CommsConstant.END_ON_CALL_API, "TimeSheetServiceRequests");
		}
	}

	private void callEndCallShiftActivity() {
		Intent intent = new Intent(EndOnCallActivity.this,ShiftOrCallEndActivity.class);
		cancelAlarm();
		finish();
		startActivity(intent);
	}
	
	public void enableNextButton(boolean isEnable) {
		if (isEnable) {
			mEndOnCall.setEnabled(isEnable);
			mEndOnCall.setBackgroundResource(R.drawable.red_background);
			mEndOnCall.setOnClickListener(this);
		} else {
			mEndOnCall.setEnabled(isEnable);
			mEndOnCall.setBackgroundResource(R.drawable.dark_grey_background);
			mEndOnCall.setOnClickListener(null);
		}
	}
	
	private void executeEndOnCallService() {
		User userProfile = JobViewerDBHandler
				.getUserProfile(EndOnCallActivity.this);
		Utils.callEndTimeRequest.setUser_id(userProfile.getEmail());
		Utils.callEndTimeRequest.setRecord_for(userProfile
				.getEmail());
		Utils.callEndTimeRequest.setStarted_at(Utils.getCurrentDateAndTime());
		insertCallEndTimeIntoHoursCalculator();
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.callEndTimeRequest.getStarted_at());
		data.put("record_for", Utils.callEndTimeRequest.getRecord_for());
		data.put("is_inactive", Utils.callEndTimeRequest.getIs_inactive());
		data.put("is_overriden", Utils.callEndTimeRequest.getIs_overriden());
		data.put("override_reason",
				Utils.callEndTimeRequest.getOverride_reason());
		data.put("override_comment",
				Utils.callEndTimeRequest.getOverride_comment());
		data.put("override_timestamp",
				Utils.callEndTimeRequest.getOverride_timestamp());
		if (!Utils.isNullOrEmpty(Utils.callEndTimeRequest
				.getReference_id())) {
			data.put("reference_id", Utils.callEndTimeRequest.getReference_id());
		} else {
			data.put("reference_id","");
		}
		data.put("user_id", Utils.callEndTimeRequest.getUser_id());
		String time = "";
		if (Utils.isNullOrEmpty(Utils.callEndTimeRequest
				.getOverride_timestamp())) {
			time = Utils.callEndTimeRequest.getOverride_timestamp();
		} else {
			time = Utils.callEndTimeRequest.getStarted_at();
		}

		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.END_ON_CALL_API, data, getEndCallOrShiftHandler());

	}
	
	private void executeShiftEndService() {
		User userProfile = JobViewerDBHandler
				.getUserProfile(EndOnCallActivity.this);
		Utils.endShiftRequest.setUser_id(userProfile.getEmail());
		Utils.endShiftRequest.setRecord_for(userProfile
				.getEmail());
		Utils.endShiftRequest.setStarted_at(Utils.getCurrentDateAndTime());
		insertShiftEndTimeIntoHoursCalculator();
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.endShiftRequest.getStarted_at());
		data.put("record_for", Utils.endShiftRequest.getRecord_for());
		data.put("is_inactive", Utils.endShiftRequest.getIs_inactive());
		data.put("is_overriden", Utils.endShiftRequest.getIs_overriden());
		data.put("override_reason",
				Utils.endShiftRequest.getOverride_reason());
		data.put("override_comment",
				Utils.endShiftRequest.getOverride_comment());
		data.put("override_timestamp",
				Utils.endShiftRequest.getOverride_timestamp());
		if (!Utils.isNullOrEmpty(Utils.endShiftRequest
				.getReference_id())) {
			data.put("reference_id", Utils.endShiftRequest.getReference_id());
		} else {
			data.put("reference_id","");
		}
		data.put("user_id", Utils.endShiftRequest.getUser_id());
		String time = "";
		if (Utils.isNullOrEmpty(Utils.endShiftRequest
				.getOverride_timestamp())) {
			time = Utils.endShiftRequest.getOverride_timestamp();
		} else {
			time = Utils.endShiftRequest.getStarted_at();
		}

		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.END_SHIFT_API, data, getEndCallOrShiftHandler());

	}
	
	private Handler getEndCallOrShiftHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					// String result = (String) msg.obj;
					callEndCallShiftActivity();
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(context, exception, "Info");
					saveEndCallOrEndShiftInBackLogDB();
					break;

				default:
					break;
				}
			}
		};
		return handler;
	}
	
	private void insertShiftEndTimeIntoHoursCalculator() {
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(EndOnCallActivity.this);
		
		String timeToStore = String.valueOf(System.currentTimeMillis());
		if(Utils.endShiftRequest.getIs_overriden().equalsIgnoreCase(ActivityConstants.TRUE)){
			Log.d(Utils.LOG_TAG,"shift end time overridden");
			timeToStore = Utils.getMillisFromFormattedDate(Utils.endShiftRequest.getOverride_timestamp());
		}
		
		Log.d(Utils.LOG_TAG,"shift end time "+timeToStore);
		
		breakShiftTravelCall.setShiftEndTime(timeToStore);
		JobViewerDBHandler.saveBreakShiftTravelCall(EndOnCallActivity.this, breakShiftTravelCall);
	}
	
	private void insertCallEndTimeIntoHoursCalculator() {
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(EndOnCallActivity.this);
		String timeToStore = String.valueOf(System.currentTimeMillis());
		if(Utils.callEndTimeRequest.getIs_overriden().equalsIgnoreCase(ActivityConstants.TRUE)){
			Log.d(Utils.LOG_TAG,"call end time overridden");
			timeToStore = Utils.callEndTimeRequest.getOverride_timestamp();
		}
		Log.d(Utils.LOG_TAG,"call end time "+timeToStore);
		breakShiftTravelCall.setCallEndTime(timeToStore);
		JobViewerDBHandler.saveBreakShiftTravelCall(EndOnCallActivity.this, breakShiftTravelCall);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {	
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.RESULT_CODE_CHANGE_TIME
				&& resultCode == RESULT_OK) {
			TimeSheetRequest timeSheetRequest;
			if(checkOutRemember.getJobSelected().contains("shift")){
				timeSheetRequest = Utils.endShiftRequest;	
			} else {
				timeSheetRequest = Utils.callEndTimeRequest;
			}
			if (ActivityConstants.TRUE.equalsIgnoreCase(timeSheetRequest
					.getIs_overriden())) {
				Intent intent = new Intent(this, OverrideReasoneDialog.class);
				intent.putExtra("eventType",
						data.getExtras().getString("eventType"));
				intent.putExtra("eventType1",
						data.getExtras().getString("eventType1"));
				startActivityForResult(intent,
						Constants.RESULT_CODE_OVERRIDE_COMMENT);
			}
		} else if (requestCode == Constants.RESULT_CODE_OVERRIDE_COMMENT
				&& resultCode == RESULT_OK) {
			mOverrideStartTime.setVisibility(View.VISIBLE);

			if(checkOutRemember.getJobSelected().contains("shift")){
				mOverrideStartTime.setText(Utils.endShiftRequest
						.getOverride_timestamp() + " (User)");
			}else
				mOverrideStartTime.setText(Utils.callEndTimeRequest
					.getOverride_timestamp() + " (User)");
			

		}
		
	}
	private void cancelAlarm(){
		if(Utils.alarmMgr!=null){
			Utils.alarmMgr.cancel(ClockInConfirmationActivity.alarmIntent);
		}
	}
}
