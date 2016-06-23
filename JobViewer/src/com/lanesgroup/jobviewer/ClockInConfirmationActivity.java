package com.lanesgroup.jobviewer;

import java.text.NumberFormat;
import java.util.Locale;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
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
import com.jobviewer.util.showTimeDialog.DialogCallback;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.User;
import com.jobviwer.service.OverTimeAlertService;
import com.vehicle.communicator.HttpConnection;

public class ClockInConfirmationActivity extends BaseActivity implements
		OnClickListener, DialogCallback {

	private ProgressBar mProgress;
	private TextView mProgressStep, mShiftStartTime, mUserEmail, mDivider,
			mVehicleUsed, mMileage, mOverrideStartTime;
	private ImageView mEditTime;
	private CheckBox mCheckBox;
	private Button mBack, mClockIn;
	private String mCallingActivity;
	private Context mContext;
	public static PendingIntent alarmIntent;
	private String eventType;
	private final String CALLING_ACTIVITY = "callingActivity";
	private boolean shouldCallActivityPageActivity = true;
	private TextView clockin_text;
	private TextView shift_start_time_text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clock_in_no_screen);
		if (Utils.checkOutObject == null) {
			Utils.checkOutObject = JobViewerDBHandler.getCheckOutRemember(this);
		}
		Log.d(Utils.LOG_TAG, "ClockInConfirmationActivity");
		initUI();
		initiateAlarm();
	}

	private void initUI() {
		mContext = this;
		clockin_text = (TextView) findViewById(R.id.clockin_text);
		shift_start_time_text = (TextView) findViewById(R.id.shift_start_time_text);
		mCallingActivity = getIntent().getExtras().get(CALLING_ACTIVITY)
				.toString();
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mProgressStep = (TextView) findViewById(R.id.progress_step_text);
		mShiftStartTime = (TextView) findViewById(R.id.date_time_text);

		mOverrideStartTime = (TextView) findViewById(R.id.overrided_date_time_text);
		mEditTime = (ImageView) findViewById(R.id.edit_date);
		mEditTime.setOnClickListener(this);

		mUserEmail = (TextView) findViewById(R.id.user_email_text);
		mDivider = (TextView) findViewById(R.id.stroke_text2);
		mVehicleUsed = (TextView) findViewById(R.id.vehicle_used_text);
		mMileage = (TextView) findViewById(R.id.mileage_text);
		mProgress.setMax(8);
		mProgress.setProgress(7);
		mShiftStartTime.setText(Utils.getCurrentDateAndTime() + " (System)");		
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
			mProgressStep.setText(Utils.PROGRESS_3_TO_3);
			mVehicleUsed.setVisibility(View.VISIBLE);
			mMileage.setVisibility(View.VISIBLE);
			Bundle extras = getIntent().getExtras();
			if (extras != null
					&& extras
							.containsKey(ActivityConstants.VEHICLE_REGISTRATION_NUMBER)
					&& extras.containsKey(ActivityConstants.VEHICLE_MILEAGE)) {
				String mileage = extras
						.getString(ActivityConstants.VEHICLE_MILEAGE);
				NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
				mileage = numberFormat.format(Double.valueOf(mileage));
				mMileage.setText(extras
						.getString(ActivityConstants.VEHICLE_REGISTRATION_NUMBER)
						+ " (mileage " + mileage + ")");
			} else {
				mMileage.setText(Utils.checkOutObject.getVehicleRegistration()
						+ " (mileage " + Utils.checkOutObject.getMilage() + ")");
				mDivider.setVisibility(View.VISIBLE);
			}
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

		if (Utils.checkOutObject != null
				&& Utils.checkOutObject.getJobSelected() != null
				&& !Utils.checkOutObject.getJobSelected().equals("")) {
			if (Utils.checkOutObject.getJobSelected().equals(
					ActivityConstants.JOB_SELECTED_ON_CALL)) {
				clockin_text.setText(context.getString(R.string.start_on_call));
				shift_start_time_text.setText(context.getString(R.string.on_call_start_time));
				mClockIn.setText(context.getString(R.string.start_on_call));
			} else {

			}
		}
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
			Bundle bundle = getIntent().getExtras();
			BackLogRequest backLogRequest = new BackLogRequest();
			User userProfile = JobViewerDBHandler.getUserProfile(view
					.getContext());
			if (!Utils.isInternetAvailable(ClockInConfirmationActivity.this)) {
				if (mCallingActivity.equalsIgnoreCase("WelcomeActivity")) {
					if (bundle != null && bundle.containsKey(Utils.CALL_START)) {
						Utils.callStartTimeRequest.setUser_id(userProfile
								.getEmail());
						Utils.callStartTimeRequest.setRecord_for(userProfile
								.getEmail());
						Utils.callStartTimeRequest.setStarted_at(Utils
								.getCurrentDateAndTime());
						insertCallStartTimeIntoHoursCalculator();
						Utils.saveTimeSheetInBackLogTable(
								ClockInConfirmationActivity.this,
								Utils.callStartTimeRequest,
								CommsConstant.START_ON_CALL_API,
								Utils.REQUEST_TYPE_TIMESHEET);
					} else if (bundle != null
							&& bundle.containsKey(Utils.SHIFT_START)) {
						Utils.startShiftTimeRequest.setUser_id(userProfile
								.getEmail());
						Utils.startShiftTimeRequest.setRecord_for(userProfile
								.getEmail());
						Utils.startShiftTimeRequest.setStarted_at(Utils
								.getCurrentDateAndTime());
						insertShiftStartTimeIntoHoursCalculator();
						Utils.saveTimeSheetInBackLogTable(
								ClockInConfirmationActivity.this,
								Utils.startShiftTimeRequest,
								CommsConstant.START_SHIFT_API,
								Utils.REQUEST_TYPE_TIMESHEET);
					}
				} else if (mCallingActivity.equalsIgnoreCase("ClockInActivity")) {
					Utils.startShiftTimeRequest.setUser_id(userProfile
							.getEmail());
					Utils.startShiftTimeRequest.setRecord_for(userProfile
							.getEmail());
					Utils.startShiftTimeRequest.setStarted_at(Utils
							.getCurrentDateAndTime());
					insertShiftStartTimeIntoHoursCalculator();
					Utils.saveTimeSheetInBackLogTable(
							ClockInConfirmationActivity.this,
							Utils.startShiftTimeRequest,
							CommsConstant.START_SHIFT_API,
							Utils.REQUEST_TYPE_TIMESHEET);
				} else if (mCallingActivity
						.equalsIgnoreCase("CheckoutVehicleActivity")) {
					Utils.startShiftTimeRequest.setUser_id(userProfile
							.getEmail());
					Utils.startShiftTimeRequest.setRecord_for(userProfile
							.getEmail());
					Utils.startShiftTimeRequest.setStarted_at(Utils
							.getCurrentDateAndTime());
					insertShiftStartTimeIntoHoursCalculator();
					Utils.saveTimeSheetInBackLogTable(
							ClockInConfirmationActivity.this,
							Utils.startShiftTimeRequest,
							CommsConstant.START_SHIFT_API,
							Utils.REQUEST_TYPE_TIMESHEET);
				} else if (mCallingActivity
						.equalsIgnoreCase("ActivityPageActivity")) {
					if (bundle != null && bundle.containsKey(Utils.END_CALL)) {
						Utils.callEndTimeRequest.setUser_id(userProfile
								.getEmail());
						Utils.callEndTimeRequest.setRecord_for(userProfile
								.getEmail());
						Utils.callEndTimeRequest.setStarted_at(Utils
								.getCurrentDateAndTime());
						Utils.saveTimeSheetInBackLogTable(
								ClockInConfirmationActivity.this,
								Utils.callEndTimeRequest,
								CommsConstant.END_ON_CALL_API,
								Utils.REQUEST_TYPE_TIMESHEET);
					} else if (bundle != null
							&& bundle.containsKey(Utils.SHIFT_END)) {
						Utils.endShiftRequest
								.setUser_id(userProfile.getEmail());
						Utils.endShiftRequest.setRecord_for(userProfile
								.getEmail());
						Utils.endShiftRequest.setStarted_at(Utils
								.getCurrentDateAndTime());
						Utils.saveTimeSheetInBackLogTable(
								ClockInConfirmationActivity.this,
								Utils.endShiftRequest,
								CommsConstant.END_SHIFT_API,
								Utils.REQUEST_TYPE_TIMESHEET);
					}
					shouldCallActivityPageActivity = false;
					Intent intent = new Intent(
							ClockInConfirmationActivity.this,
							ShiftOrCallEndActivity.class);
					cancelAlarm();
					startActivity(intent);
				}
				if (shouldCallActivityPageActivity) {
					Intent intent = new Intent(
							ClockInConfirmationActivity.this,
							ActivityPageActivity.class);
					setAlarmForOverTime();
					putVehicleRegistrationNumberInIntent(intent);
					startActivity(intent);
				}
			} else {
				if (mCallingActivity.equalsIgnoreCase("WelcomeActivity")) {
					if (bundle != null && bundle.containsKey(Utils.CALL_START)) {
						executeOnCallStartService();
					} else if (bundle != null
							&& bundle.containsKey(Utils.SHIFT_START)) {
						executeStartShiftService();
					}
				} else if (mCallingActivity.equalsIgnoreCase("ClockInActivity")) {
					executeStartShiftService();
				} else if (mCallingActivity
						.equalsIgnoreCase("CheckoutVehicleActivity")) {
					executeStartShiftService();
				} else if (mCallingActivity
						.equalsIgnoreCase("ActivityPageActivity")) {
					if (bundle != null && bundle.containsKey(Utils.END_CALL)) {
						executeCallEndService();
					} else if (bundle != null
							&& bundle.containsKey(Utils.SHIFT_END)) {
						executeShiftEndService();
					}
				}
			}
			// JobViewerDBHandler.saveBackLog(ClockInConfirmationActivity.this,
			// request);

		} else if (view == mEditTime) {
			// new showTimeDialog(this, this, "ClockIn").show();
			Intent intent = new Intent(mContext, ChangeTimeDialog.class);
			intent.putExtra("eventType", "ClockIn");
			intent.putExtra("eventType1", Utils.SHIFT_START);
			Bundle bundle = getIntent().getExtras();
			if (bundle != null && bundle.containsKey(Utils.CALL_START)) {
				intent.putExtra("eventType1", Utils.CALL_START);
				Utils.callStartTimeRequest.setStarted_at(Utils
						.getCurrentDateAndTime());
				Utils.callStartTimeRequest.setIs_overriden("true");
				User userProfile = JobViewerDBHandler.getUserProfile(this);
				CheckOutObject checkOutRemember = JobViewerDBHandler
						.getCheckOutRemember(this);
				if (userProfile != null && checkOutRemember != null) {
					Utils.callStartTimeRequest.setUser_id(userProfile
							.getEmail());
					if (!Utils.isNullOrEmpty(checkOutRemember.getVistecId())) {
						Utils.callStartTimeRequest
								.setReference_id(checkOutRemember.getVistecId());
					}
					Utils.callStartTimeRequest.setRecord_for(userProfile
							.getEmail());
				}
			} else if (bundle != null && bundle.containsKey(Utils.SHIFT_START)) {
				intent.putExtra("eventType1", Utils.SHIFT_START);
				Utils.startShiftTimeRequest.setStarted_at(Utils
						.getCurrentDateAndTime());
				Utils.startShiftTimeRequest.setIs_overriden("true");
				User userProfile = JobViewerDBHandler.getUserProfile(this);
				CheckOutObject checkOutRemember = JobViewerDBHandler
						.getCheckOutRemember(this);
				if (userProfile != null && checkOutRemember != null) {
					Utils.startShiftTimeRequest.setUser_id(userProfile
							.getEmail());
					if (!Utils.isNullOrEmpty(checkOutRemember.getVistecId())) {
						Utils.startShiftTimeRequest
								.setReference_id(checkOutRemember.getVistecId());
					}
					Utils.startShiftTimeRequest.setRecord_for(userProfile
							.getEmail());
				}
			}

			((Activity) mContext).startActivityForResult(intent,
					Constants.RESULT_CODE_CHANGE_TIME);
		}
	}

	private void insertShiftStartTimeIntoHoursCalculator() {
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler
				.getBreakShiftTravelCall(ClockInConfirmationActivity.this);
		if (breakShiftTravelCall == null) {
			breakShiftTravelCall = new BreakShiftTravelCall();
		}
		String isOVerridden = Utils.startShiftTimeRequest.getIs_overriden();
		String timeToStore = Utils
				.getMillisFromFormattedDate(Utils.startShiftTimeRequest
						.getStarted_at());
		if (isOVerridden.equalsIgnoreCase(ActivityConstants.TRUE)) {
			Log.d(Utils.LOG_TAG, "shift start time overriden");
			timeToStore = Utils
					.getMillisFromFormattedDate(Utils.startShiftTimeRequest
							.getOverride_timestamp());
		}
		Log.d(Utils.LOG_TAG, "shift start time " + timeToStore);
		breakShiftTravelCall.setShiftStartTime(timeToStore);

		JobViewerDBHandler.saveBreakShiftTravelCall(
				ClockInConfirmationActivity.this, breakShiftTravelCall);
	}

	private void insertCallStartTimeIntoHoursCalculator() {
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler
				.getBreakShiftTravelCall(ClockInConfirmationActivity.this);

		if (breakShiftTravelCall == null) {
			breakShiftTravelCall = new BreakShiftTravelCall();
		}
		String timeToStore = Utils
				.getMillisFromFormattedDate(Utils.callStartTimeRequest
						.getStarted_at());
		if (Utils.callStartTimeRequest.getIs_overriden().equalsIgnoreCase(
				ActivityConstants.TRUE)) {
			Log.d(Utils.LOG_TAG, "call Start Time Overriden");
			timeToStore = Utils
					.getMillisFromFormattedDate(Utils.callStartTimeRequest
							.getOverride_timestamp());
		}
		breakShiftTravelCall.setCallStartTime(timeToStore);
		Log.d(Utils.LOG_TAG, "call start time " + timeToStore);
		JobViewerDBHandler.saveBreakShiftTravelCall(
				ClockInConfirmationActivity.this, breakShiftTravelCall);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.RESULT_CODE_CHANGE_TIME
				&& resultCode == RESULT_OK) {
			String eventType1 = data.getExtras().getString("eventType1");
			TimeSheetRequest timeSheetRequest;
			if (eventType1.equalsIgnoreCase(Utils.SHIFT_START)) {
				timeSheetRequest = Utils.startShiftTimeRequest;
			} else {
				timeSheetRequest = Utils.callStartTimeRequest;
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
			String eventType1 = data.getExtras().getString("eventType1");
			mOverrideStartTime.setVisibility(View.VISIBLE);
			if (Utils.SHIFT_START.equalsIgnoreCase(eventType1)) {
				mOverrideStartTime.setText(Utils.startShiftTimeRequest
						.getOverride_timestamp() + " (User)");
				mShiftStartTime.setTextColor(this.getResources().getColor(R.color.grey));
			} else {
				mOverrideStartTime.setText(Utils.callStartTimeRequest
						.getOverride_timestamp() + " (User)");
				mShiftStartTime.setTextColor(this.getResources().getColor(R.color.grey));
			}

		} else if (requestCode == Constants.RESULT_CODE_CLOCK_IN
				&& resultCode == RESULT_OK) {
			String time = data.getExtras().get(Constants.TIME).toString();
			mOverrideStartTime.setVisibility(View.VISIBLE);
			mShiftStartTime.setTextColor(this.getResources().getColor(R.color.grey));
			mOverrideStartTime.setText(time + " (User)");
		}
	}

	private void putVehicleRegistrationNumberInIntent(Intent intent) {
		Bundle extras = getIntent().getExtras();
		if (extras != null
				&& extras
						.containsKey(ActivityConstants.VEHICLE_REGISTRATION_NUMBER)) {
			intent.putExtra(
					ActivityConstants.VEHICLE_REGISTRATION_NUMBER,
					extras.getString(ActivityConstants.VEHICLE_REGISTRATION_NUMBER));

			Utils.checkOutObject.setVehicleRegistration(extras
					.getString(ActivityConstants.VEHICLE_REGISTRATION_NUMBER));
			Utils.checkOutObject.setMilage(extras
					.getString(ActivityConstants.VEHICLE_MILEAGE));
			JobViewerDBHandler.saveCheckOutRemember(
					ClockInConfirmationActivity.this, Utils.checkOutObject);

		}
	}

	private void executeStartShiftService() {
		Utils.startProgress(ClockInConfirmationActivity.this);
		User userProfile = JobViewerDBHandler
				.getUserProfile(ClockInConfirmationActivity.this);
		Utils.startShiftTimeRequest.setUser_id(userProfile.getEmail());
		Utils.startShiftTimeRequest.setRecord_for(userProfile.getEmail());
		Utils.startShiftTimeRequest
				.setStarted_at(Utils.getCurrentDateAndTime());
		insertShiftStartTimeIntoHoursCalculator();
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
		Utils.startProgress(ClockInConfirmationActivity.this);
		User userProfile = JobViewerDBHandler
				.getUserProfile(ClockInConfirmationActivity.this);
		Utils.callStartTimeRequest.setUser_id(userProfile.getEmail());
		Utils.callStartTimeRequest.setRecord_for(userProfile.getEmail());
		Utils.callStartTimeRequest.setStarted_at(Utils.getCurrentDateAndTime());
		insertCallStartTimeIntoHoursCalculator();
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

	private void executeCallEndService() {
		Utils.startProgress(ClockInConfirmationActivity.this);
		User userProfile = JobViewerDBHandler
				.getUserProfile(ClockInConfirmationActivity.this);
		Utils.callEndTimeRequest.setUser_id(userProfile.getEmail());
		Utils.callEndTimeRequest.setRecord_for(userProfile.getEmail());
		Utils.callEndTimeRequest.setStarted_at(Utils.getCurrentDateAndTime());
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
		data.put("reference_id", Utils.callEndTimeRequest.getReference_id());
		data.put("user_id", Utils.callEndTimeRequest.getUser_id());
		String time = "";
		if (Utils.isNullOrEmpty(Utils.callEndTimeRequest
				.getOverride_timestamp())) {
			time = Utils.callEndTimeRequest.getOverride_timestamp();
		} else {
			time = Utils.callEndTimeRequest.getStarted_at();
		}

		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.END_ON_CALL_API, data,
				getEndCallOrShiftHandler());

	}

	private void executeShiftEndService() {
		Utils.startProgress(ClockInConfirmationActivity.this);
		User userProfile = JobViewerDBHandler
				.getUserProfile(ClockInConfirmationActivity.this);
		Utils.endShiftRequest.setUser_id(userProfile.getEmail());
		Utils.endShiftRequest.setRecord_for(userProfile.getEmail());
		Utils.endShiftRequest.setStarted_at(Utils.getCurrentDateAndTime());
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.endShiftRequest.getStarted_at());
		data.put("record_for", Utils.endShiftRequest.getRecord_for());
		data.put("is_inactive", Utils.endShiftRequest.getIs_inactive());
		data.put("is_overriden", Utils.endShiftRequest.getIs_overriden());
		data.put("override_reason", Utils.endShiftRequest.getOverride_reason());
		data.put("override_comment",
				Utils.endShiftRequest.getOverride_comment());
		data.put("override_timestamp",
				Utils.endShiftRequest.getOverride_timestamp());
		data.put("reference_id", Utils.endShiftRequest.getReference_id());
		data.put("user_id", Utils.endShiftRequest.getUser_id());
		String time = "";
		if (Utils.isNullOrEmpty(Utils.endShiftRequest.getOverride_timestamp())) {
			time = Utils.endShiftRequest.getOverride_timestamp();
		} else {
			time = Utils.endShiftRequest.getStarted_at();
		}

		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.END_SHIFT_API, data, getEndCallOrShiftHandler());

	}

	private Handler getStartShiftHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					// String result = (String) msg.obj;
					Intent intent = new Intent(
							ClockInConfirmationActivity.this,
							ActivityPageActivity.class);
					setAlarmForOverTime();
					putVehicleRegistrationNumberInIntent(intent);
					intent.putExtra(Utils.CALLING_ACTIVITY,
							ClockInConfirmationActivity.this.getClass()
									.getSimpleName());
					startActivity(intent);
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);

					ExceptionHandler.showException(context, exception, "Info");
					if (mCallingActivity.equalsIgnoreCase("WelcomeActivity")) {
						insertCallStartTimeIntoHoursCalculator();
						Utils.saveTimeSheetInBackLogTable(
								ClockInConfirmationActivity.this,
								Utils.callStartTimeRequest,
								CommsConstant.START_ON_CALL_API,
								Utils.REQUEST_TYPE_TIMESHEET);
					} else if (mCallingActivity
							.equalsIgnoreCase("ClockInActivity")) {
						insertShiftStartTimeIntoHoursCalculator();
						Utils.saveTimeSheetInBackLogTable(
								ClockInConfirmationActivity.this,
								Utils.startShiftTimeRequest,
								CommsConstant.START_SHIFT_API,
								Utils.REQUEST_TYPE_TIMESHEET);
					}
					break;

				default:
					break;
				}
			}
		};
		return handler;
	}

	private Handler getEndCallOrShiftHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					// String result = (String) msg.obj;
					Intent intent = new Intent(
							ClockInConfirmationActivity.this,
							ShiftOrCallEndActivity.class);
					intent.putExtra(Utils.CALLING_ACTIVITY,
							ClockInConfirmationActivity.this.getClass()
									.getSimpleName());
					cancelAlarm();
					startActivity(intent);
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(context, exception, "Info");
					Bundle bundle = getIntent().getExtras();

					if (bundle != null && bundle.containsKey(Utils.END_CALL))
						Utils.saveTimeSheetInBackLogTable(
								ClockInConfirmationActivity.this,
								Utils.callEndTimeRequest,
								CommsConstant.END_ON_CALL_API,
								Utils.REQUEST_TYPE_TIMESHEET);
					else if (bundle != null
							&& bundle.containsKey(Utils.SHIFT_END)) {
						Utils.saveTimeSheetInBackLogTable(
								ClockInConfirmationActivity.this,
								Utils.endShiftRequest,
								CommsConstant.END_SHIFT_API,
								Utils.REQUEST_TYPE_TIMESHEET);
					}
					Intent shiftOrEndCallIntent = new Intent(
							ClockInConfirmationActivity.this,
							ShiftOrCallEndActivity.class);
					cancelAlarm();
					shiftOrEndCallIntent.putExtra(Utils.CALLING_ACTIVITY,
							ClockInConfirmationActivity.this.getClass()
									.getSimpleName());

					startActivity(shiftOrEndCallIntent);
					break;

				default:
					break;
				}
			}
		};
		return handler;
	}

	@Override
	public void onContinue() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDismiss() {
		// TODO Auto-generated method stub

	}

	private void initiateAlarm() {
		Utils.alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, OverTimeAlertService.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	private void setAlarmForOverTime() {
		Utils.alarmMgr.setInexactRepeating(
				AlarmManager.ELAPSED_REALTIME_WAKEUP,
				Utils.OVETTIME_ALERT_TOGGLE, Utils.OVETTIME_ALERT_INTERVAL,
				alarmIntent);
	}

	private void cancelAlarm() {
		if (Utils.alarmMgr != null) {
			Utils.alarmMgr.cancel(ClockInConfirmationActivity.alarmIntent);
		}
	}
}