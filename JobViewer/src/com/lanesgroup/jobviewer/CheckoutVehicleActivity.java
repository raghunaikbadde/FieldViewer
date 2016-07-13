package com.lanesgroup.jobviewer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.jobviewer.exception.IDialogListener;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.JobViewerSharedPref;
import com.jobviewer.util.Utils;
import com.jobviwer.request.object.VehicleCheckInOut;
import com.jobviwer.response.object.User;
import com.vehicle.communicator.HttpConnection;

public class CheckoutVehicleActivity extends BaseActivity implements
		OnClickListener {

	private TextView mProgressSteps;
	private ProgressBar mProgressBar;
	private EditText mMileage;
	private CheckBox mRememberSelection;
	private Button mBack, mNext;
	private boolean isRegistraionEntered, isMileageEntered;
	private String callingFrom = "";
	private JobViewerSharedPref mSharedPref;
	private AutoCompleteTextView autocompleteRegistrationEditTextView;
	private TextView enter_details_text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.check_out_vehicle_screen2);
		initUI();
	}

	private void initUI() {
		mSharedPref = new JobViewerSharedPref();
		autocompleteRegistrationEditTextView = (AutoCompleteTextView) findViewById(R.id.autocompleteRegistrationEditTextView);
		mProgressSteps = (TextView) findViewById(R.id.progress_step_text);
		enter_details_text = (TextView) findViewById(R.id.enter_details_text);
		mProgressSteps.setText(Utils.PROGRESS_2_TO_3);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mProgressBar.setMax(3);
		mProgressBar.setProgress(2);
		mBack = (Button) findViewById(R.id.back_button);
		mBack.setOnClickListener(this);
		mNext = (Button) findViewById(R.id.next_button);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey(Utils.CALLING_ACTIVITY)) {
			callingFrom = bundle.getString(Utils.CALLING_ACTIVITY);
		}
		if (callingFrom.contains("ActivityPageActivity")) {
			mProgressSteps.setText(Utils.PROGRESS_1_TO_1);
		}
		if (ActivityConstants.JOB_SELECTED_ON_CALL
				.equalsIgnoreCase(Utils.checkOutObject.getJobSelected())) {
			enter_details_text.setText(getResources().getString(
					R.string.enter_vehicle_detail_on_call));
		}
		autocompleteRegistrationEditTextView
				.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence characters,
							int start, int before, int count) {
						isRegistraionEntered = characters.length() > 0;
						if (isRegistraionEntered && isMileageEntered)
							enableNextAction();
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
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
				isMileageEntered = characters.length() > 0;
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
									.setVehicleRegistration(autocompleteRegistrationEditTextView
											.getText().toString());
							Utils.checkOutObject.setMilage(mMileage.getText()
									.toString());
						} else {
							Utils.checkOutObject.setVehicleRegistration("");
							Utils.checkOutObject.setMilage("");
						}

					}
				});
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(this);
		if (!Utils.isNullOrEmpty(checkOutRemember.getVehicleRegistration())) {
			autocompleteRegistrationEditTextView.setText(checkOutRemember
					.getVehicleRegistration());
			mMileage.requestFocus();
		}

		if (!Utils.isNullOrEmpty(mSharedPref.getSharedPref(context).getString(
				JobViewerSharedPref.KEY_REGISTRATION_ARRAY, ""))) {
			setSuggestionsAdapter(mSharedPref.getSharedPref(context).getString(
					JobViewerSharedPref.KEY_REGISTRATION_ARRAY, ""));
		} else {
			if (Utils.isInternetAvailable(context)) {
				Utils.startProgress(context);
				getVehicleRegistrationNumberList();
			}
		}

	}

	private void setSuggestionsAdapter(String jsonArray) {
		if (!Utils.isNullOrEmpty(jsonArray)) {
			mSharedPref.saveRegistrationArrayString(context, jsonArray);
			ArrayList<String> suggestionList = new ArrayList<String>();
			JSONArray jArray = null;
			try {
				jArray = new JSONArray(jsonArray);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (jArray != null) {
				for (int i = 0; i < jArray.length(); i++) {
					try {
						suggestionList.add(jArray.get(i).toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
					android.R.layout.select_dialog_item, suggestionList);

			// Used to specify minimum number of characters the user has to type
			// in order to display the drop down hint.
			autocompleteRegistrationEditTextView.setThreshold(1);

			// Setting adapter
			autocompleteRegistrationEditTextView.setAdapter(arrayAdapter);
		}

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
			if (Utils.isInternetAvailable(view.getContext())) {
				Utils.startProgress(view.getContext());
				excuteCheckOutVehicle();
			} else {
				saveVechicleCheckOutInDB();

				JobViewerDBHandler.saveCheckOutRemember(
						CheckoutVehicleActivity.this, Utils.checkOutObject);
				Intent intent = null;
				if (callingFrom.contains("ActivityPageActivity")) {
					Utils.checkOutObject
							.setVehicleRegistration(autocompleteRegistrationEditTextView
									.getText().toString());
					Utils.checkOutObject.setMilage(mMileage.getText()
							.toString());
					JobViewerDBHandler.saveCheckOutRemember(
							CheckoutVehicleActivity.this, Utils.checkOutObject);
					intent = new Intent(CheckoutVehicleActivity.this,
							ActivityPageActivity.class);
				} else {
					intent = new Intent(CheckoutVehicleActivity.this,
							ClockInConfirmationActivity.class);
				}
				putVehcielRegNoAndMileageInIntent(intent);
				intent.putExtra(Utils.CALLING_ACTIVITY,
						CheckoutVehicleActivity.this.getClass().getSimpleName());
				startActivity(intent);
				// finish();
			}
		}
	}

	private void putVehcielRegNoAndMileageInIntent(Intent intent) {
		intent.putExtra(ActivityConstants.VEHICLE_REGISTRATION_NUMBER,
				autocompleteRegistrationEditTextView.getText().toString());
		intent.putExtra(ActivityConstants.VEHICLE_MILEAGE, mMileage.getText()
				.toString());
	}

	private void getVehicleRegistrationNumberList() {
		Utils.SendHTTPRequest(CheckoutVehicleActivity.this, CommsConstant.HOST
				+ CommsConstant.VEHICLE_REGISTRATIONS, new ContentValues(),
				vehicleRegistrationHandler());
	}

	private void excuteCheckOutVehicle() {
		User userProfile = JobViewerDBHandler
				.getUserProfile(CheckoutVehicleActivity.this);
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.getCurrentDateAndTime());
		data.put("record_for", userProfile.getEmail());
		data.put("registration", autocompleteRegistrationEditTextView.getText()
				.toString());
		data.put("mileage", mMileage.getText().toString());
		data.put("user_id", userProfile.getEmail());
		Log.d("TAG", "data" + data);
		Utils.SendHTTPRequest(CheckoutVehicleActivity.this, CommsConstant.HOST
				+ CommsConstant.CHECKOUT_VEHICLE, data, getCheckOutHandler());

	}

	private Handler getCheckOutHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					// String result = (String) msg.obj;
					Intent intent = null;
					if (callingFrom.contains("ActivityPageActivity")) {
						Utils.checkOutObject
								.setVehicleRegistration(autocompleteRegistrationEditTextView
										.getText().toString());
						Utils.checkOutObject.setMilage(mMileage.getText()
								.toString());
						JobViewerDBHandler.saveCheckOutRemember(
								CheckoutVehicleActivity.this,
								Utils.checkOutObject);
						intent = new Intent(CheckoutVehicleActivity.this,
								ActivityPageActivity.class);
					} else {
						intent = new Intent(CheckoutVehicleActivity.this,
								ClockInConfirmationActivity.class);
					}
					putVehcielRegNoAndMileageInIntent(intent);
					intent.putExtra(Utils.CALLING_ACTIVITY,
							CheckoutVehicleActivity.this.getClass()
									.getSimpleName());
					startActivity(intent);
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					if (exception.getMessage().equalsIgnoreCase(
							"Vehicle entry has been saved successfully")) {
						ExceptionHandler.showException(context, exception,
								"Info", new IDialogListener() {
									@Override
									public void onPositiveButtonClick(
											AlertDialog dialog) {

										Utils.StopProgress();
										// String result = (String) msg.obj;
										Intent intent = null;
										if (callingFrom
												.contains("ActivityPageActivity")) {
											Utils.checkOutObject
													.setVehicleRegistration(autocompleteRegistrationEditTextView
															.getText()
															.toString());
											Utils.checkOutObject
													.setMilage(mMileage
															.getText()
															.toString());
											JobViewerDBHandler
													.saveCheckOutRemember(
															CheckoutVehicleActivity.this,
															Utils.checkOutObject);
											intent = new Intent(
													CheckoutVehicleActivity.this,
													ActivityPageActivity.class);
										} else {
											intent = new Intent(
													CheckoutVehicleActivity.this,
													ClockInConfirmationActivity.class);
										}
										putVehcielRegNoAndMileageInIntent(intent);
										intent.putExtra(Utils.CALLING_ACTIVITY,
												CheckoutVehicleActivity.this
														.getClass()
														.getSimpleName());
										startActivity(intent);
									}

									@Override
									public void onNegativeButtonClick(
											AlertDialog dialog) {

									}
								});
					} else {
						ExceptionHandler.showException(context, exception,
								"Info", null);
						saveVechicleCheckOutInDB();
					}

					break;

				default:
					break;
				}
			}
		};
		return handler;
	}

	private Handler vehicleRegistrationHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					String result = (String) msg.obj;
					setSuggestionsAdapter(result);
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);

					ExceptionHandler.showException(context, exception, "Info",
							null);

					break;

				default:
					break;
				}
			}
		};
		return handler;
	}

	public void saveVechicleCheckOutInDB() {
		User userProfile = JobViewerDBHandler
				.getUserProfile(CheckoutVehicleActivity.this);
		VehicleCheckInOut vehicleCheckInOut = new VehicleCheckInOut();
		vehicleCheckInOut.setStarted_at(Utils.getCurrentDateAndTime());
		vehicleCheckInOut.setRecord_for(userProfile.getEmail());
		vehicleCheckInOut.setRegistration(autocompleteRegistrationEditTextView
				.getText().toString());
		vehicleCheckInOut.setMileage(mMileage.getText().toString());
		vehicleCheckInOut.setUser_id(userProfile.getEmail());
		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.HOST
				+ CommsConstant.CHECKOUT_VEHICLE);
		backLogRequest.setRequestClassName("VehicleCheckInOut");
		backLogRequest.setRequestJson(GsonConverter.getInstance()
				.encodeToJsonString(vehicleCheckInOut));
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(getApplicationContext(), backLogRequest);
	}
}
