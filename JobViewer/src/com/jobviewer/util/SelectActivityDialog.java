package com.jobviewer.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
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
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.StartTrainingObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ConfirmDialog.ConfirmDialogCallback;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.NewWorkActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.TravelToWorkSiteActivity;
import com.vehicle.communicator.HttpConnection;

public class SelectActivityDialog extends Activity implements ConfirmDialogCallback {

	private CheckBox mWork, mWorkNoPhotos, mTraining;
	private String selected;
	private OnCheckedChangeListener checkChangedListner;
	private Button start, cancel;
	private Context mContext;

	private final String WORK = "Work";
	private final String WORK_NO_PHOTOS = "WorkNoPhotos";
	private final String TRAINING = "Training";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		setContentView(R.layout.dialog_box2);

		mContext = this;
		
		mWork = (CheckBox) findViewById(R.id.checkBox1);
		mWorkNoPhotos = (CheckBox) findViewById(R.id.checkBox2);
		mTraining = (CheckBox) findViewById(R.id.checkBox3);

		start = (Button) findViewById(R.id.dialog_ok);
		cancel = (Button) findViewById(R.id.dialog_cancel);

		checkChangedListner = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (buttonView == mWork && isChecked) {
					mTraining.setChecked(false);
					mWorkNoPhotos.setChecked(false);
					start.setClickable(true);
					enableStartButton();
					selected = WORK;
				} else if (buttonView == mWorkNoPhotos && isChecked) {
					mWork.setChecked(false);
					mTraining.setChecked(false);
					start.setClickable(true);
					enableStartButton();
					selected = WORK_NO_PHOTOS;
				} else if (buttonView == mTraining && isChecked) {
					mWork.setChecked(false);
					mWorkNoPhotos.setChecked(false);
					start.setClickable(true);
					enableStartButton();
					selected = TRAINING;
				} else {
					start.setClickable(false);
				}
			}
		};

		mWork.setOnCheckedChangeListener(checkChangedListner);
		mWorkNoPhotos.setOnCheckedChangeListener(checkChangedListner);
		mTraining.setOnCheckedChangeListener(checkChangedListner);

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void enableStartButton() {
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent;
				if (selected.equalsIgnoreCase(WORK)) {
					CheckOutObject checkOutRemember = JobViewerDBHandler
							.getCheckOutRemember(v.getContext());
					if (checkOutRemember != null
							&& ActivityConstants.TRUE
									.equalsIgnoreCase(checkOutRemember
											.getIsTravelEnd())) {
						intent = new Intent(SelectActivityDialog.this,
								NewWorkActivity.class);
					} else {
						intent = new Intent(SelectActivityDialog.this,
								TravelToWorkSiteActivity.class);
					}
					startActivity(intent);

				} else if (selected.equalsIgnoreCase(WORK_NO_PHOTOS)) {
					/*intent = new Intent(SelectActivityDialog.this,
							MainActivity.class);
					startActivity(intent);*/
				} else {
					new ConfirmDialog(v.getContext(), SelectActivityDialog.this, Constants.START_TRAINING).show();
					return;
				}
				finish();
			}
		});
	}

	@Override
	public void onConfirmStartTraining() {
		
		ContentValues data = new ContentValues();
		Utils.timeSheetRequest = new TimeSheetRequest();
		
		Utils.timeSheetRequest.setStarted_at(new SimpleDateFormat("HH:mm:ss dd MMM yyyy")
		.format(Calendar.getInstance().getTime())); 
				
		data.put("started_at", new SimpleDateFormat("HH:mm:ss dd MMM yyyy")
		.format(Calendar.getInstance().getTime()));
		
		
		User userProfile = JobViewerDBHandler.getUserProfile(this);
		if(userProfile!=null){
			Utils.timeSheetRequest.setRecord_for(userProfile.getEmail());
			data.put("record_for", userProfile.getEmail());
		}else{
			Utils.timeSheetRequest.setRecord_for("fsa@lancegroup.com");
			data.put("record_for", "fsa@lancegroup.com");
		}
		
		Utils.timeSheetRequest.setIs_inactive("");
		data.put("is_inactive", "");
		
		Utils.timeSheetRequest.setIs_overriden("");
		data.put("is_overriden", "");
		
		Utils.timeSheetRequest.setOverride_reason("");
		data.put("override_reason", "");
		
		Utils.timeSheetRequest.setOverride_comment("");
		data.put("override_comment","");
		
		Utils.timeSheetRequest.setOverride_timestamp("");
		data.put("override_timestamp","");
		
		CheckOutObject checkOutObject = JobViewerDBHandler.getCheckOutRemember(this);
		if(checkOutObject.getVistecId() != null){
			Utils.timeSheetRequest.setReference_id(checkOutObject.getVistecId());
			data.put("reference_id", checkOutObject.getVistecId());
		} else {
			Utils.timeSheetRequest.setReference_id("");
			data.put("reference_id", "");
		}
		if(userProfile!=null)
			data.put("user_id", userProfile.getEmail());
		else 
			data.put("user_id", "fsa@lancegroup.com");
		String time = new SimpleDateFormat("HH:mm:ss dd MMM yyyy")
		.format(Calendar.getInstance().getTime());

		if (Utils.isInternetAvailable(this)){
			Utils.SendHTTPRequest(this, CommsConstant.HOST
					+ CommsConstant.START_TRAINING_API, data,
					getStartTrainingHandler(time));
		} else {
			Utils.saveTimeSheetInBackLogTable(
					SelectActivityDialog.this, Utils.timeSheetRequest,
					CommsConstant.START_TRAINING_API,
					Utils.REQUEST_TYPE_WORK);
			saveTrainingTimeSheet(Utils.timeSheetRequest);
		}
		setResult(RESULT_OK);
		finish();
	}
	
	private void saveTrainingTimeSheet(TimeSheetRequest timeSheetRequest) {
		StartTrainingObject startTraining=new StartTrainingObject();
		startTraining.setIsTrainingStarted("true");
		startTraining.setStartTime(timeSheetRequest.getStarted_at());
		JobViewerDBHandler.saveStartTraining(this, startTraining);
		
	}

	private Handler getStartTrainingHandler(final String time) {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					saveTrainingTimeSheet(Utils.timeSheetRequest);
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(mContext, exception, "Info");
					Utils.saveTimeSheetInBackLogTable(
							SelectActivityDialog.this, Utils.timeSheetRequest,
							CommsConstant.START_TRAINING_API,
							Utils.REQUEST_TYPE_WORK);
					saveTrainingTimeSheet(Utils.timeSheetRequest);
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
		// TODO Auto-generated method stub
		
	}
}
