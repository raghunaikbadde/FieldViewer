package com.lanesgroup.jobviewer;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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
import com.raghu.VehicleCheckInOut;
import com.vehicle.communicator.HttpConnection;

public class EndShiftReturnVehicleActivity extends BaseActivity implements
		OnClickListener {

	private Button mCancel, mNext;
	private EditText mMileage;
	private TextView mVehicleRegNo;
	private ProgressBar mProgressBar;
	private TextView check_out_text;
	String titleFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.end_shift_layout);
		titleFlag=(String) getIntent().getExtras().get(Utils.TITLE_FLAG);
		initUI();

	}

	private void initUI() {
		mCancel = (Button) findViewById(R.id.cancel_button);
		check_out_text = (TextView) findViewById(R.id.check_out_text);
		mNext = (Button) findViewById(R.id.next_button);
		mMileage = (EditText) findViewById(R.id.enter_mileage_edittext);
		mVehicleRegNo = (TextView) findViewById(R.id.vehicle_registration_text_value);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mProgressBar.setMax(2);
		mProgressBar.setProgress(1);
		if (Utils.END_CALL.equalsIgnoreCase(titleFlag)) {
			check_out_text.setText(getResources().getString(
					R.string.end_on_call_str));
		} else {
			check_out_text.setText(getResources().getString(
					R.string.end_shift_screen_title));
		}
		mMileage.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().length() > 0) {
					enableNextButton(true);
				} else {
					enableNextButton(false);
				}
			}
		});
		mCancel.setOnClickListener(this);
		mVehicleRegNo.setText(JobViewerDBHandler.getCheckOutRemember(
				EndShiftReturnVehicleActivity.this).getVehicleRegistration());
	}

	@Override
	public void onClick(View v) {

		if (v == mCancel) {
			finish();
		} else if (v == mNext) {
			Utils.startProgress(EndShiftReturnVehicleActivity.this);
			if (Utils.isInternetAvailable(EndShiftReturnVehicleActivity.this)) {
				executeCheckInVehicleService();
			} else {
				saveCheckInVehicleInBackLogDB();
				Utils.StopProgress();
				launchEndOnCallActivity();
			}
		}
	}

	private void launchEndOnCallActivity() {
		finish();
		Intent endOnCallActivity = new Intent(
				EndShiftReturnVehicleActivity.this, EndOnCallActivity.class);
		endOnCallActivity.putExtra("mileage", mMileage.getText().toString());
		endOnCallActivity.putExtra(Utils.CALLING_ACTIVITY,
				EndShiftReturnVehicleActivity.this.getClass().getSimpleName());
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

	private void executeCheckInVehicleService() {
		User userProfile = JobViewerDBHandler
				.getUserProfile(EndShiftReturnVehicleActivity.this);
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.getCurrentDateAndTime());
		data.put("record_for", userProfile.getEmail());
		data.put("registration", mVehicleRegNo.getText().toString());
		data.put("mileage", mMileage.getText().toString());
		data.put("user_id", userProfile.getEmail());
		Utils.SendHTTPRequest(EndShiftReturnVehicleActivity.this,
				CommsConstant.HOST + CommsConstant.CHECKIN_VEHICLE, data,
				getCheckOutHandler());

	}

	private Handler getCheckOutHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					launchEndOnCallActivity();
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(context, exception, "Info");
					saveCheckInVehicleInBackLogDB();
					break;

				default:
					break;
				}
			}
		};
		return handler;
	}

	public void saveCheckInVehicleInBackLogDB() {
		User userProfile = JobViewerDBHandler
				.getUserProfile(EndShiftReturnVehicleActivity.this);
		VehicleCheckInOut vehicleCheckInOut = new VehicleCheckInOut();
		vehicleCheckInOut.setStarted_at(Utils.getCurrentDateAndTime());
		vehicleCheckInOut.setRecord_for(userProfile.getEmail());
		vehicleCheckInOut.setRegistration(mVehicleRegNo.getText().toString());
		vehicleCheckInOut.setMileage(mMileage.getText().toString());
		vehicleCheckInOut.setUser_id(userProfile.getEmail());
		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.HOST
				+ CommsConstant.CHECKIN_VEHICLE);
		backLogRequest.setRequestClassName("VehicleCheckInOut");
		backLogRequest.setRequestJson(GsonConverter.getInstance()
				.encodeToJsonString(vehicleCheckInOut));
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(getApplicationContext(), backLogRequest);

	}
}
