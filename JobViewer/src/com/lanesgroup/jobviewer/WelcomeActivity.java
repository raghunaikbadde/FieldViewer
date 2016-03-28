package com.lanesgroup.jobviewer;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Utils;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.StartUpResponse;
import com.jobviwer.response.object.User;
import com.raghu.StartUpRequest;
import com.vehicle.communicator.HttpConnection;

public class WelcomeActivity extends BaseActivity {
	CheckBox shift, onCall;
	String selected;
	Button cancel, start;
	OnCheckedChangeListener checkChangedListner;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_screen);
		Utils.checkOutObject=new CheckOutObject();
		context = this;
		Button clockIn = (Button) findViewById(R.id.clock_in);
		clockIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openDialog();
			}
		});
		if (Utils.isInternetAvailable(context)) {
			executeStartUpApi();
		} else {
			User user = new User();
			user.setEmail(Utils.getMailId(context));
			JobViewerDBHandler.saveUserDetail(context, user);
			saveStartUpObjectInBackLogDb();
		}

		Utils.startNotification(this);
	}

	private void executeStartUpApi() {
		ContentValues data = new ContentValues();
		data.put("imei", Utils.getIMEI(context));
		data.put("email", Utils.getMailId(context));
		Utils.SendHTTPRequest(WelcomeActivity.this, CommsConstant.HOST
				+ CommsConstant.STARTUP_API, data, getHandler());
	}

	public Handler getHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					String result = (String) msg.obj;
					StartUpResponse decodeFromJsonString = GsonConverter
							.getInstance().decodeFromJsonString(result,
									StartUpResponse.class);
					JobViewerDBHandler.saveUserDetail(context,
							decodeFromJsonString.getData().getUser());
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

	public void openDialog() {
		final Dialog dialog = new Dialog(this, R.style.AppCompatDialogStyle);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);

		dialog.setContentView(R.layout.dialog_box1);

		shift = (CheckBox) dialog.findViewById(R.id.checkBox1);
		onCall = (CheckBox) dialog.findViewById(R.id.checkBox2);

		start = (Button) dialog.findViewById(R.id.dialog_ok);
		cancel = (Button) dialog.findViewById(R.id.dialog_cancel);

		shift.setOnCheckedChangeListener(checkChangedListner);
		onCall.setOnCheckedChangeListener(checkChangedListner);
		checkChangedListner = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (buttonView == shift && isChecked) {
					onCall.setChecked(false);
					start.setClickable(true);
					enableStartButton(dialog);
					selected = "shift";
					Utils.startShiftTimeRequest = new TimeSheetRequest();
					Utils.checkOutObject.setJobSelected(selected);
				} else if (buttonView == onCall && isChecked) {
					shift.setChecked(false);
					start.setClickable(true);
					enableStartButton(dialog);
					selected = "onCall";
					Utils.callStartTimeRequest = new TimeSheetRequest();
					Utils.checkOutObject.setJobSelected(selected);
				} else {
					start.setClickable(false);
				}
			}
		};

		shift.setOnCheckedChangeListener(checkChangedListner);
		onCall.setOnCheckedChangeListener(checkChangedListner);

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void enableStartButton(final Dialog dialog) {
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent;
				if (selected.equalsIgnoreCase("shift")) {
					Utils.startShiftTimeRequest = new TimeSheetRequest();
					JobViewerDBHandler.saveTimeSheet(WelcomeActivity.this, Utils.startShiftTimeRequest, CommsConstant.START_SHIFT_API);
					intent = new Intent(WelcomeActivity.this,
							ClockInActivity.class);
				} else {
					intent = new Intent(WelcomeActivity.this,
							ClockInConfirmationActivity.class);
					Utils.callStartTimeRequest = new TimeSheetRequest();
					intent.putExtra(Utils.CALLING_ACTIVITY,
							WelcomeActivity.this.getClass().getSimpleName());
				}
				dialog.dismiss();
				startActivity(intent);
			}
		});
	}
	
	private void saveStartUpObjectInBackLogDb(){
		StartUpRequest  startUpRequest = new StartUpRequest();
		startUpRequest.setImei(Utils.getIMEI(WelcomeActivity.this));
		startUpRequest.setEmail(Utils.getMailId(WelcomeActivity.this));
		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.HOST+CommsConstant.STARTUP_API);
		backLogRequest.setRequestJson(GsonConverter.getInstance().encodeToJsonString(startUpRequest));
		//GsonConverter.getInstance().encodeToJsonString(startUpRequest);
		backLogRequest.setRequestClassName("StartrUpRequest");
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(getApplicationContext(), backLogRequest);
	}
}
