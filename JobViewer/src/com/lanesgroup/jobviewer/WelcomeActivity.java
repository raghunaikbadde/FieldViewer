package com.lanesgroup.jobviewer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ShoutAboutSafetyObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.IDialogListener;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.StartUpResponse;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.fragment.ShoutOutActivity;
import com.raghu.StartUpRequest;
import com.vehicle.communicator.HttpConnection;

public class WelcomeActivity extends BaseActivity {
	private CheckBox shift, onCall;
	private String selected;
	private Button cancel, start;
	private OnCheckedChangeListener checkChangedListner;
	private Context context;
	private ImageView shout_about_image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_screen);
		if (getIntent().getBooleanExtra("Exit me", false)) {
			finish();
		} else {
			Utils.checkOutObject = new CheckOutObject();
			context = this;
			if (Utils.isInternetAvailable(context)) {
				GPSTracker tracker = new GPSTracker(context);
				tracker.getLocation();
			}
			shout_about_image = (ImageView) findViewById(R.id.shout_about_image);
			shout_about_image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					launchShoutAboutSafetyScreen(v);
				}
			});

			Button clockIn = (Button) findViewById(R.id.clock_in);

			clockIn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(WelcomeActivity.this,
							SelectClockInActivityDialog.class);
					startActivityForResult(intent,
							Constants.RESULT_CODE_WELCOME);
				}
			});
			if (Utils.isInternetAvailable(context) && !Utils.isExitApplication) {
				executeStartUpApi();
			} else {
				if (!Utils.isNullOrEmpty(Utils.getMailId(context))) {
					User user = new User();
					user.setEmail(Utils.getMailId(context));
					JobViewerDBHandler.saveUserDetail(context, user);
					saveStartUpObjectInBackLogDb();
				} else {
					VehicleException ex = new VehicleException();
					ex.setMessage("Lanes group email id is not found in this device. Please configure in device settings.");
					IDialogListener listener = new IDialogListener() {

						@Override
						public void onPositiveButtonClick(AlertDialog dialog) {
							closeApplication();

						}

						@Override
						public void onNegativeButtonClick(AlertDialog dialog) {
							// TODO Auto-generated method stub

						}
					};
					ExceptionHandler.showException(context, ex, "Info",
							listener);
				}
			}

			Utils.startNotification(this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1003 && resultCode == RESULT_OK) {
			selected = data.getExtras().getString("Selected");

			if (Utils.checkOutObject == null) {
				Utils.checkOutObject = new CheckOutObject();
			}
			Intent intent;
			if (selected.equalsIgnoreCase("Shift")) {
				Utils.startShiftTimeRequest = new TimeSheetRequest();
				JobViewerDBHandler.saveTimeSheet(WelcomeActivity.this,
						Utils.startShiftTimeRequest,
						CommsConstant.START_SHIFT_API);
				intent = new Intent(WelcomeActivity.this, ClockInActivity.class);
				Utils.checkOutObject
						.setJobSelected(ActivityConstants.JOB_SELECTED_SHIFT);
				JobViewerDBHandler.saveCheckOutRemember(WelcomeActivity.this,
						Utils.checkOutObject);
				intent.putExtra(Utils.SHIFT_START, Utils.SHIFT_START);
			} else {
				intent = new Intent(WelcomeActivity.this,
						ClockInConfirmationActivity.class);
				Utils.callStartTimeRequest = new TimeSheetRequest();
				intent.putExtra(Utils.CALLING_ACTIVITY, WelcomeActivity.this
						.getClass().getSimpleName());
				Utils.checkOutObject
						.setJobSelected(ActivityConstants.JOB_SELECTED_ON_CALL);
				JobViewerDBHandler.saveCheckOutRemember(WelcomeActivity.this,
						Utils.checkOutObject);
				intent.putExtra(Utils.CALL_START, Utils.CALL_START);
			}
			startActivity(intent);

		}
	}

	private void executeStartUpApi() {

		if (!Utils.isNullOrEmpty(Utils.getMailId(context))) {
			ContentValues data = new ContentValues();
			data.put("imei", Utils.getIMEI(context));
			data.put("email", Utils.getMailId(context));
			Utils.startProgress(WelcomeActivity.this);
			Utils.SendHTTPRequest(WelcomeActivity.this, CommsConstant.HOST
					+ CommsConstant.STARTUP_API, data, getHandler());
		} else {
			VehicleException ex = new VehicleException();
			ex.setMessage("Lanes group email id is not found in this device. Please configure in device settings.");
			IDialogListener listener = new IDialogListener() {

				@Override
				public void onPositiveButtonClick(AlertDialog dialog) {
					closeApplication();

				}

				@Override
				public void onNegativeButtonClick(AlertDialog dialog) {
					// TODO Auto-generated method stub

				}
			};
			ExceptionHandler.showException(context, ex, "Info", listener);
		}

	}

	private void launchShoutAboutSafetyScreen(View v) {
		ShoutAboutSafetyObject shoutAboutSafety = JobViewerDBHandler
				.getShoutAboutSafety(v.getContext());
		Intent intent = new Intent();
		if (shoutAboutSafety != null
				&& !Utils.isNullOrEmpty(shoutAboutSafety.getQuestionSet())) {
			intent.setClass(v.getContext(), ShoutOutActivity.class);
			intent.putExtra(ActivityConstants.SHOUT_OPTION,
					shoutAboutSafety.getOptionSelected());
			intent.putExtra(ActivityConstants.IS_SHOUT_SAVED,
					ActivityConstants.TRUE);
			startActivity(intent);
		} else {
			intent.setClass(v.getContext(), ShoutOptionsActivity.class);
			startActivity(intent);
		}
	}

	public Handler getHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					String result = (String) msg.obj;
					StartUpResponse decodeFromJsonString = GsonConverter
							.getInstance().decodeFromJsonString(result,
									StartUpResponse.class);
					JobViewerDBHandler.saveUserDetail(context,
							decodeFromJsonString.getData().getUser());
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
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

	private void saveStartUpObjectInBackLogDb() {
		StartUpRequest startUpRequest = new StartUpRequest();
		startUpRequest.setImei(Utils.getIMEI(WelcomeActivity.this));
		startUpRequest.setEmail(Utils.getMailId(WelcomeActivity.this));
		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.HOST
				+ CommsConstant.STARTUP_API);
		backLogRequest.setRequestJson(GsonConverter.getInstance()
				.encodeToJsonString(startUpRequest));
		// GsonConverter.getInstance().encodeToJsonString(startUpRequest);
		backLogRequest.setRequestClassName("StartrUpRequest");
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(getApplicationContext(), backLogRequest);
	}

	@Override
	public void onBackPressed() {
		closeApplication();
	}
}
