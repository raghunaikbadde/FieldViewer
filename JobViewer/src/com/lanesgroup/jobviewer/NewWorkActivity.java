package com.lanesgroup.jobviewer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.nophotos.WorkWithNoPhotosQuestionActivity;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ConfirmDialog;
import com.jobviewer.util.ConfirmDialog.ConfirmDialogCallback;
import com.jobviewer.util.Constants;
import com.jobviewer.util.EditTextFocusListener;
import com.jobviewer.util.EditTextWatcher;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.JVResponse;
import com.jobviwer.response.object.User;
import com.raghu.WorkRequest;
import com.vehicle.communicator.HttpConnection;

public class NewWorkActivity extends BaseActivity implements OnClickListener,ConfirmDialogCallback {

	private ProgressBar mProgress;
	private TextView mProgressStep;
	private CheckBox mPollutionCheckBox;
	private EditText mDistrict1;
	private static EditText mDistrict2;
	private static EditText mTaskNumber1;
	private static EditText mTaskNumber2;
	private Button mCancel;
	private static Button mNext;
	static Context context;
	static int progress = 100 / 5;
	private Location mLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_work_screen);
		context = this;
		initUI();
	}

	private void initUI() {
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mProgress.setProgress(progress);
		mLocation = Utils.getCurrentLocation(this);
		mProgressStep = (TextView) findViewById(R.id.progress_step_text);
		mPollutionCheckBox = (CheckBox) findViewById(R.id.pollutionCheckBox);
		mDistrict1 = (EditText) findViewById(R.id.distric1_edittext);
		mDistrict2 = (EditText) findViewById(R.id.distric2_edittext);
		mTaskNumber1 = (EditText) findViewById(R.id.distric3_edittext);
		mTaskNumber2 = (EditText) findViewById(R.id.distric4_edittext);
		mCancel = (Button) findViewById(R.id.button1);
		mCancel.setOnClickListener(this);
		mNext = (Button) findViewById(R.id.button2);
		mNext.setOnClickListener(this);
		mDistrict2.requestFocus();
		mDistrict2.setOnFocusChangeListener(new EditTextFocusListener(this,
				mDistrict2, 2));
		mTaskNumber1.setOnFocusChangeListener(new EditTextFocusListener(this,
				mTaskNumber1, 2));
		mTaskNumber2.setOnFocusChangeListener(new EditTextFocusListener(this,
				mTaskNumber2, 4));

		mDistrict2.addTextChangedListener(new EditTextWatcher(this, mDistrict2,
				2, mTaskNumber1));
		mTaskNumber1.addTextChangedListener(new EditTextWatcher(this,
				mTaskNumber1, 2, mTaskNumber2));
		mTaskNumber2.addTextChangedListener(new EditTextWatcher(this,
				mTaskNumber2, 4, null));
		mPollutionCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							mProgressStep
									.setText(buttonView
											.getContext()
											.getResources()
											.getString(
													R.string.progress_step_pollution));
							progress = 100 / 6;
							mProgress.setProgress(progress);

						} else {
							mProgressStep.setText(buttonView.getContext()
									.getResources()
									.getString(R.string.progress_step));
							progress = 100 / 5;
							mProgress.setProgress(progress);
						}

					}
				});
	}

	public static void enableNextButton() {
		int mDistrict2Text = mDistrict2.getText().toString().length();
		int mTaskNumber1Text = mTaskNumber1.getText().toString().length();
		int mTaskNumber2Text = mTaskNumber2.getText().toString().length();
		if (mDistrict2Text == 2 && mTaskNumber1Text == 2
				&& mTaskNumber2Text == 4) {
			mNext.setEnabled(true);
			mNext.setBackgroundResource(R.drawable.red_background);
		} else {
			mNext.setEnabled(false);
			mNext.setBackgroundResource(R.drawable.dark_grey_background);
		}
	}

	@Override
	public void onClick(View view) {
		if (view == mCancel) {
			finish();
			closeApplication();
		} else if (view == mNext) {
			boolean isValidUserInput = isValidUserInput();
			if (isValidUserInput) {
				if(mPollutionCheckBox.isChecked()){
					ConfirmDialog confirmDialog = new ConfirmDialog(context, this, Constants.POLLUTION_CONFIRMATION);
					confirmDialog.show();
				} else{
					executeNewWorkActivity();
				}
			}
		}
	}

	private void executeNewWorkActivity() {
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(context);
		checkOutRemember.setVistecId(mDistrict2.getText().toString()
				+ mTaskNumber1.getText().toString()
				+ mTaskNumber2.getText().toString());
		if (mPollutionCheckBox.isChecked()) {
			checkOutRemember.setIsPollutionSelected("true");
		} else {
			checkOutRemember.setIsPollutionSelected("");
		}
		JobViewerDBHandler.saveCheckOutRemember(context,
				checkOutRemember);
		if (Utils.isInternetAvailable(context)) {
			executeWorkCreateService();
		} else {
			insertWorkStartTimeIntoHoursCalculator();
			saveCreatedWorkInBackLogDb();
			startEndActvity();
		}
	}

	private boolean isValidUserInput() {
		if (mDistrict2.getText().toString().length() < 2) {
			setError(mDistrict2);
			return false;
		} else if (mTaskNumber1.getText().toString().length() < 2) {
			setError(mTaskNumber1);
			return false;
		} else if (mTaskNumber2.getText().toString().length() < 4) {
			setError(mTaskNumber2);
			return false;
		}
		return true;
	}

	public static void setError(EditText editText) {
		final Drawable error_indicator;
		error_indicator = ResourcesCompat.getDrawable(context.getResources(),
				R.drawable.small_error_icon, null);
		int left = 0;
		int top = 0;
		int right = error_indicator.getIntrinsicHeight();
		int bottom = error_indicator.getIntrinsicWidth();
		error_indicator.setBounds(new Rect(left, top, right, bottom));
		switch (editText.getId()) {
		case R.id.distric2_edittext:
			mDistrict2.setError("Enter 2 digits", error_indicator);
			break;
		case R.id.distric3_edittext:
			mTaskNumber1.setError("Enter 2 digits", error_indicator);
			break;
		case R.id.distric4_edittext:
			mTaskNumber2.setError("Enter 4 letters or digits", error_indicator);
			break;

		default:
			break;
		}
	}

	private void executeWorkCreateService() {
		ContentValues data = new ContentValues();
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getApplicationContext());
		User userProfile = JobViewerDBHandler
				.getUserProfile(NewWorkActivity.this);
		insertWorkStartTimeIntoHoursCalculator();
		data.put("started_at", Utils.getCurrentDateAndTime());
		if (checkOutRemember.getVistecId() != null) {
			data.put("reference_id", checkOutRemember.getVistecId());
		} else {
			data.put("reference_id", "");
		}
		data.put("engineer_id", Utils.work_engineer_id);
		data.put("status", Utils.work_status);
		data.put("completed_at", Utils.work_completed_at);
		data.put("activity_type", "");
		if (Utils.isNullOrEmpty(Utils.work_flooding_status)) {
			data.put("flooding_status", "");
		} else
			data.put("flooding_status", Utils.work_flooding_status);
		data.put("DA_call_out", Utils.work_DA_call_out);
		data.put("is_redline_captured", false);
		GPSTracker tracker = new GPSTracker(NewWorkActivity.this);
		data.put("location_latitude", tracker.getLatitude());
		data.put("location_longitude", tracker.getLongitude());
		data.put("created_by", userProfile.getEmail());
		Utils.startProgress(this);
		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.WORK_CREATE_API, data, getWorkCreateHandler());

	}

	private Handler getWorkCreateHandler() {

		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					String result = (String) msg.obj;
					JVResponse decodeFromJsonString = GsonConverter
							.getInstance().decodeFromJsonString(result,
									JVResponse.class);
					CheckOutObject checkOutRemember = JobViewerDBHandler
							.getCheckOutRemember(context);
					checkOutRemember.setIsStartedTravel("true");
					checkOutRemember.setWorkId(decodeFromJsonString.getId());
					JobViewerDBHandler.saveCheckOutRemember(context,
							checkOutRemember);
					Utils.work_id = decodeFromJsonString.getId();
					startEndActvity();
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(context, exception, "Info");
					saveCreatedWorkInBackLogDb();
					break;
				default:
					break;
				}
			}

		};
		return handler;
	}

	private void startEndActvity() {
		if(CheckAndCcontinueNoWork()){
			Intent intent = new Intent(NewWorkActivity.this,
					WorkWithNoPhotosQuestionActivity.class);
			String jsonStr = JobViewerDBHandler.getJSONFlagObject(NewWorkActivity.this);
			try{
			
				JSONObject jsonObject = new JSONObject(jsonStr);
				jsonObject.put(Constants.WorkWithNoPhotosStartedAt,Utils.getCurrentDateAndTime());
				JobViewerDBHandler.saveFlaginJSONObject(NewWorkActivity.this, jsonObject.toString());
			}catch(JSONException jse){
				
			}
			
			startActivity(intent);
		} else {
			Intent intent = new Intent(NewWorkActivity.this,
					CaptureVistecActivity.class);
		
			startActivity(intent);
		}
	}

	private boolean CheckAndCcontinueNoWork() {
		Bundle bundle = getIntent().getExtras();
		if(bundle!=null && bundle.containsKey(Constants.WORK_NO_PHOTOS)){
			return true;
		}
		return false;
	}
	private void saveCreatedWorkInBackLogDb() {
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getApplicationContext());
		User userProfile = JobViewerDBHandler
				.getUserProfile(NewWorkActivity.this);
		WorkRequest workRequest = new WorkRequest();
		workRequest.setStarted_at(Utils.getCurrentDateAndTime());
		Utils.lastest_work_started_at = Utils.getCurrentDateAndTime();
		if (checkOutRemember.getVistecId() != null) {
			workRequest.setReference_id(checkOutRemember.getVistecId());
		} else {
			workRequest.setReference_id("");
		}
		workRequest.setEngineer_id(Utils.work_engineer_id);
		workRequest.setStatus(Utils.work_status);
		workRequest.setCompleted_at(Utils.work_completed_at);
		workRequest.setActivity_type("work");
		workRequest.setFlooding_status(Utils.work_flooding_status);
		workRequest.setDA_call_out(Utils.work_DA_call_out);
		workRequest.setIs_redline_captured(Utils.work_is_redline_captured);
		GPSTracker tracker = new GPSTracker(NewWorkActivity.this);
		workRequest.setLocation_latitude("" + tracker.getLatitude());
		workRequest.setLocation_longitude("" + tracker.getLongitude());
		workRequest.setCreated_by(userProfile.getEmail());
		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.WORK_CREATE_API);
		backLogRequest.setRequestClassName("WorkRequest");
		backLogRequest.setRequestJson(workRequest.toString());
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(context, backLogRequest);
	}

	@Override
	public void onConfirmStartTraining() {
		executeNewWorkActivity();		
	}

	@Override
	public void onConfirmDismiss() {
		mPollutionCheckBox.setChecked(false);
	}
	
	private void insertWorkStartTimeIntoHoursCalculator() {
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(NewWorkActivity.this);
		if (breakShiftTravelCall==null) {
			breakShiftTravelCall=new BreakShiftTravelCall();
		}
		breakShiftTravelCall.setWorkStartTime(String.valueOf(System.currentTimeMillis()));
		JobViewerDBHandler.saveBreakShiftTravelCall(NewWorkActivity.this, breakShiftTravelCall);
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		closeApplication();
	}
}
