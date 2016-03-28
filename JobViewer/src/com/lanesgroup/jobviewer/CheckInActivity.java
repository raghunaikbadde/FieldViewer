package com.lanesgroup.jobviewer;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;
import com.vehicle.communicator.HttpConnection;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

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
		setContentView(R.layout.check_in_screen);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		vehicle_registration_text_value = (TextView) findViewById(R.id.vehicle_registration_text_value);
		enter_mileage_edittext = (EditText) findViewById(R.id.enter_mileage_edittext);
		cancel_button = (Button) findViewById(R.id.cancel_button);
		next_button = (Button) findViewById(R.id.next_button);
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
				executeCheckInService();
			} else {
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
		Utils.SendHTTPRequest(CheckInActivity.this, CommsConstant.HOST
				+ CommsConstant.CHECKIN_VEHICLE, data, getCheckInHandler());

	}

	private void loadHomeActivity() {
		checkOutRemember.setMilage("");
		JobViewerDBHandler.saveCheckOutRemember(CheckInActivity.this,
				checkOutRemember);
		Intent intent = new Intent(CheckInActivity.this,
				ActivityPageActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private Handler getCheckInHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					loadHomeActivity();
					break;
				case HttpConnection.DID_ERROR:
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(context, exception, "Info");
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}
}
