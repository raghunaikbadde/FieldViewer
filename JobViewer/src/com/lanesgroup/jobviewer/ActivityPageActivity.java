package com.lanesgroup.jobviewer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.SurveyJson;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.network.SendImageService;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.OverrideReasoneDialog;
import com.jobviewer.util.SelectActivityDialog;
import com.jobviewer.util.Utils;
import com.jobviewer.util.showTimeDialog;
import com.jobviewer.util.showTimeDialog.DialogCallback;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.fragment.QuestionsActivity;
import com.vehicle.communicator.HttpConnection;

public class ActivityPageActivity extends Activity implements
		View.OnClickListener, DialogCallback {
	TextView user_email_text, date_time_text, vehicleRegistrationNumber;
	LinearLayout checked_out_layout;
	
	private ImageView mShoutAbout;
	
	private Button mStart, mCheckOutVehicle, mStartTravel, mEndOnCall;
	Context mContext;
	Bundle bundle;

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
		if (Utils.isNullOrEmpty(userProfile.getFirstname())) {
			user_email_text.setText(userProfile.getEmail());
		} else {
			user_email_text.setText(userProfile.getFirstname());
		}
		if (Utils.checkOutObject != null) {
			Utils.checkOutObject = JobViewerDBHandler
					.getCheckOutRemember(mContext);
		}
		String dateText = "Shift Started: "
				+ Utils.checkOutObject.getJobStartedTime()
				+ "  .  Breaks: None taken";
		date_time_text.setText(dateText);
		date_time_text.setSelected(true);
		vehicleRegistrationNumber.setText(Utils.checkOutObject
				.getVehicleRegistration());
		if ("shift".equalsIgnoreCase(Utils.checkOutObject.getJobSelected())) {
			mEndOnCall.setText("End Shift");
		} else {
			mEndOnCall.setText("End Call");
		}

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

	private void initUI() {
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
						&& bundle
								.containsKey(Utils.CALLING_ACTIVITY) && bundle.getString(Utils.CALLING_ACTIVITY).equalsIgnoreCase(ActivityConstants.ADD_PHOTOS_ACTIVITY)) {					
					Intent addPhotoScreenIntent = new Intent(mContext,
							AddPhotosActivity.class);
					Bundle addPhotoScreenIntentBundle = new Bundle();
					addPhotoScreenIntentBundle.putString(Utils.CALLING_ACTIVITY, ActivityConstants.ACTIVITY_PAGE_ACTIVITY);
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
			} else {
				intent.setClass(this, SelectActivityDialog.class);
				startActivity(intent);
			}
		} else if (view == mCheckOutVehicle) {
			intent.setClass(this, CheckoutVehicleActivity.class);
			startActivity(intent);
		} else if (view == mStartTravel) {
			Utils.startTravelTimeRequest = new TimeSheetRequest();
			new showTimeDialog(this, this, "travel").show();
		} else if (view == mEndOnCall) {

		} else if (view == mShoutAbout){
			intent.setClass(ActivityPageActivity.this, ShoutOptionsActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onContinue() {
		if (!Utils.isInternetAvailable(mContext)) {
			JobViewerDBHandler.saveTimeSheet(this,
					Utils.startTravelTimeRequest,
					CommsConstant.START_TRAVEL_API);
			String time = new SimpleDateFormat("HH:mm:ss dd MMM yyyy")
					.format(Calendar.getInstance().getTime());

			startEndActvity(time);
		} else {
			executeStartBreakService();
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
			if (ActivityConstants.TRUE.equalsIgnoreCase(Utils.timeSheetRequest
					.getIs_overriden())) {
				Intent intent = new Intent(this, OverrideReasoneDialog.class);
				startActivityForResult(intent,
						Constants.RESULT_CODE_OVERRIDE_COMMENT);
			}
		} else if (requestCode == Constants.RESULT_CODE_OVERRIDE_COMMENT
				&& resultCode == RESULT_OK) {

			if (!Utils.isInternetAvailable(this)) {
				JobViewerDBHandler.saveTimeSheet(this, Utils.timeSheetRequest,
						CommsConstant.START_BREAK_API);
				JobViewerDBHandler.getAllTimeSheet(mContext);
				Utils.saveTimeSheetInBackLogTable(ActivityPageActivity.this,
						Utils.timeSheetRequest, CommsConstant.START_BREAK_API,
						Utils.REQUEST_TYPE_WORK);
				// saveStartBreakinToBackLogDb();
				startEndActvity(Utils.timeSheetRequest.getOverride_timestamp());
			} else {
				Utils.startProgress(ActivityPageActivity.this);
				executeStartBreakService();
			}
		}
	}

	private void startEndActvity(String time) {
		Intent intent = new Intent(this, EndTravelActivity.class);
		intent.putExtra(Constants.STARTED, Constants.TRAVEL_STARTED);
		intent.putExtra(Constants.TIME, time);
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
					// String result = (String) msg.obj;
					startEndActvity(time);
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

}
