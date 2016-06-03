package com.lanesgroup.jobviewer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.provider.JobViewerProviderContract.BreakTravelShiftCallTable;
import com.jobviewer.provider.JobViewerProviderContract.FlagJSON;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.OverrideReasoneDialog;
import com.jobviewer.util.Utils;
import com.jobviewer.util.showTimeDialog;
import com.jobviewer.util.showTimeDialog.DialogCallback;
import com.jobviwer.request.object.TimeSheetRequest;
import com.vehicle.communicator.HttpConnection;

public class EndBreakActivity extends BaseActivity implements OnClickListener,
		DialogCallback {

	Context mContext;
	Button mEndBreak;
	TextView mBreakTime;
	private String eventType = "End Break";
	TextView overriddenBreakStartTime;
	BreakShiftTravelCall breakTravelShiftCallTable;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.end_break);
		initUI();
	}

	private void initUI() {
		mEndBreak = (Button) findViewById(R.id.end_break);
		mBreakTime = (TextView)findViewById(R.id.break_start_time_text);
		overriddenBreakStartTime = (TextView) findViewById(R.id.break_start_time_text_user);
		Bundle bundle = getIntent().getExtras();
		String time ="";
		String overridenTime = "";
		if(bundle!=null && bundle.containsKey(Constants.TIME)){
			time = bundle.getString(Constants.TIME);
		}
		
		mBreakTime.append(time+" (System)");
		mContext = EndBreakActivity.this;
		if(!isAlreadyOveridden()){
			if(bundle!=null && bundle.containsKey(Constants.IS_OVERRIDEN) && bundle.getString(Constants.IS_OVERRIDEN).equalsIgnoreCase(ActivityConstants.TRUE)){
				overridenTime = bundle.getString(Constants.OVERRIDE_TIME);
				overriddenBreakStartTime.setVisibility(View.VISIBLE);
				overriddenBreakStartTime.setText(overridenTime +" (User)");
				saveOverrideAndActualBreakStartTimeInFlagDB(time,overridenTime,ActivityConstants.TRUE);
				time = overridenTime;
			} else{
				saveOverrideAndActualBreakStartTimeInFlagDB(time,overridenTime,ActivityConstants.FALSE);
			}
		}
		mEndBreak.setOnClickListener(this);
		
		
		
		saveBreakShftCAllTravelDataOnStartUp(time);
		
	}

	private void saveOverrideAndActualBreakStartTimeInFlagDB(String actualTime,String overridenTime, String isOVeridden) {
		String flagJSON = JobViewerDBHandler.getJSONFlagObject(mContext);
		try{
			JSONObject jsonObject = new JSONObject(flagJSON);
			jsonObject.put(Constants.ACTUAL_BREAK_START_TIME_FLAG_JSON, actualTime);
			jsonObject.put(Constants.OVERRIDE_BREAK_START_TIME_FLAG_JSON, overridenTime);
			jsonObject.put(Constants.IS_OVERRIDEN_FLAG_JSON, isOVeridden);
			JobViewerDBHandler.saveFlaginJSONObject(mContext, jsonObject.toString());
		}catch(JSONException jse){
			
		}
		
	}
	
	private boolean isAlreadyOveridden(){
		String flagJSON = JobViewerDBHandler.getJSONFlagObject(mContext);
		String isOveridden = "";
		try{
			JSONObject jsonObject = new JSONObject(flagJSON);		
			isOveridden = jsonObject.getString(Constants.IS_OVERRIDEN_FLAG_JSON);
			
		}catch(JSONException jse){
			
		}
		return isOveridden.equalsIgnoreCase(ActivityConstants.TRUE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		String flagJSON = JobViewerDBHandler.getJSONFlagObject(mContext);
		try{
			JSONObject jsonObject = new JSONObject(flagJSON);
			String actualBreakStart = jsonObject.getString(Constants.ACTUAL_BREAK_START_TIME_FLAG_JSON);
			mBreakTime.setText(actualBreakStart+" (System)");
			String isOveridden = jsonObject.getString(Constants.IS_OVERRIDEN_FLAG_JSON);
			String overiddenTime = "";
			if(isOveridden.equalsIgnoreCase(ActivityConstants.TRUE)){
				overiddenTime = jsonObject.getString(Constants.OVERRIDE_BREAK_START_TIME_FLAG_JSON);
				overriddenBreakStartTime.setVisibility(View.VISIBLE);
				overriddenBreakStartTime.setText(overiddenTime +" (User)");
			}
			
		}catch(JSONException jse){
			
		}
		
	}
	private void saveBreakShftCAllTravelDataOnStartUp(String time) {
		breakTravelShiftCallTable = JobViewerDBHandler.getBreakShiftTravelCall(mContext);
		if(breakTravelShiftCallTable == null){
			breakTravelShiftCallTable = new BreakShiftTravelCall();
		}
		breakTravelShiftCallTable.setBreakStarted(Constants.YES_CONSTANT);
		breakTravelShiftCallTable.setBreakStartedTime(time);
		
		JobViewerDBHandler.saveBreakShiftTravelCall(mContext, breakTravelShiftCallTable);
	}

	@Override
	public void onClick(View view) {

		if (view == mEndBreak) {
			Utils.endTimeRequest = new TimeSheetRequest();
			new showTimeDialog(this, this, "End Break").show();
		}

	}

	@Override
	public void onContinue() {
		if (Utils.isInternetAvailable(mContext)) {
			executeEndBreakService();
		} else {
			JobViewerDBHandler.saveTimeSheet(mContext, Utils.endTimeRequest,
					CommsConstant.END_BREAK_API);
			Utils.saveTimeSheetInBackLogTable(mContext, Utils.endTimeRequest, CommsConstant.END_BREAK_API, "TimeSheetServiceRequests");
			startHomePage();
		}

	}

	@Override
	public void onDismiss() {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.RESULT_CODE_CHANGE_TIME
				&& resultCode == RESULT_OK) {
			if (ActivityConstants.TRUE.equalsIgnoreCase(Utils.endTimeRequest
					.getIs_overriden())) {
				Intent intent = new Intent(this, OverrideReasoneDialog.class);
				intent.putExtra("eventType", eventType);
				startActivityForResult(intent,
						Constants.RESULT_CODE_OVERRIDE_COMMENT);
			}
		} else if (requestCode == Constants.RESULT_CODE_OVERRIDE_COMMENT
				&& resultCode == RESULT_OK) {

			if (Utils.isInternetAvailable(mContext)) {
				executeEndBreakService();
			} else {
				Utils.saveTimeSheetInBackLogTable(EndBreakActivity.this,
						Utils.endTimeRequest, CommsConstant.END_BREAK_API,
						Utils.REQUEST_TYPE_WORK);
				JobViewerDBHandler.saveTimeSheet(this, Utils.endTimeRequest,
						CommsConstant.HOST + CommsConstant.END_BREAK_API);				
				
				startHomePage();
			}
		}
	}

	private void executeEndBreakService() {
		TimeSheetRequest endTimeRequest = null;
		String api;
		endTimeRequest = Utils.endTimeRequest;
		api = CommsConstant.END_BREAK_API;
		ContentValues data = new ContentValues();
		data.put("started_at", endTimeRequest.getStarted_at());
		data.put("record_for", endTimeRequest.getRecord_for());
		data.put("is_inactive", endTimeRequest.getIs_inactive());
		data.put("is_overriden", endTimeRequest.getIs_overriden());
		data.put("override_reason", endTimeRequest.getOverride_reason());
		data.put("override_comment", endTimeRequest.getOverride_comment());
		data.put("override_timestamp", endTimeRequest.getOverride_timestamp());
		data.put("reference_id", endTimeRequest.getReference_id());
		data.put("user_id", endTimeRequest.getUser_id());
		
		Utils.SendHTTPRequest(this, CommsConstant.HOST + api, data,
				getEndBreakHandler());
	}

	private Handler getEndBreakHandler() {
		Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					String result = (String) msg.obj;					
					String jsonStr = JobViewerDBHandler.getJSONFlagObject(mContext);
					try{
					JSONObject jsonObject = new JSONObject(jsonStr);
					jsonObject.remove(Constants.ACTUAL_BREAK_START_TIME_FLAG_JSON);
					jsonObject.remove(Constants.IS_OVERRIDEN_FLAG_JSON);
					jsonObject.remove(Constants.OVERRIDE_BREAK_START_TIME_FLAG_JSON);
					JobViewerDBHandler.saveFlaginJSONObject(mContext, jsonObject.toString());
					}catch (JSONException e) {
						// TODO: handle exception
					}
					startHomePage();
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(mContext, exception, "Info");
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

	private void startHomePage() {
		
		if(saveBreakEndDB()){
			Intent homePageIntent = new Intent(EndBreakActivity.this,
					ActivityPageActivity.class);
			finish();
			BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(this);
			startActivity(homePageIntent);
		}
	}
	
	private boolean saveBreakEndDB() {
		breakTravelShiftCallTable.setBreakEndTime(Utils.getMillisFromFormattedDate(Utils.endTimeRequest.getStarted_at()));
		
		if(Utils.endTimeRequest.getIs_overriden().equalsIgnoreCase(ActivityConstants.TRUE)){
			breakTravelShiftCallTable.setBreakEndTime(Utils.getMillisFromFormattedDate(Utils.endTimeRequest.getOverride_timestamp()));
		}
		int numberOfBreaks =0;
		try{
			numberOfBreaks = breakTravelShiftCallTable.getNoOfBreaks();
		}catch(Exception e){
			numberOfBreaks = 0;
		}
		numberOfBreaks = numberOfBreaks+1;
		breakTravelShiftCallTable.setNoOfBreaks(numberOfBreaks);
		breakTravelShiftCallTable.setBreakStarted(Constants.NO_CONSTANT);
		JobViewerDBHandler.saveBreakShiftTravelCall(mContext, breakTravelShiftCallTable);
		
		
		return true;
	}
	
	@Override
	public void onBackPressed() {
		closeApplication();
	}

	
}
