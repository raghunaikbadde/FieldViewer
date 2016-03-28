package com.lanesgroup.jobviewer;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.provider.JobViewerProviderContract.CheckOutRemember;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;
import com.raghu.VehicleCheckInOut;
import com.vehicle.communicator.HttpConnection;

public class CheckoutVehicleActivity extends BaseActivity implements
		OnClickListener {

	private TextView mProgressSteps;
	private ProgressBar mProgressBar;
	private EditText mRegistration, mMileage;
	private CheckBox mRememberSelection;
	private Button mBack, mNext;
	private boolean isRegistraionEntered, isMileageEntered;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_out_vehicle_screen2);
		initUI();
	}

	private void initUI() {
		mProgressSteps = (TextView) findViewById(R.id.progress_step_text);
		mProgressSteps.setText(Utils.PROGRESS_2_TO_3);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mProgressBar.setMax(3);
		mProgressBar.setProgress(2);
		mBack = (Button) findViewById(R.id.back_button);
		mBack.setOnClickListener(this);
		mNext = (Button) findViewById(R.id.next_button);
		mRegistration = (EditText) findViewById(R.id.enter_registration_edittext);
		mRegistration.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence characters, int start,
					int before, int count) {
				if (characters.length() > 0)
					isRegistraionEntered = true;
				else
					isRegistraionEntered = false;
				if (isRegistraionEntered && isMileageEntered)
					enableNextAction();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mMileage = (EditText) findViewById(R.id.enter_mileage_edittext);
		mMileage.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence characters, int start,
					int before, int count) {
				if (characters.length() > 0)
					isMileageEntered = true;
				else
					isMileageEntered = false;
				if (isMileageEntered && isRegistraionEntered)
					enableNextAction();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mRememberSelection = (CheckBox) findViewById(R.id.checkBox3);

		mRememberSelection
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							Utils.checkOutObject
									.setVehicleRegistration(mRegistration
											.getText().toString());
							Utils.checkOutObject.setMilage(mMileage.getText()
									.toString());
						} else {
							Utils.checkOutObject.setVehicleRegistration("");
							Utils.checkOutObject.setMilage("");
						}

					}
				});
	}

	private void enableNextAction() {
		mNext.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
				R.drawable.red_background, null));
		mNext.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view == mBack) {
			finish();
		} else if (view == mNext) {
			if(Utils.isInternetAvailable(view.getContext())){
				excuteCheckOutVehicle();
			} else {
				saveVechicleCheckOutInDB();
				JobViewerDBHandler.saveCheckOutRemember(CheckoutVehicleActivity.this,
						Utils.checkOutObject);
				Intent intent = new Intent(CheckoutVehicleActivity.this,
						ClockInConfirmationActivity.class);
				intent.putExtra(Utils.CALLING_ACTIVITY,
						CheckoutVehicleActivity.this.getClass()
								.getSimpleName());
				startActivity(intent);
				//finish();
			}
			/*
			 * Intent intent = new Intent(CheckoutVehicleActivity.this,
			 * ClockInConfirmationActivity.class);
			 * intent.putExtra(Utils.CALLING_ACTIVITY,
			 * CheckoutVehicleActivity.this.getClass().getSimpleName());
			 * startActivity(intent);
			 */
		}
	}

	private void excuteCheckOutVehicle() {
		User userProfile = JobViewerDBHandler
				.getUserProfile(CheckoutVehicleActivity.this);
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.getCurrentDateAndTime());
		data.put("record_for", userProfile.getEmail());
		data.put("registration", mRegistration.getText().toString());
		data.put("mileage", mMileage.getText().toString());
		data.put("user_id", userProfile.getEmail());
		Utils.SendHTTPRequest(CheckoutVehicleActivity.this, CommsConstant.HOST
				+ CommsConstant.CHECKOUT_VEHICLE, data, getCheckOutHandler());

	}

	private Handler getCheckOutHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					// String result = (String) msg.obj;
					Intent intent = new Intent(CheckoutVehicleActivity.this,
							ClockInConfirmationActivity.class);
					intent.putExtra(Utils.CALLING_ACTIVITY,
							CheckoutVehicleActivity.this.getClass()
									.getSimpleName());
					startActivity(intent);
					break;
				case HttpConnection.DID_ERROR:
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(context, exception, "Info");
					saveVechicleCheckOutInDB();
					break;

				default:
					break;
				}
			}
		};
		return handler;
	}
	public void saveVechicleCheckOutInDB(){
		User userProfile = JobViewerDBHandler
				.getUserProfile(CheckoutVehicleActivity.this);
		VehicleCheckInOut vehicleCheckInOut = new VehicleCheckInOut();
		vehicleCheckInOut.setStarted_at(Utils.getCurrentDateAndTime());
		vehicleCheckInOut.setRecord_for(userProfile.getEmail());
		vehicleCheckInOut.setRegistration(mRegistration.getText().toString());
		vehicleCheckInOut.setMileage(mMileage.getText().toString());
		vehicleCheckInOut.setUser_id(userProfile.getEmail());
		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.HOST+CommsConstant.CHECKOUT_VEHICLE);
		backLogRequest.setRequestClassName("VehicleCheckInOut");
		backLogRequest.setRequestJson(vehicleCheckInOut.toString());
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(getApplicationContext(), backLogRequest);
	}
}
