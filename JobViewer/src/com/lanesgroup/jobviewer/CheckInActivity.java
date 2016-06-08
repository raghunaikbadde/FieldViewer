package com.lanesgroup.jobviewer;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;
import com.raghu.VehicleCheckInOut;
import com.vehicle.communicator.HttpConnection;

public class CheckInActivity extends BaseActivity implements
		View.OnClickListener {
	ProgressBar progressBar;
	TextView vehicle_registration_text_value;
	EditText enter_mileage_edittext;
	Button cancel_button, next_button;
	CheckOutObject checkOutRemember;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.check_in_screen);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		vehicle_registration_text_value = (TextView) findViewById(R.id.vehicle_registration_text_value);
		enter_mileage_edittext = (EditText) findViewById(R.id.enter_mileage_edittext);
		cancel_button = (Button) findViewById(R.id.cancel_button);
		next_button = (Button) findViewById(R.id.next_button);
		cancel_button.setOnClickListener(this);
		updateData();
	}

	private void updateData() {
		checkOutRemember = JobViewerDBHandler.getCheckOutRemember(this);
		vehicle_registration_text_value.setText(checkOutRemember
				.getVehicleRegistration());
		progressBar.setProgress(50);
		enableNextButton(false);
		enter_mileage_edittext.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().length() > 1) {
					enableNextButton(true);
				} else {
					enableNextButton(false);
				}

			}
		});
		
		next_button.setOnClickListener(this);
	}

	public void enableNextButton(boolean isEnable) {
		if (isEnable) {
			next_button.setEnabled(isEnable);
			next_button.setBackgroundResource(R.drawable.red_background);
		} else {
			next_button.setEnabled(isEnable);
			next_button.setBackgroundResource(R.drawable.dark_grey_background);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancel_button:
			finish();
			break;
		case R.id.next_button:
			if (Utils.isInternetAvailable(v.getContext())) {
				Utils.startProgress(v.getContext());
				executeCheckInService();
			} else {
				saveCheckInVehicleInBackLogDB();
				loadHomeActivity();
			}
			break;
		default:
			break;
		}

	}

	private void executeCheckInService() {
		User userProfile = JobViewerDBHandler
				.getUserProfile(CheckInActivity.this);
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.getCurrentDateAndTime());
		data.put("record_for", userProfile.getEmail());
		data.put("registration", checkOutRemember.getVehicleRegistration());
		data.put("mileage", enter_mileage_edittext.getText().toString());
		data.put("user_id", userProfile.getEmail());
		Log.d(Utils.LOG_TAG, "executeCheckInService : "+GsonConverter.getInstance().encodeToJsonString(data));
		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.CHECKIN_VEHICLE, data, getCheckInHandler());

	}

	private void loadHomeActivity() {
		checkOutRemember.setMilage("");
		
		JobViewerDBHandler.saveCheckOutRemember(CheckInActivity.this,
				checkOutRemember);
		
		Intent intent = new Intent(CheckInActivity.this,
				ActivityPageActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Utils.checkOutObject.setMilage("");
		startActivity(intent);
	}

	private Handler getCheckInHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					loadHomeActivity();
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
	
	public void saveCheckInVehicleInBackLogDB(){
		User userProfile = JobViewerDBHandler
				.getUserProfile(CheckInActivity.this);		
		VehicleCheckInOut vehicleCheckInOut = new VehicleCheckInOut();
		vehicleCheckInOut.setStarted_at(Utils.getCurrentDateAndTime());
		vehicleCheckInOut.setRecord_for(userProfile.getEmail());
		vehicleCheckInOut.setRegistration(checkOutRemember.getVehicleRegistration());
		vehicleCheckInOut.setMileage(checkOutRemember.getMilage());
		vehicleCheckInOut.setUser_id(userProfile.getEmail());
		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.HOST+CommsConstant.CHECKIN_VEHICLE);
		backLogRequest.setRequestClassName("VehicleCheckInOut");
		backLogRequest.setRequestJson(GsonConverter.getInstance().encodeToJsonString(vehicleCheckInOut));
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(getApplicationContext(), backLogRequest);
	}
}
