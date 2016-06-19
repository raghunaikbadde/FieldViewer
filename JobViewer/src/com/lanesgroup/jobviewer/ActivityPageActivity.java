package com.lanesgroup.jobviewer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.confined.ConfinedAssessmentQuestionsActivity;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.BreakShiftTravelCall;
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
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.OverrideReasoneDialog;
import com.jobviewer.util.SelectActivityDialog;
import com.jobviewer.util.Utils;
import com.jobviewer.util.showTimeDialog;
import com.jobviewer.util.showTimeDialog.DialogCallback;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.fragment.QuestionsActivity;
import com.lanesgroup.jobviewer.fragment.ShoutOutActivity;
import com.raghu.WorkRequest;
import com.vehicle.communicator.HttpConnection;

public class ActivityPageActivity extends BaseActivity implements
		View.OnClickListener, DialogCallback, ConfirmDialogCallback {
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
		if (Utils.isInternetAvailable(mContext)) {
			GPSTracker tracker = new GPSTracker(mContext);
			tracker.getLocation();
		}
		initUI();
		updateDetailsOnUI();
		Utils.startNotification(mContext);
		Intent pushIntent = new Intent(this, SendImageService.class);
		startService(pushIntent);
	}

	private void updateDetailsOnUI() {
		this.unregisterForContextMenu(mStart);
		mShoutAbout = (ImageView) findViewById(R.id.shout_about_image);
		mShoutAbout.setOnClickListener(this);

		User userProfile = JobViewerDBHandler.getUserProfile(this);
		if (userProfile != null) {
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
		if (ActivityConstants.JOB_SELECTED_SHIFT
				.equalsIgnoreCase(Utils.checkOutObject.getJobSelected())) {
			mEndOnCall.setText("End Shift");
		} else {
			mEndOnCall.setText("End Call");
		}

		StartTrainingObject trainingToolBox = JobViewerDBHandler
				.getTrainingToolBox(this);
		if (trainingToolBox != null) {
			if (ActivityConstants.TRUE.equalsIgnoreCase(trainingToolBox
					.getIsTrainingStarted())) {
				mStart.setTag(Constants.END_TRAINING);
				mStart.setText(Constants.END_TRAINING);
			}
		} else {
			bundle = getIntent().getExtras();
			boolean shouldShowWorkInProgress = false;

			boolean shouldShowWorkInProgressWithNoPhotos = false;
			if (bundle != null
					&& bundle.containsKey(Utils.SHOULD_SHOW_WORK_IN_PROGRESS)) {
				shouldShowWorkInProgress = bundle
						.getBoolean(Utils.SHOULD_SHOW_WORK_IN_PROGRESS);
			}
			if (bundle != null
					&& bundle.containsKey(Constants.WORK_NO_PHOTOS_HOME)) {
				shouldShowWorkInProgressWithNoPhotos = bundle
						.getBoolean(Constants.WORK_NO_PHOTOS_HOME);
			}

			SurveyJson WorkWithNoPhotosSurveryJSON = JobViewerDBHandler
					.getWorkWithNoPhotosQuestionSet(this);
			SurveyJson questionSet = JobViewerDBHandler
					.getQuestionSet(mContext);
			if (questionSet != null
					&& !Utils.isNullOrEmpty(questionSet.getQuestionJson())) {
				mStart.setText("Continue Work In Progress");
				mStart.setTag("Continue Work In Progress");
			} else if (shouldShowWorkInProgress) {
				mStart.setText("Continue Work In Progress");
				mStart.setTag("Continue Work In Progress");
			} else if (shouldShowWorkInProgressWithNoPhotos
					|| (WorkWithNoPhotosSurveryJSON != null && !Utils
							.isNullOrEmpty(WorkWithNoPhotosSurveryJSON
									.getQuestionJson()))) {
				mStart.setText(getResources().getString(
						R.string.work_in_progree_str));
				mStart.setTag(getResources().getString(
						R.string.work_in_progree_str)
						+ Constants.WORK_NO_PHOTOS_HOME);
				this.registerForContextMenu(mStart);
			} else {
				mStart.setText(getResources().getString(R.string.start_text));
				mStart.setTag(getResources().getString(R.string.start_text));
			}

		}

		if (Utils.checkOutObject.getJobSelected().equalsIgnoreCase(
				ActivityConstants.JOB_SELECTED_SHIFT)) {
			mStartTravel
					.setText(getResources().getString(R.string.start_break));
		}

		String flagStr = JobViewerDBHandler.getJSONFlagObject(this);
		try {
			JSONObject jsonObject = new JSONObject(flagStr);
			if (jsonObject.has(Constants.CAPTURE_VISTEC_SCREEN)) {
				if (jsonObject.getBoolean(Constants.CAPTURE_VISTEC_SCREEN)) {
					mStart.setText("Continue Work In Progress");
					mStart.setTag("captureVisTecScreen");
					return;
				}
			}
		} catch (Exception e) {

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

		if (bundle != null
				&& bundle
						.containsKey(ActivityConstants.VEHICLE_REGISTRATION_NUMBER)) {
			checked_out_layout.setVisibility(View.VISIBLE);
			mCheckOutVehicle.setVisibility(View.GONE);

			if (Utils.isNullOrEmpty(vehicleRegNo))
				vehicleRegNo = bundle
						.getString(ActivityConstants.VEHICLE_REGISTRATION_NUMBER);
			if (Utils.isNullOrEmpty(vehicleRegNo))
				vehicleRegNo = bundle
						.getString(ActivityConstants.VEHICLE_REGISTRATION_NUMBER);
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
			String flagStr = JobViewerDBHandler.getJSONFlagObject(this);
			try {
				JSONObject jsonObject = new JSONObject(flagStr);
				if (jsonObject.has(Constants.CAPTURE_VISTEC_SCREEN)) {
					if (jsonObject.getBoolean(Constants.CAPTURE_VISTEC_SCREEN)) {
						Intent vistecIntent = new Intent(this,
								CaptureVistecActivity.class);
						startActivity(vistecIntent);
						return;
					}
				}
			} catch (Exception e) {

			}

			if ("Continue Work In Progress".equalsIgnoreCase(tag)) {
				CheckOutObject checkOutRemember = JobViewerDBHandler
						.getCheckOutRemember(mContext);
				SurveyJson questionSet = JobViewerDBHandler
						.getQuestionSet(mContext);
				if ("true".equalsIgnoreCase(checkOutRemember
						.getIsSavedOnAddPhotoScreen())) {
					Intent addPhotoScreenIntent = new Intent(mContext,
							AddPhotosActivity.class);
					Bundle addPhotoScreenIntentBundle = new Bundle();
					addPhotoScreenIntentBundle.putString(
							Utils.CALLING_ACTIVITY,
							ActivityConstants.ACTIVITY_PAGE_ACTIVITY);
					startActivity(addPhotoScreenIntent);
				} else if (bundle != null
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
				} else if (bundle != null
						&& bundle.containsKey(Utils.CALLING_ACTIVITY)
						&& bundle.getString(Utils.CALLING_ACTIVITY)
								.equalsIgnoreCase(
										ActivityConstants.POLLUTION_ACTIVITY)) {
					Intent addPhotoScreenIntent = new Intent(mContext,
							PollutionActivity.class);
					Bundle addPhotoScreenIntentBundle = new Bundle();
					addPhotoScreenIntentBundle.putString(
							Utils.CALLING_ACTIVITY,
							ActivityConstants.ACTIVITY_PAGE_ACTIVITY);
					addPhotoScreenIntent.putExtras(addPhotoScreenIntentBundle);
					startActivity(addPhotoScreenIntent);
				} else if (questionSet != null
						&& !Utils.isNullOrEmpty(questionSet.getQuestionJson())) {
					Intent riskAssIntent = new Intent(mContext,
							QuestionsActivity.class);
					startActivity(riskAssIntent);
				} else if (mStart.getTag().toString()
						.contains("captureVistecScreen")) {
					Intent vistecIntent = new Intent(this,
							CaptureVistecActivity.class);
					startActivity(vistecIntent);
				} else if (!Utils.isNullOrEmpty(checkOutRemember
						.getAssessmentSelected())) {
					Intent riskAssIntent = new Intent(mContext,
							RiskAssessmentActivity.class);
					startActivity(riskAssIntent);
				}
			} else if ((getResources().getString(R.string.work_in_progree_str) + Constants.WORK_NO_PHOTOS_HOME)
					.equalsIgnoreCase(tag)) {
				openContextMenu(mStart);
			} else if (Constants.END_TRAINING.equalsIgnoreCase(tag)) {
				executeEndTraining();

			} else {
				intent.setClass(this, SelectActivityDialog.class);
				startActivityForResult(intent,
						Constants.RESULT_CODE_START_TRAINING);
			}
		} else if (view == mCheckOutVehicle) {
			intent.setClass(this, CheckoutVehicleActivity.class);
			intent.putExtra(Utils.CALLING_ACTIVITY, ActivityPageActivity.this
					.getClass().getSimpleName());
			startActivity(intent);
		} else if (view == mStartTravel) {
			Utils.startTravelTimeRequest = new TimeSheetRequest();
			if (mStartTravel.getText().toString()
					.contains(getResources().getString(R.string.start_travel))) {
				new showTimeDialog(this, this, "travel").show();
			} else if (mStartTravel.getText().toString()
					.contains(getResources().getString(R.string.start_break))) {
				Utils.timeSheetRequest = new TimeSheetRequest();
				new showTimeDialog(this, this, "start").show();
			} else if (mStartTravel
					.getText()
					.toString()
					.contains(getResources().getString(R.string.end_travel_str))) {
				new showTimeDialog(this, this, getResources().getString(
						R.string.end_travel_str)).show();
			}
		} else if (view == mEndOnCall) {
			if (!mStart.getTag().toString()
					.contains("Continue Work In Progress")
					&& !mStart
							.getTag()
							.toString()
							.contains(
									getResources().getString(
											R.string.work_in_progree_str))) {
				endShiftOrCall();
			} else {
				if (ActivityConstants.JOB_SELECTED_SHIFT
						.equalsIgnoreCase(Utils.checkOutObject.getJobSelected())) {
					Toast.makeText(
							BaseActivity.context,
							BaseActivity.context.getResources().getString(
									R.string.closeWorkInProgressToastMsg),
							Toast.LENGTH_SHORT).show();
				}
			}

		} else if (view == mShoutAbout) {
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
		if (mEndOnCall.getText().toString().equals("End Call")) {

			Utils.callEndTimeRequest = new TimeSheetRequest();
			if (mCheckOutVehicle.getVisibility() != View.VISIBLE) {
				intent = new Intent(ActivityPageActivity.this,
						EndShiftReturnVehicleActivity.class);
				intent.putExtra("progressStep", "Step 1 of 2");
			} else {
				intent = new Intent(ActivityPageActivity.this,
						EndOnCallActivity.class);
				intent.putExtra("progressStep", "Step 1 of 1");
				intent.putExtra("mileage", JobViewerDBHandler
						.getCheckOutRemember(ActivityPageActivity.this)
						.getMilage());
			}
			intent.putExtra(Utils.END_CALL, Utils.END_CALL);
			intent.putExtra(Utils.CALLING_ACTIVITY, ActivityPageActivity.this
					.getClass().getSimpleName());
			startActivity(intent);
		} else if (mEndOnCall.getText().toString().equals("End Shift")) {
			String tag = (String) mStart.getTag();
			if (tag.contains(Constants.END_TRAINING)) {
				Toast.makeText(
						BaseActivity.context,
						BaseActivity.context.getResources().getString(
								R.string.endTrainingWarningMsg),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (mCheckOutVehicle.getVisibility() != View.VISIBLE) {
				intent = new Intent(ActivityPageActivity.this,
						EndShiftReturnVehicleActivity.class);
				intent.putExtra("progressStep", "Step 1 of 2");
			} else {
				intent = new Intent(ActivityPageActivity.this,
						EndOnCallActivity.class);
				intent.putExtra("mileage", JobViewerDBHandler
						.getCheckOutRemember(ActivityPageActivity.this)
						.getMilage());
				intent.putExtra("progressStep", "Step 1 of 1");
			}
			Utils.endShiftRequest = new TimeSheetRequest();
			intent.putExtra(Utils.SHIFT_END, Utils.SHIFT_END);
			intent.putExtra(Utils.CALLING_ACTIVITY, ActivityPageActivity.this
					.getClass().getSimpleName());
			startActivity(intent);
		}
	}

	private void executeEndTraining() {
		new ConfirmDialog(mContext, ActivityPageActivity.this,
				Constants.END_TRAINING).show();
	}

	@Override
	public void onContinue() {
		if (!Utils.isInternetAvailable(mContext)) {

			if (Utils.checkOutObject.getJobSelected().contains(
					ActivityConstants.JOB_SELECTED_SHIFT)) {
				JobViewerDBHandler.saveTimeSheet(this, Utils.timeSheetRequest,
						CommsConstant.START_BREAK_API);
				Utils.saveTimeSheetInBackLogTable(this, Utils.timeSheetRequest,
						CommsConstant.START_BREAK_API,
						"TimeSheetServiceRequests");
				startEndBreakActivity();

			} else {
				JobViewerDBHandler.saveTimeSheet(this,
						Utils.startTravelTimeRequest,
						CommsConstant.START_TRAVEL_API);
				Utils.saveTimeSheetInBackLogTable(this,
						Utils.startTravelTimeRequest,
						CommsConstant.START_TRAVEL_API,
						"TimeSheetServiceRequests");
				String time = new SimpleDateFormat("HH:mm:ss dd MMM yyyy")
						.format(Calendar.getInstance().getTime());
				insertStartTravelTimeRequestInDB();
				startEndActvity(time);
			}
		} else {
			if (Utils.checkOutObject.getJobSelected().contains(
					ActivityConstants.JOB_SELECTED_SHIFT)) {
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
			if (data.getExtras().getString("eventType")
					.equalsIgnoreCase("start")) {
				timeSheetRequest = Utils.timeSheetRequest;
			} else if (data.getExtras().getString("eventType")
					.equalsIgnoreCase("travel")) {
				timeSheetRequest = Utils.startTravelTimeRequest;
			} else {
				timeSheetRequest = Utils.endTravelTimeRequest;
			}

			if (ActivityConstants.TRUE.equalsIgnoreCase(timeSheetRequest
					.getIs_overriden())) {
				Intent intent = new Intent(this, OverrideReasoneDialog.class);
				intent.putExtra("eventType",
						data.getExtras().getString("eventType"));
				startActivityForResult(intent,
						Constants.RESULT_CODE_OVERRIDE_COMMENT);
			}
		} else if (requestCode == Constants.RESULT_CODE_OVERRIDE_COMMENT
				&& resultCode == RESULT_OK) {
			TimeSheetRequest timeSheetRequest;
			if (data.getExtras().getString("eventType")
					.equalsIgnoreCase("start")) {
				timeSheetRequest = Utils.timeSheetRequest;
			} else if (data.getExtras().getString("eventType")
					.equalsIgnoreCase("travel")) {
				timeSheetRequest = Utils.startTravelTimeRequest;
			} else {
				timeSheetRequest = Utils.endTravelTimeRequest;
			}
			if (!Utils.isInternetAvailable(this)) {
				if (data.getExtras().getString("eventType")
						.equalsIgnoreCase("start")) {
					JobViewerDBHandler.saveTimeSheet(this, timeSheetRequest,
							CommsConstant.START_BREAK_API);
					JobViewerDBHandler.getAllTimeSheet(mContext);
					Utils.saveTimeSheetInBackLogTable(
							ActivityPageActivity.this, timeSheetRequest,
							CommsConstant.START_BREAK_API,
							Utils.REQUEST_TYPE_WORK);
				} else if (data.getExtras().getString("eventType")
						.equalsIgnoreCase("travel")) {
					JobViewerDBHandler.saveTimeSheet(this, timeSheetRequest,
							CommsConstant.START_TRAVEL_API);
					JobViewerDBHandler.getAllTimeSheet(mContext);
					Utils.saveTimeSheetInBackLogTable(
							ActivityPageActivity.this, timeSheetRequest,
							CommsConstant.START_TRAVEL_API,
							Utils.REQUEST_TYPE_WORK);
				} else {
					JobViewerDBHandler.saveTimeSheet(this, timeSheetRequest,
							CommsConstant.END_TRAVEL_API);
					JobViewerDBHandler.getAllTimeSheet(mContext);
					Utils.saveTimeSheetInBackLogTable(
							ActivityPageActivity.this, timeSheetRequest,
							CommsConstant.END_TRAVEL_API,
							Utils.REQUEST_TYPE_WORK);
				}

				// saveStartBreakinToBackLogDb();
				if (Utils.checkOutObject.getJobSelected().contains(
						ActivityConstants.JOB_SELECTED_SHIFT)) {
					startEndBreakActivity();
				} else {
					insertStartTravelTimeRequestInDB();
					startEndActvity(Utils.startTravelTimeRequest
							.getOverride_timestamp());
				}
			} else {
				Utils.startProgress(ActivityPageActivity.this);
				if (data.getExtras().getString("eventType")
						.equalsIgnoreCase("start")) {
					executeStartBreakService();
				} else if (data.getExtras().getString("eventType")
						.equalsIgnoreCase("travel")) {
					executeStartTravelService();
				} else {
					Utils.StopProgress();
					startEndActvity(Utils.endTravelTimeRequest
							.getOverride_timestamp());
				}
			}
		} else if (requestCode == Constants.RESULT_CODE_START_TRAINING
				&& resultCode == RESULT_OK) {
			mStart.setText(Constants.END_TRAINING);
			mStart.setTag(Constants.END_TRAINING);
		}
	}

	private void startEndActvity(String time) {
		Intent intent = new Intent(this, EndTravelActivity.class);
		intent.putExtra("eventType", "End Travel");
		intent.putExtra(Constants.TIME, time);
		if (Utils.isNullOrEmpty(Utils.startTravelTimeRequest
				.getOverride_timestamp())) {
			intent.putExtra(Constants.TIME,
					Utils.startTravelTimeRequest.getStarted_at());
		} else {
			intent.putExtra(Constants.TIME,
					Utils.startTravelTimeRequest.getStarted_at());
			intent.putExtra(Constants.OVERRIDE_TIME,
					Utils.startTravelTimeRequest.getOverride_timestamp());
		}

		startActivity(intent);
	}

	private void startEndBreakActivity() {
		Intent intent = new Intent(this, EndBreakActivity.class);
		intent.putExtra("eventType", "End Break");
		intent.putExtra(Constants.OVERRIDE_TIME,
				Utils.timeSheetRequest.getOverride_timestamp());
		intent.putExtra(Constants.IS_OVERRIDEN,
				Utils.timeSheetRequest.getIs_overriden());
		intent.putExtra(Constants.TIME, Utils.timeSheetRequest.getStarted_at());
		startActivity(intent);
	}

	private void executeStartBreakService() {
		if (Utils.isInternetAvailable(mContext)) {
			ContentValues data = new ContentValues();
			data.put("started_at", Utils.timeSheetRequest.getStarted_at());
			data.put("record_for", Utils.timeSheetRequest.getRecord_for());
			data.put("is_inactive", Utils.timeSheetRequest.getIs_inactive());
			data.put("is_overriden", Utils.timeSheetRequest.getIs_overriden());
			data.put("override_reason",
					Utils.timeSheetRequest.getOverride_reason());
			data.put("override_comment",
					Utils.timeSheetRequest.getOverride_comment());
			data.put("override_timestamp",
					Utils.timeSheetRequest.getOverride_timestamp());
			data.put("reference_id", Utils.timeSheetRequest.getReference_id());
			data.put("user_id", Utils.timeSheetRequest.getUser_id());
			String time = "";
			if (Utils.isNullOrEmpty(Utils.timeSheetRequest
					.getOverride_timestamp())) {
				time = Utils.timeSheetRequest.getOverride_timestamp();
			} else {
				time = Utils.timeSheetRequest.getStarted_at();
			}
			Log.d(Utils.LOG_TAG,
					"executeStartBreakService "
							+ GsonConverter.getInstance().encodeToJsonString(
									Utils.timeSheetRequest));
			Utils.SendHTTPRequest(this, CommsConstant.HOST
					+ CommsConstant.START_BREAK_API, data,
					getStartBreakHandler(time));
		} else {
			Utils.saveTimeSheetInBackLogTable(mContext, Utils.timeSheetRequest,
					CommsConstant.START_BREAK_API, "TimeSheetServiceRequests");
		}

	}

	private void insertStartBreakTime() {
		BreakShiftTravelCall breakStart = JobViewerDBHandler
				.getBreakShiftTravelCall(context);
		breakStart.setBreakStartedTime(Utils
				.getMillisFromFormattedDate(Utils.timeSheetRequest
						.getStarted_at()));
		breakStart.setBreakStarted(Constants.YES_CONSTANT);
		if (Utils.timeSheetRequest.getIs_overriden().equalsIgnoreCase(
				ActivityConstants.TRUE)) {
			breakStart.setBreakStartedTime(Utils
					.getMillisFromFormattedDate(Utils.timeSheetRequest
							.getOverride_timestamp()));
		}
		JobViewerDBHandler.saveBreakShiftTravelCall(context, breakStart);
	}

	private Handler getStartBreakHandler(final String time) {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					insertStartBreakTime();
					startEndBreakActivity();

					// String result = (String) msg.obj;
					// startEndActvity(time);
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
		closeApplication();
	}

	@Override
	public void onConfirmStartTraining() {
		if (ConfirmDialog.eventType.contains(Constants.END_TRAINING)) {
			sendEndTraining();
		} else if (ConfirmDialog.eventType
				.contains(ActivityConstants.LEAVE_WORK_CONFIMRATION)) {
			sendLeaveWorkToServer();
		}
	}

	private void sendLeaveWorkToServer() {
		GPSTracker gpsTracker = new GPSTracker(mContext);
		if (Utils.isInternetAvailable(this)) {
			Utils.startProgress(mContext);
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(mContext);
			// User userProfile = JobViewerDBHandler.getUserProfile(mContext);
			ContentValues values = new ContentValues();
			values.put("started_at", checkOutRemember.getJobStartedTime());
			values.put("reference_id", checkOutRemember.getVistecId());
			values.put("engineer_id", Utils.work_engineer_id);
			values.put("status", Utils.work_status_stopped);
			values.put("completed_at", Utils.getCurrentDateAndTime());
			values.put("activity_type", "work");
			values.put("flooding_status", "");
			values.put("DA_call_out", Utils.work_DA_call_out);
			values.put("is_redline_captured", "false");
			values.put("location_latitude", gpsTracker.getLatitude());
			values.put("location_longitude", gpsTracker.getLongitude());
			Utils.work_id = JobViewerDBHandler.getCheckOutRemember(mContext)
					.getWorkId();
			Utils.SendHTTPRequest(mContext, CommsConstant.HOST
					+ CommsConstant.WORK_UPDATE_API + "/" + Utils.work_id,
					values, getLeaveWorkHandler());
		} else {
			saveUpdateWorkInBackLogDb();
		}

		/*
		 * JobViewerDBHandler.deleteWorkWithNoPhotosQuestionSet(mContext);
		 * Utils.StopProgress(); Intent intent = new
		 * Intent(mContext,ActivityPageActivity.class); finish();
		 * startActivity(intent);
		 */

	}

	private Handler getLeaveWorkHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					removeConfinedSpaceAsessementStartedTime(ActivityPageActivity.this);
					JobViewerDBHandler
							.deleteWorkWithNoPhotosQuestionSet(mContext);
					Utils.StopProgress();
					Intent intent = new Intent(mContext,
							ActivityPageActivity.class);
					finish();
					startActivity(intent);

					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(mContext, exception, "Info");
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

	private void saveUpdateWorkInBackLogDb() {
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getApplicationContext());
		User userProfile = JobViewerDBHandler.getUserProfile(mContext);
		WorkRequest workRequest = new WorkRequest();
		workRequest.setStarted_at(checkOutRemember.getJobStartedTime());
		Utils.lastest_work_started_at = Utils.getCurrentDateAndTime();
		if (checkOutRemember.getVistecId() != null) {
			workRequest.setReference_id(checkOutRemember.getVistecId());
		} else {
			workRequest.setReference_id("");
		}
		workRequest.setEngineer_id(Utils.work_engineer_id);
		workRequest.setStatus(Utils.work_status_stopped);
		workRequest.setCompleted_at(Utils.getCurrentDateAndTime());
		workRequest.setActivity_type("work");
		workRequest.setFlooding_status(Utils.work_flooding_status);
		workRequest.setDA_call_out(Utils.work_DA_call_out);
		workRequest.setIs_redline_captured(Utils.work_is_redline_captured);
		GPSTracker tracker = new GPSTracker(mContext);
		workRequest.setLocation_latitude("" + tracker.getLatitude());
		workRequest.setLocation_longitude("" + tracker.getLongitude());
		workRequest.setCreated_by(userProfile.getEmail());
		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.WORK_UPDATE_API + "/"
				+ Utils.work_id);
		backLogRequest.setRequestClassName("WorkRequest");
		backLogRequest.setRequestJson(GsonConverter.getInstance()
				.encodeToJsonString(workRequest));
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(context, backLogRequest);
		removeConfinedSpaceAsessementStartedTime(ActivityPageActivity.this);
	}

	private void sendEndTraining() {

		Utils.startProgress(mContext);
		ContentValues values = new ContentValues();
		values.put("started_at", Utils.getCurrentDateAndTime());
		User userProfile = JobViewerDBHandler.getUserProfile(mContext);
		values.put("record_for", userProfile.getEmail());
		values.put("is_inactive", false);
		values.put("is_overriden", false);
		values.put("override_reason", "");
		values.put("override_comment", "");
		values.put("override_timestamp", "");
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(mContext);
		if (Utils.isNullOrEmpty(checkOutRemember.getVistecId())) {
			values.put("reference_id", "");
		} else {
			values.put("reference_id", checkOutRemember.getVistecId());
		}
		values.put("user_id", userProfile.getEmail());
		if (Utils.isInternetAvailable(mContext)) {
			Utils.SendHTTPRequest(mContext, CommsConstant.HOST
					+ CommsConstant.END_TRAINING_API, values,
					getTrainingEndHandler());
		} else {

			TimeSheetRequest endTrainingTimeSheetRequest = new TimeSheetRequest();
			endTrainingTimeSheetRequest.setIs_inactive("false");
			endTrainingTimeSheetRequest.setIs_overriden("false");
			endTrainingTimeSheetRequest.setOverride_comment("");
			endTrainingTimeSheetRequest.setOverride_reason("");
			endTrainingTimeSheetRequest.setOverride_timestamp("");
			endTrainingTimeSheetRequest.setRecord_for(userProfile.getEmail());
			endTrainingTimeSheetRequest.setReference_id(userProfile.getEmail());
			endTrainingTimeSheetRequest.setStarted_at(Utils
					.getCurrentDateAndTime());
			endTrainingTimeSheetRequest.setUser_id(userProfile.getEmail());
			Utils.saveTimeSheetInBackLogTable(mContext,
					endTrainingTimeSheetRequest, CommsConstant.HOST
							+ CommsConstant.END_TRAINING_API,
					"TimeSheetServiceRequests");
			Utils.StopProgress();
			JobViewerDBHandler.deleteStartTraining(mContext);
			mStart.setText("Start...");
			mStart.setTag("Start...");
		}

	}

	private Handler getTrainingEndHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					String result = (String) msg.obj;
					Log.i("Android", result);
					Utils.StopProgress();
					JobViewerDBHandler.deleteStartTraining(mContext);
					mStart.setText("Start...");
					mStart.setTag(Constants.START_TRAINING);
					Toast.makeText(
							BaseActivity.context,
							BaseActivity.context.getResources().getString(
									R.string.traningendmsg), Toast.LENGTH_SHORT)
							.show();
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(mContext, exception, "Info");
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

	@Override
	public void onConfirmDismiss() {

	}

	private void executeStartTravelService() {
		if (Utils.isInternetAvailable(mContext)) {
			ContentValues data = new ContentValues();
			data.put("started_at", Utils.startTravelTimeRequest.getStarted_at());
			data.put("record_for", Utils.startTravelTimeRequest.getRecord_for());
			data.put("is_inactive",
					Utils.startTravelTimeRequest.getIs_inactive());
			data.put("is_overriden",
					Utils.startTravelTimeRequest.getIs_overriden());
			data.put("override_reason",
					Utils.startTravelTimeRequest.getOverride_reason());
			data.put("override_comment",
					Utils.startTravelTimeRequest.getOverride_comment());
			data.put("override_timestamp",
					Utils.startTravelTimeRequest.getOverride_timestamp());
			data.put("reference_id",
					Utils.startTravelTimeRequest.getReference_id());
			data.put("user_id", Utils.startTravelTimeRequest.getUser_id());
			Utils.startProgress(this);
			Utils.SendHTTPRequest(this, CommsConstant.HOST
					+ CommsConstant.START_TRAVEL_API, data,
					getStartTravelHandler());
		} else {
			insertStartTravelTimeRequestInDB();
			Utils.saveTimeSheetInBackLogTable(mContext,
					Utils.startTravelTimeRequest,
					CommsConstant.START_TRAVEL_API, "TimeSheetServiceRequests");
		}

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
					insertStartTravelTimeRequestInDB();
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(getResources().getString(
				R.string.context_work_options));
		if (v == mStart) {
			menu.add(
					0,
					1,
					0,
					getResources().getString(
							R.string.context_menu_start_confined_space_entry));
			menu.add(0, 2, 0,
					getResources().getString(R.string.context_menu_leave_work));
			menu.add(0, 3, 0,
					getResources().getString(R.string.context_menu_back));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Intent confinedWorkintent = new Intent(ActivityPageActivity.this,
					ConfinedAssessmentQuestionsActivity.class);
			insertConfinedSpaceEntryStartedTime();
			confinedWorkintent.putExtra(Constants.CALLING_ACTIVITY,
					ActivityPageActivity.this.getClass().getSimpleName());
			startActivity(confinedWorkintent);
			return true;
		case 2:
			ConfirmDialog confirmDialog = new ConfirmDialog(mContext, this,
					ActivityConstants.LEAVE_WORK_CONFIMRATION);
			confirmDialog.show();

			return true;
		case 3:
			mStart.getRootView().dispatchKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void insertStartTravelTimeRequestInDB() {
		String timeToStore = Utils
				.getMillisFromFormattedDate(Utils.startTravelTimeRequest
						.getStarted_at());
		if (Utils.startTravelTimeRequest.getIs_overriden().equalsIgnoreCase(
				ActivityConstants.TRUE))
			timeToStore = Utils
					.getMillisFromFormattedDate(Utils.startTravelTimeRequest
							.getOverride_timestamp());
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler
				.getBreakShiftTravelCall(ActivityPageActivity.this);
		breakShiftTravelCall.setTravelStarted(Constants.YES_CONSTANT);
		breakShiftTravelCall.setTravelStartedTime(timeToStore);
		JobViewerDBHandler.saveBreakShiftTravelCall(ActivityPageActivity.this,
				breakShiftTravelCall);

	}

	private void insertConfinedSpaceEntryStartedTime() {
		String str = JobViewerDBHandler.getJSONFlagObject(mContext);
		if (Utils.isNullOrEmpty(str)) {
			str = "{}";
		}
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.has(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME)) {
				jsonObject.remove(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME);
			}

			jsonObject.put(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME,
					Utils.getCurrentDateAndTime());
			String jsonString = jsonObject.toString();
			JobViewerDBHandler.saveFlaginJSONObject(getApplicationContext(),
					jsonString);
		} catch (Exception e) {

		}
	}

	private void removeConfinedSpaceAsessementStartedTime(Context mContext) {
		String str = JobViewerDBHandler.getJSONFlagObject(mContext);
		if (Utils.isNullOrEmpty(str)) {
			str = "{}";
		}
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.has(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME)) {
				jsonObject.remove(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME);
				String jsonString = jsonObject.toString();
				JobViewerDBHandler.saveFlaginJSONObject(this, jsonString);
			}

		} catch (Exception e) {

		}
	}
}
