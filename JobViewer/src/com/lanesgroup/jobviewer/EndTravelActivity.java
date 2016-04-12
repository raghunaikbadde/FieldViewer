package com.lanesgroup.jobviewer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.OverrideReasoneDialog;
import com.jobviewer.util.Utils;
import com.jobviewer.util.showTimeDialog;
import com.jobviewer.util.showTimeDialog.DialogCallback;
import com.jobviwer.request.object.TimeSheetRequest;
import com.vehicle.communicator.HttpConnection;

public class EndTravelActivity extends BaseActivity implements
		View.OnClickListener, DialogCallback {
	private Button mStartTravel;
	private TextView mTravelTime, mTitleText, mOverrideText;
	private String TRAVEL_STARTED = "Travel started: ";
	//private String ON_BREAK = "On Break";
	private String ENROUTE = "Enroute to Work";
	//private String BREAK_TIME = "Break started: ";
	//private String END_BREAK = "End Break";
	private String END_TRAVEL = "End Travel";
	//private boolean isBreakStarted;
	Context mContext;
	private String eventType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.end_travel_screen);
		Utils.endTimeRequest = new TimeSheetRequest();
		mContext = this;
		mTitleText = (TextView) findViewById(R.id.enroute_to_work_text);
		mTravelTime = (TextView) findViewById(R.id.travel_start_time_text);
		mOverrideText = (TextView) findViewById(R.id.travel_override_time_text);
		if(getIntent().getExtras().get(Constants.OVERRIDE_TIME) != null){
			String overtime = getIntent().getExtras().get(Constants.OVERRIDE_TIME).toString();
			mOverrideText.setVisibility(View.VISIBLE);
			mOverrideText.setText(overtime + " (User)");
		}
		
		mStartTravel = (Button) findViewById(R.id.end_travel);
		eventType = (String) getIntent().getExtras().get(Constants.STARTED);
		mTitleText.setText(ENROUTE);
		//Log.i(Utils.LOG_TAG, Utils.timeSheetRequest.getStarted_at());
		/*mTravelTime.setText(TRAVEL_STARTED
				+ getIntent().getExtras().get(Constants.TIME));*/
		JobViewerDBHandler.getCheckOutRemember(mContext).getTravelStartedTime();
		mTravelTime.setText(JobViewerDBHandler.getCheckOutRemember(mContext).getTravelStartedTime() + " (System)");
		mStartTravel.setText(END_TRAVEL);

		mStartTravel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Utils.endTravelTimeRequest = new TimeSheetRequest();
				eventType = "End Travel";
				new showTimeDialog(mContext, EndTravelActivity.this,
						"End Travel").show();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.RESULT_CODE_CHANGE_TIME
				&& resultCode == RESULT_OK) {
			if (ActivityConstants.TRUE.equalsIgnoreCase(Utils.endTravelTimeRequest.getIs_overriden())){
				Intent intent = new Intent(this, OverrideReasoneDialog.class);
				intent.putExtra("eventType", eventType);
				startActivityForResult(intent,
						Constants.RESULT_CODE_OVERRIDE_COMMENT);
			}
		} else if (requestCode == Constants.RESULT_CODE_OVERRIDE_COMMENT
				&& resultCode == RESULT_OK) {

				if (Utils.isInternetAvailable(mContext)) {					
					executeEndTravelService();
				} else {
					Utils.saveTimeSheetInBackLogTable(EndTravelActivity.this,Utils.endTravelTimeRequest,CommsConstant.END_TRAVEL_API,Utils.REQUEST_TYPE_WORK);
					//savePaidEndTravelinBackLogDb();
					JobViewerDBHandler.saveTimeSheet(this,
							Utils.endTravelTimeRequest, CommsConstant.HOST
									+ CommsConstant.END_TRAVEL_API);
					CheckOutObject checkOutRemember = JobViewerDBHandler
							.getCheckOutRemember(mContext);
					checkOutRemember.setIsStartedTravel("");
					checkOutRemember.setIsTravelEnd("true");
					JobViewerDBHandler.saveCheckOutRemember(mContext,
							checkOutRemember);
					Intent intent = new Intent(this, NewWorkActivity.class);
					startActivity(intent);
				}
			}
	}

	/*
	 * private void executeEndBreakService() { TimeSheetRequest endTimeRequest =
	 * null; String api; if
	 * (Constants.BREAK_STARTED.equalsIgnoreCase(eventType)) { endTimeRequest =
	 * Utils.endTimeRequest; api = CommsConstant.END_BREAK_API; } else {
	 * endTimeRequest = Utils.startTravelTimeRequest; api =
	 * CommsConstant.END_TRAVEL_API; } ContentValues data = new ContentValues();
	 * data.put("started_at", endTimeRequest.getStarted_at());
	 * data.put("record_for", endTimeRequest.getRecord_for());
	 * data.put("is_inactive", endTimeRequest.getIs_inactive());
	 * data.put("is_overriden", endTimeRequest.getIs_overriden());
	 * data.put("override_reason", endTimeRequest.getOverride_reason());
	 * data.put("override_comment", endTimeRequest.getOverride_comment());
	 * data.put("override_timestamp", endTimeRequest.getOverride_timestamp());
	 * data.put("reference_id", endTimeRequest.getReference_id());
	 * data.put("user_id", endTimeRequest.getUser_id());
	 * 
	 * Utils.SendHTTPRequest(this, CommsConstant.HOST + api, data,
	 * getEndBreakHandler()); }
	 * 
	 * private Handler getEndBreakHandler() { Handler handler = new Handler() {
	 * 
	 * @Override public void handleMessage(Message msg) { switch (msg.what) {
	 * case HttpConnection.DID_SUCCEED: Utils.StopProgress(); String result =
	 * (String) msg.obj; CheckOutObject checkOutRemember = JobViewerDBHandler
	 * .getCheckOutRemember(mContext); checkOutRemember.setIsTravelEnd("true");
	 * JobViewerDBHandler.saveCheckOutRemember(mContext, checkOutRemember);
	 * Intent intent = new Intent(EndTravelActivity.this,
	 * ActivityPageActivity.class);
	 * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(intent);
	 * break; case HttpConnection.DID_ERROR: Utils.StopProgress(); String error
	 * = (String) msg.obj; VehicleException exception = GsonConverter
	 * .getInstance() .decodeFromJsonString(error, VehicleException.class);
	 * ExceptionHandler.showException(mContext, exception, "Info"); break;
	 * default: break; } } }; return handler; }
	 */

	@Override
	public void onContinue() {
		if (Utils.isInternetAvailable(mContext)) {
			JobViewerDBHandler.saveTimeSheet(mContext,
					Utils.endTravelTimeRequest, CommsConstant.END_TRAVEL_API);
			executeEndTravelService();
		} else {
			JobViewerDBHandler.saveTimeSheet(this, Utils.endTimeRequest,
					CommsConstant.HOST + CommsConstant.END_TRAVEL_API);
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(mContext);
			checkOutRemember.setIsStartedTravel("");
			checkOutRemember.setIsTravelEnd("true");
			JobViewerDBHandler.saveCheckOutRemember(mContext, checkOutRemember);
			Utils.saveTimeSheetInBackLogTable(EndTravelActivity.this,
					Utils.endTravelTimeRequest, CommsConstant.END_TRAVEL_API,
					Utils.REQUEST_TYPE_WORK);
			Intent intent = new Intent(this, NewWorkActivity.class);
			startActivity(intent);
		}

	}

	private Handler getEndTravelHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					// String result = (String) msg.obj;
					CheckOutObject checkOutRemember = JobViewerDBHandler
							.getCheckOutRemember(mContext);
					checkOutRemember.setIsStartedTravel("");
					checkOutRemember.setIsTravelEnd("true");
					JobViewerDBHandler.saveCheckOutRemember(mContext,
							checkOutRemember);
					Intent intent = new Intent(mContext, NewWorkActivity.class);
					startActivity(intent);
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(mContext, exception, "Info");
					Utils.saveTimeSheetInBackLogTable(EndTravelActivity.this,
							Utils.endTravelTimeRequest,
							CommsConstant.END_TRAVEL_API,
							Utils.REQUEST_TYPE_WORK);
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

	private void executeEndTravelService() {
		Utils.startProgress(mContext);
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.endTravelTimeRequest.getStarted_at());
		data.put("record_for", Utils.endTravelTimeRequest.getRecord_for());
		data.put("is_inactive", Utils.endTravelTimeRequest.getIs_inactive());
		data.put("is_overriden", Utils.endTravelTimeRequest.getIs_overriden());
		data.put("override_reason",
				Utils.endTravelTimeRequest.getOverride_reason());
		data.put("override_comment",
				Utils.endTravelTimeRequest.getOverride_comment());
		data.put("override_timestamp",
				Utils.endTravelTimeRequest.getOverride_timestamp());
		data.put("reference_id", Utils.endTravelTimeRequest.getReference_id());
		data.put("user_id", Utils.endTravelTimeRequest.getUser_id());

		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.END_TRAVEL_API, data, getEndTravelHandler());

	}
	
	@Override
	public void onBackPressed() {
		closeApplication();
	}

	@Override
	public void onDismiss() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
