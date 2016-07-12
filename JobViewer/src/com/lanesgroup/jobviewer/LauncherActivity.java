package com.lanesgroup.jobviewer;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.IDialogListener;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSDialog;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.StartUpResponse;
import com.jobviwer.response.object.User;
import com.raghu.StartUpRequest;
import com.vehicle.communicator.HttpConnection;

public class LauncherActivity extends BaseActivity {
	private Intent launcherIntent;

	// Splash screen timer
	private static int SPLASH_TIME_OUT = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		Utils.startProgress(this);
		new Handler().postDelayed(new Runnable() {

			/*
			 * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

			@Override
			public void run() {
				// This method will be executed once the timer is over
				// Start your app main activity
				launchNextActivity();

			}
		}, SPLASH_TIME_OUT);
	}

	
	private void launchNextActivity() {
		
		if (!Utils.isGPSEnabled(this)) {
			Utils.StopProgress();
			showGPSDialog();
			return;
		} else {
			// bug no 12: fix
			// Implement the GPS check on app startup and to store the
			// co-ordinates. If GPS is turned off/unavailable after the app
			// starts up,
			// the GPS co-ordinates obtained at the startup should be sent to
			// the server for images or for work location as appropriate.
			// This is similar to the fix provided in Vehicle Check application
			GPSTracker gpsTracker = new GPSTracker(this);
			gpsTracker.getLatitude();
			gpsTracker.getLongitude();
		}

		if (isBreakStartedShown()) {
			Utils.StopProgress();
			return;
		}

		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(this);
		if (checkOutRemember != null
				&& !Utils.isNullOrEmpty(checkOutRemember
						.getIsAssessmentCompleted())
				&& !Utils.isNullOrEmpty(checkOutRemember
						.getIsPollutionSelected())) {
			Utils.checkOutObject = checkOutRemember;
			launcherIntent = new Intent(this, PollutionActivity.class);
		} else if (checkOutRemember != null
				&& !Utils.isNullOrEmpty(checkOutRemember.getVistecId())) {
			Utils.checkOutObject = checkOutRemember;
			launcherIntent = new Intent(this, ActivityPageActivity.class);
		} else if (checkOutRemember != null
				&& ActivityConstants.TRUE.equalsIgnoreCase(checkOutRemember
						.getIsStartedTravel())) {
			launcherIntent = new Intent(this, EndTravelActivity.class);
			launcherIntent
					.putExtra(Constants.STARTED, Constants.TRAVEL_STARTED);
		} else if (checkOutRemember != null
				&& ActivityConstants.TRUE.equalsIgnoreCase(checkOutRemember
						.getIsTravelEnd())) {
			Utils.checkOutObject = checkOutRemember;
			launcherIntent = new Intent(this, ActivityPageActivity.class);
		} else if (checkOutRemember != null
				&& !Utils.isNullOrEmpty(checkOutRemember.getJobSelected())) {
			Utils.checkOutObject = checkOutRemember;
			launcherIntent = new Intent(this, ActivityPageActivity.class);
		} else {
			if (Utils.isInternetAvailable(context) && !Utils.isExitApplication) {
				executeStartUpApi();
			} else {
				if (!Utils.isNullOrEmpty(Utils.getMailId(context))) {
					User user = new User();
					user.setEmail(Utils.getMailId(context));
					JobViewerDBHandler.saveUserDetail(context, user);
					launcherIntent = new Intent(this, WelcomeActivity.class);
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
							closeApplication();
						}
					};
					ExceptionHandler.showException(context, ex, "Info",
							listener);
				}
			}
			
		}
		if(launcherIntent != null) {
			launcherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Utils.StopProgress();
			startActivity(launcherIntent);
		}
	}

	private boolean isBreakStartedShown() {
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler
				.getBreakShiftTravelCall(this);
		try {
			if (breakShiftTravelCall.isBreakStarted().equalsIgnoreCase(
					Constants.YES_CONSTANT)) {
				launcherIntent = new Intent(this, EndBreakActivity.class);
				launcherIntent.putExtra(Constants.TIME,
						breakShiftTravelCall.getBreakStartedTime());
				launcherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(launcherIntent);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;

		}
	}

	private void showGPSDialog() {
		new GPSDialog(this).show();

	}
	
	private void executeStartUpApi() {

		if (!Utils.isNullOrEmpty(Utils.getMailId(context))) {
			ContentValues data = new ContentValues();
			data.put("imei", Utils.getIMEI(context));
			data.put("email", Utils.getMailId(context));
			Utils.startProgress(LauncherActivity.this);
			Utils.SendHTTPRequest(LauncherActivity.this, CommsConstant.HOST
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
					launcherIntent = new Intent(getApplicationContext(), WelcomeActivity.class);
					launcherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Utils.StopProgress();
					startActivity(launcherIntent);
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
		startUpRequest.setImei(Utils.getIMEI(LauncherActivity.this));
		startUpRequest.setEmail(Utils.getMailId(LauncherActivity.this));
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
}
