
package com.lanesgroup.jobviewer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ShoutAboutSafetyObject;
import com.jobviewer.db.objects.StartTrainingObject;
import com.jobviewer.db.objects.SurveyJson;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.network.SendImageService;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.ConfirmDialog;
import com.jobviewer.util.ConfirmDialog.ConfirmDialogCallback;
import com.jobviewer.util.Constants;
import com.jobviewer.util.OverrideReasoneDialog;
import com.jobviewer.util.SelectActivityDialog;
import com.jobviewer.util.Utils;
import com.jobviewer.util.showTimeDialog;
import com.jobviewer.util.showTimeDialog.DialogCallback;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.fragment.QuestionsActivity;
import com.lanesgroup.jobviewer.fragment.ShoutOutActivity;
import com.vehicle.communicator.HttpConnection;

public class ActivityPageActivity extends BaseActivity implements
		View.OnClickListener, DialogCallback, ConfirmDialogCallback  {
	TextView user_email_text, date_time_text, vehicleRegistrationNumber;
	LinearLayout checked_out_layout;
	
	private ImageView mShoutAbout;
	
	private Button mStart, mCheckOutVehicle, mStartTravel, mEndOnCall;
	Context mContext;
	Bundle bundle;
	private String vehicleRegNo = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_page_screen);
		mContext = this;
		initUI();
		updateDetailsOnUI();
		Utils.startNotification(mContext);
		Intent pushIntent = new Intent(this, SendImageService.class);
		startService(pushIntent);
	}

	private void updateDetailsOnUI() {
		
		mShoutAbout = (ImageView) findViewById(R.id.shout_about_image);
		mShoutAbout.setOnClickListener(this);
		
		User userProfile = JobViewerDBHandler.getUserProfile(this);
		if(userProfile!=null){
			if (Utils.isNullOrEmpty(userProfile.getFirstname())) {
				user_email_text.setText(userProfile.getEmail());
			} else {
				user_email_text.setText(userProfile.getFirstname());
			}
		}
		
		
		String dateText = "Shift Started: "
				+ Utils.checkOutObject.getJobStartedTime()
				+ "  .  Breaks: None taken";
		date_time_text.setText(dateText);
		date_time_text.setSelected(true);
		
		vehicleRegistrationNumber.setText(vehicleRegNo);
		if (ActivityConstants.JOB_SELECTED_SHIFT.equalsIgnoreCase(Utils.checkOutObject.getJobSelected())) {
			mEndOnCall.setText("End Shift");
		} else {
			mEndOnCall.setText("End Call");
		}
		
		StartTrainingObject trainingToolBox = JobViewerDBHandler.getTrainingToolBox(this);
		if(trainingToolBox!=null){
			if(ActivityConstants.TRUE.equalsIgnoreCase(trainingToolBox.getIsTrainingStarted())){
				mStart.setTag(Constants.END_TRAINING);
				mStart.setText(Constants.END_TRAINING);
			}
		} else {			
		bundle = getIntent().getExtras();
		boolean shouldShowWorkInProgress = false;
		if (bundle != null
				&& bundle.containsKey(Utils.SHOULD_SHOW_WORK_IN_PROGRESS)) {
			shouldShowWorkInProgress = bundle
					.getBoolean(Utils.SHOULD_SHOW_WORK_IN_PROGRESS);
		}

		SurveyJson questionSet = JobViewerDBHandler.getQuestionSet(mContext);
		if (questionSet != null
				&& !Utils.isNullOrEmpty(questionSet.getQuestionJson())) {
			mStart.setText("Continue Work In Progress");
			mStart.setTag("Continue Work In Progress");
		} else if (shouldShowWorkInProgress) {
			mStart.setText("Continue Work In Progress");
			mStart.setTag("Continue Work In Progress");
		} else {
			mStart.setText(getResources().getString(R.string.start_text));
			mStart.setTag(getResources().getString(R.string.start_text));
		}
		}
		
		if(Utils.checkOutObject.getJobSelected().equalsIgnoreCase(ActivityConstants.JOB_SELECTED_SHIFT)){
			mStartTravel.setText(getResources().getString(R.string.start_break));
		}
	}

	private void initUI() {
		
		Utils.checkOutObject = JobViewerDBHandler.getCheckOutRemember(mContext);
		vehicleRegNo = Utils.checkOutObject.getVehicleRegistration();
		user_email_text = (TextView) findViewById(R.id.user_email_text);
		date_time_text = (TextView) findViewById(R.id.date_time_text);
		vehicleRegistrationNumber = (TextView) findViewById(R.id.vehicleRegistrationNumber);
		checked_out_layout = (LinearLayout) findViewById(R.id.checked_out_layout);
		mStart = (Button) findViewById(R.id.start);
		mCheckOutVehicle = (Button) findViewById(R.id.check_out_vehicle);
		mStartTravel = (Button) findViewById(R.id.start_travel);
		mEndOnCall = (Button) findViewById(R.id.end_on_call);
		mStart.setOnClickListener(this);
		mCheckOutVehicle.setOnClickListener(this);
		mStartTravel.setOnClickListener(this);
		mEndOnCall.setOnClickListener(this);
		bundle = getIntent().getExtras();
		
		if (Utils.checkOutObject == null
				|| Utils.isNullOrEmpty(Utils.checkOutObject.getMilage())) {
			checked_out_layout.setVisibility(View.GONE);
			mCheckOutVehicle.setVisibility(View.VISIBLE);
		} else {
			checked_out_layout.setVisibility(View.VISIBLE);
			mCheckOutVehicle.setVisibility(View.GONE);
			checked_out_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent checkinIntent = new Intent(v.getContext(),
							CheckInActivity.class);
					startActivity(checkinIntent);

				}
			});
		}
		
		if(bundle != null && bundle.containsKey(ActivityConstants.VEHICLE_REGISTRATION_NUMBER)){
			checked_out_layout.setVisibility(View.VISIBLE);
			mCheckOutVehicle.setVisibility(View.GONE);
			
			if(Utils.isNullOrEmpty(vehicleRegNo))
				vehicleRegNo = bundle.getString(ActivityConstants.VEHICLE_REGISTRATION_NUMBER);
			if(Utils.isNullOrEmpty(vehicleRegNo))
				vehicleRegNo = bundle.getString(ActivityConstants.VEHICLE_REGISTRATION_NUMBER);
			checked_out_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent checkinIntent = new Intent(v.getContext(),
							CheckInActivity.class);
					startActivity(checkinIntent);

				}
			});
		}
	}

	@Override
	public void onClick(View view) {
		Intent intent = new Intent();
		if (view == mStart) {
			String tag = (String) mStart.getTag();
			if ("Continue Work In Progress".equalsIgnoreCase(tag)) {
				CheckOutObject checkOutRemember = JobViewerDBHandler
						.getCheckOutRemember(mContext);
				SurveyJson questionSet = JobViewerDBHandler
						.getQuestionSet(mContext);
				if (bundle != null
						&& bundle.containsKey(Utils.CALLING_ACTIVITY)
						&& bundle.getString(Utils.CALLING_ACTIVITY)
								.equalsIgnoreCase(
										ActivityConstants.ADD_PHOTOS_ACTIVITY)) {
					Intent addPhotoScreenIntent = new Intent(mContext,
							AddPhotosActivity.class);
					Bundle addPhotoScreenIntentBundle = new Bundle();
					addPhotoScreenIntentBundle.putString(
							Utils.CALLING_ACTIVITY,
							ActivityConstants.ACTIVITY_PAGE_ACTIVITY);
					startActivity(addPhotoScreenIntent);
				} else if (questionSet != null
						&& !Utils.isNullOrEmpty(questionSet.getQuestionJson())) {
					Intent riskAssIntent = new Intent(mContext,
							QuestionsActivity.class);
					startActivity(riskAssIntent);
				} else if (!Utils.isNullOrEmpty(checkOutRemember
						.getAssessmentSelected())) {
					Intent riskAssIntent = new Intent(mContext,
							RiskAssessmentActivity.class);
					startActivity(riskAssIntent);
				} 
			} else if (Constants.END_TRAINING.equalsIgnoreCase(tag)){
				executeEndTraining();
				
			}else {
				intent.setClass(this, SelectActivityDialog.class);
				startActivityForResult(intent, Constants.RESULT_CODE_START_TRAINING);
			}
		} else if (view == mCheckOutVehicle) {
			intent.setClass(this, CheckoutVehicleActivity.class);
			intent.putExtra(Utils.CALLING_ACTIVITY, ActivityPageActivity.this.getClass().getSimpleName());
			startActivity(intent);
		} else if (view == mStartTravel) {
			Utils.startTravelTimeRequest = new TimeSheetRequest();
			if(mStartTravel.getText().toString().contains(getResources().getString(R.string.start_travel))){
				new showTimeDialog(this, this, "travel").show();
			} else if(mStartTravel.getText().toString().contains(getResources().getString(R.string.start_break))){
				Utils.timeSheetRequest = new TimeSheetRequest();
				new showTimeDialog(this, this, "start").show();
			} else if(mStartTravel.getText().toString().contains(getResources().getString(R.string.end_travel_str))){
				new showTimeDialog(this, this, getResources().getString(R.string.end_travel_str)).show();
			}
		} else if (view == mEndOnCall) {
			if(!mStart.getTag().toString().equalsIgnoreCase("Continue Work In Progress")){
				endShiftOrCall();	
			}
			
		} else if (view == mShoutAbout){
			ShoutAboutSafetyObject shoutAboutSafety = JobViewerDBHandler
					.getShoutAboutSafety(view.getContext());
			if (shoutAboutSafety != null
					&& !Utils.isNullOrEmpty(shoutAboutSafety.getQuestionSet())) {
				intent.setClass(ActivityPageActivity.this,
						ShoutOutActivity.class);
				intent.putExtra(ActivityConstants.SHOUT_OPTION,
						shoutAboutSafety.getOptionSelected());
				intent.putExtra(ActivityConstants.IS_SHOUT_SAVED,
						ActivityConstants.TRUE);
				startActivity(intent);
			} else {
				intent.setClass(ActivityPageActivity.this,
						ShoutOptionsActivity.class);
			startActivity(intent);
		}
	}
	}

	private void endShiftOrCall() {
		Intent intent;
		if(mEndOnCall.getText().toString().equals("End Call")){
			
			Utils.callEndTimeRequest = new TimeSheetRequest();
			if(mCheckOutVehicle.getVisibility()!=View.VISIBLE){
				intent = new Intent(ActivityPageActivity.this,
						EndShiftReturnVehicleActivity.class);	
				intent.putExtra("progressStep", "Step 1 of 2");
			} else{
				intent = new Intent(ActivityPageActivity.this,
						EndOnCallActivity.class);	
				intent.putExtra("progressStep", "Step 1 of 1");
				intent.putExtra("mileage", JobViewerDBHandler.getCheckOutRemember(ActivityPageActivity.this).getMilage());
			}
			intent.putExtra(Utils.END_CALL,Utils.END_CALL);
			intent.putExtra(Utils.CALLING_ACTIVITY,
					ActivityPageActivity.this.getClass().getSimpleName());
			startActivity(intent);
		} else if(mEndOnCall.getText().toString().equals("End Shift")){
			
			if(mCheckOutVehicle.getVisibility()!=View.VISIBLE){
				intent = new Intent(ActivityPageActivity.this,
						EndShiftReturnVehicleActivity.class);	
				intent.putExtra("progressStep", "Step 1 of 2");
			} else{
				intent = new Intent(ActivityPageActivity.this,
						EndOnCallActivity.class);	
				intent.putExtra("mileage", JobViewerDBHandler.getCheckOutRemember(ActivityPageActivity.this).getMilage());
				intent.putExtra("progressStep", "Step 1 of 1");
			}
			Utils.endShiftRequest = new TimeSheetRequest();
			intent.putExtra(Utils.SHIFT_END,Utils.SHIFT_END);
			intent.putExtra(Utils.CALLING_ACTIVITY,
					ActivityPageActivity.this.getClass().getSimpleName());
			startActivity(intent);
		}
	}

	private void executeEndTraining() {
		new ConfirmDialog(mContext, ActivityPageActivity.this, Constants.END_TRAINING).show();
	}

	@Override
	public void onContinue() {
		if (!Utils.isInternetAvailable(mContext)) {
			
			if(Utils.checkOutObject.getJobSelected().contains(ActivityConstants.JOB_SELECTED_SHIFT)){
				JobViewerDBHandler.saveTimeSheet(this,
						Utils.timeSheetRequest,
						CommsConstant.START_BREAK_API);
				startEndBreakActivity();
				
			} else{
				JobViewerDBHandler.saveTimeSheet(this,
						Utils.startTravelTimeRequest,
						CommsConstant.START_TRAVEL_API);
				String time = new SimpleDateFormat("HH:mm:ss dd MMM yyyy")
						.format(Calendar.getInstance().getTime());
				startEndActvity(time);
			}
		} else {
			if(Utils.checkOutObject.getJobSelected().contains(ActivityConstants.JOB_SELECTED_SHIFT)){
				executeStartBreakService();
			} else {
				executeStartTravelService();
			}
		}
	}

	@Override
	public void onDismiss() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == Constants.RESULT_CODE_CHANGE_TIME
				&& resultCode == RESULT_OK) {
			TimeSheetRequest timeSheetRequest;
			if(data.getExtras().getString("eventType").equalsIgnoreCase("start")){
				timeSheetRequest = Utils.timeSheetRequest;
			} else if (data.getExtras().getString("eventType").equalsIgnoreCase("travel")){
				timeSheetRequest = Utils.startTravelTimeRequest;
			} else {
				timeSheetRequest = Utils.endTravelTimeRequest;
			}
			
			if (ActivityConstants.TRUE.equalsIgnoreCase(timeSheetRequest
					.getIs_overriden())) {
				Intent intent = new Intent(this, OverrideReasoneDialog.class);
				intent.putExtra("eventType", data.getExtras().getString("eventType"));
				startActivityForResult(intent,
						Constants.RESULT_CODE_OVERRIDE_COMMENT);
			}
		} else if (requestCode == Constants.RESULT_CODE_OVERRIDE_COMMENT
				&& resultCode == RESULT_OK) {
			TimeSheetRequest timeSheetRequest;
			if(data.getExtras().getString("eventType").equalsIgnoreCase("start")){
				timeSheetRequest = Utils.timeSheetRequest;
			} else if (data.getExtras().getString("eventType").equalsIgnoreCase("travel")){
				timeSheetRequest = Utils.startTravelTimeRequest;
			} else {
				timeSheetRequest = Utils.endTravelTimeRequest;
			}
			if (!Utils.isInternetAvailable(this)) {
				if(data.getExtras().getString("eventType").equalsIgnoreCase("start")){
					JobViewerDBHandler.saveTimeSheet(this, timeSheetRequest,
							CommsConstant.START_BREAK_API);
					JobViewerDBHandler.getAllTimeSheet(mContext);
					Utils.saveTimeSheetInBackLogTable(ActivityPageActivity.this,
							timeSheetRequest, CommsConstant.START_BREAK_API,
							Utils.REQUEST_TYPE_WORK);
				}else if (data.getExtras().getString("eventType").equalsIgnoreCase("travel")){
					JobViewerDBHandler.saveTimeSheet(this, timeSheetRequest,
							CommsConstant.START_TRAVEL_API);
					JobViewerDBHandler.getAllTimeSheet(mContext);
					Utils.saveTimeSheetInBackLogTable(ActivityPageActivity.this,
							timeSheetRequest, CommsConstant.START_TRAVEL_API,
							Utils.REQUEST_TYPE_WORK);
				} else{
					JobViewerDBHandler.saveTimeSheet(this, timeSheetRequest,
							CommsConstant.END_TRAVEL_API);
					JobViewerDBHandler.getAllTimeSheet(mContext);
					Utils.saveTimeSheetInBackLogTable(ActivityPageActivity.this,
							timeSheetRequest, CommsConstant.END_TRAVEL_API,
							Utils.REQUEST_TYPE_WORK);
				}
				
				
				// saveStartBreakinToBackLogDb();
				if(Utils.checkOutObject.getJobSelected().contains(ActivityConstants.JOB_SELECTED_SHIFT)){
					startEndBreakActivity();
				}else{				
					startEndActvity(Utils.startTravelTimeRequest.getOverride_timestamp());
				}
			} else {
				Utils.startProgress(ActivityPageActivity.this);
				if(data.getExtras().getString("eventType").equalsIgnoreCase("start")){	
					executeStartBreakService();
				} else if(data.getExtras().getString("eventType").equalsIgnoreCase("travel")){
					executeStartTravelService();
				} else {
					Utils.StopProgress();
					startEndActvity(Utils.endTravelTimeRequest.getOverride_timestamp());
				}
			}
		} else if(requestCode == Constants.RESULT_CODE_START_TRAINING && resultCode == RESULT_OK){
			mStart.setText(Constants.END_TRAINING);
			mStart.setTag(Constants.END_TRAINING);
		}
	}

	private void startEndActvity(String time) {
		Intent intent = new Intent(this, EndTravelActivity.class);
		intent.putExtra("eventType", "End Travel");
		intent.putExtra(Constants.TIME, time);
		startActivity(intent);
	}

	private void startEndBreakActivity(){
		Intent intent = new Intent(this, EndBreakActivity.class);
		intent.putExtra("eventType", "End Break");		
		intent.putExtra(Constants.TIME, Utils.getCurrentDateAndTime());
		startActivity(intent);
	}
	
	private void executeStartBreakService() {
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.timeSheetRequest.getStarted_at());
		data.put("record_for", Utils.timeSheetRequest.getRecord_for());
		data.put("is_inactive", Utils.timeSheetRequest.getIs_inactive());
		data.put("is_overriden", Utils.timeSheetRequest.getIs_overriden());
		data.put("override_reason", Utils.timeSheetRequest.getOverride_reason());
		data.put("override_comment",
				Utils.timeSheetRequest.getOverride_comment());
		data.put("override_timestamp",
				Utils.timeSheetRequest.getOverride_timestamp());
		data.put("reference_id", Utils.timeSheetRequest.getReference_id());
		data.put("user_id", Utils.timeSheetRequest.getUser_id());
		String time = "";
		if (Utils.isNullOrEmpty(Utils.timeSheetRequest.getOverride_timestamp())) {
			time = Utils.timeSheetRequest.getOverride_timestamp();
		} else {
			time = Utils.timeSheetRequest.getStarted_at();
		}

		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.START_BREAK_API, data,
				getStartBreakHandler(time));

	}

	private Handler getStartBreakHandler(final String time) {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					startEndBreakActivity();
					// String result = (String) msg.obj;
					//startEndActvity(time);
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(mContext, exception, "Info");
					Utils.saveTimeSheetInBackLogTable(
							ActivityPageActivity.this, Utils.timeSheetRequest,
							CommsConstant.START_BREAK_API,
							Utils.REQUEST_TYPE_WORK);
					// saveStartBreakinToBackLogDb();
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}
	
	@Override
	public void onBackPressed() {
		exitApplication(this);
	}

	@Override
	public void onConfirmStartTraining() {
		JobViewerDBHandler.deleteStartTraining(mContext);
		mStart.setText("Start...");
		mStart.setTag(Constants.START_TRAINING);
	}

	@Override
	public void onConfirmDismiss() {
		
	}
	
	private void executeStartTravelService() {
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.startTravelTimeRequest.getStarted_at());
		data.put("record_for", Utils.startTravelTimeRequest.getRecord_for());
		data.put("is_inactive", Utils.startTravelTimeRequest.getIs_inactive());
		data.put("is_overriden", Utils.startTravelTimeRequest.getIs_overriden());
		data.put("override_reason",
				Utils.startTravelTimeRequest.getOverride_reason());
		data.put("override_comment",
				Utils.startTravelTimeRequest.getOverride_comment());
		data.put("override_timestamp",
				Utils.startTravelTimeRequest.getOverride_timestamp());
		data.put("reference_id", Utils.startTravelTimeRequest.getReference_id());
		data.put("user_id", Utils.startTravelTimeRequest.getUser_id());
		Utils.startProgress(this);
		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.START_TRAVEL_API, data, getStartTravelHandler());

	}

	private Handler getStartTravelHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					// String result = (String) msg.obj;
					CheckOutObject checkOutRemember = JobViewerDBHandler
							.getCheckOutRemember(mContext);
					checkOutRemember.setIsStartedTravel("true");
					JobViewerDBHandler.saveCheckOutRemember(mContext,
							checkOutRemember);
					
					startEndActvity(Utils.getCurrentDateAndTime());
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(mContext, exception, "Info");
					Utils.saveTimeSheetInBackLogTable(
							ActivityPageActivity.this,
							Utils.startTravelTimeRequest,
							CommsConstant.START_TRAVEL_API,
							Utils.REQUEST_TYPE_WORK);
					// saveStartTravelInBackLogDb();
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

}
