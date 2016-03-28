package com.lanesgroup.jobviewer;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;
import com.vehicle.communicator.HttpConnection;

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
			mClockIn.setBackgroundDrawable(ResourcesCompat.getDrawable(
					getResources(), R.drawable.red_background, null));
			mClockIn.setOnClickListener(this);
			mProgress.setProgress(8);
		} else {
			mClockIn.setBackgroundDrawable(ResourcesCompat.getDrawable(
					getResources(), R.drawable.dark_grey_background, null));
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
			BackLogRequest backLogRequest = new BackLogRequest();
			if(!Utils.isInternetAvailable(ClockInConfirmationActivity.this)){
				if (mCallingActivity.equalsIgnoreCase("WelcomeActivity"))
					Utils.saveTimeSheetInBackLogTable(ClockInConfirmationActivity.this, Utils.callStartTimeRequest, CommsConstant.START_ON_CALL_API, Utils.REQUEST_TYPE_TIMESHEET);
				else if(mCallingActivity.equalsIgnoreCase("ClockInActivity"))
					Utils.saveTimeSheetInBackLogTable(ClockInConfirmationActivity.this, Utils.startShiftTimeRequest, CommsConstant.START_SHIFT_API, Utils.REQUEST_TYPE_TIMESHEET);
				Intent intent = new Intent(ClockInConfirmationActivity.this,
						ActivityPageActivity.class);
				startActivity(intent);
			}
			else{
				if (mCallingActivity.equalsIgnoreCase("WelcomeActivity"))
					executeOnCallStartService();
				else if(mCallingActivity.equalsIgnoreCase("ClockInActivity"))
					executeStartShiftService();
			}
				
			//JobViewerDBHandler.saveBackLog(ClockInConfirmationActivity.this, request);
			
		}
	}

	private void executeStartShiftService() {
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.startShiftTimeRequest.getStarted_at());
		data.put("record_for", Utils.startShiftTimeRequest.getRecord_for());
		data.put("is_inactive", Utils.startShiftTimeRequest.getIs_inactive());
		data.put("is_overriden", Utils.startShiftTimeRequest.getIs_overriden());
		data.put("override_reason",
				Utils.startShiftTimeRequest.getOverride_reason());
		data.put("override_comment",
				Utils.startShiftTimeRequest.getOverride_comment());
		data.put("override_timestamp",
				Utils.startShiftTimeRequest.getOverride_timestamp());
		data.put("reference_id", Utils.startShiftTimeRequest.getReference_id());
		data.put("user_id", Utils.startShiftTimeRequest.getUser_id());
		String time = "";
		if (Utils.isNullOrEmpty(Utils.startShiftTimeRequest
				.getOverride_timestamp())) {
			time = Utils.startShiftTimeRequest.getOverride_timestamp();
		} else {
			time = Utils.startShiftTimeRequest.getStarted_at();
		}

		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.START_SHIFT_API, data, getStartShiftHandler());

	}

	private void executeOnCallStartService() {
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.callStartTimeRequest.getStarted_at());
		data.put("record_for", Utils.callStartTimeRequest.getRecord_for());
		data.put("is_inactive", Utils.callStartTimeRequest.getIs_inactive());
		data.put("is_overriden", Utils.callStartTimeRequest.getIs_overriden());
		data.put("override_reason",
				Utils.callStartTimeRequest.getOverride_reason());
		data.put("override_comment",
				Utils.callStartTimeRequest.getOverride_comment());
		data.put("override_timestamp",
				Utils.callStartTimeRequest.getOverride_timestamp());
		data.put("reference_id", Utils.callStartTimeRequest.getReference_id());
		data.put("user_id", Utils.callStartTimeRequest.getUser_id());
		String time = "";
		if (Utils.isNullOrEmpty(Utils.callStartTimeRequest
				.getOverride_timestamp())) {
			time = Utils.callStartTimeRequest.getOverride_timestamp();
		} else {
			time = Utils.callStartTimeRequest.getStarted_at();
		}

		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.START_ON_CALL_API, data, getStartShiftHandler());

	}

	private Handler getStartShiftHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					// String result = (String) msg.obj;
					Intent intent = new Intent(
							ClockInConfirmationActivity.this,
							ActivityPageActivity.class);
					intent.putExtra(Utils.CALLING_ACTIVITY,
							ClockInConfirmationActivity.this.getClass()
									.getSimpleName());
					startActivity(intent);
					break;
				case HttpConnection.DID_ERROR:
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(context, exception, "Info");
					if (mCallingActivity.equalsIgnoreCase("WelcomeActivity"))
						Utils.saveTimeSheetInBackLogTable(
								ClockInConfirmationActivity.this,
								Utils.callStartTimeRequest,
								CommsConstant.START_ON_CALL_API,
								Utils.REQUEST_TYPE_TIMESHEET);
					else if (mCallingActivity
							.equalsIgnoreCase("ClockInActivity"))
						Utils.saveTimeSheetInBackLogTable(
								ClockInConfirmationActivity.this,
								Utils.startShiftTimeRequest,
								CommsConstant.START_SHIFT_API,
								Utils.REQUEST_TYPE_TIMESHEET);
					break;

				default:
					break;
				}
			}
		};
		return handler;
	}
}