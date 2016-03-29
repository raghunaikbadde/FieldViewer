package com.lanesgroup.jobviewer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
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
import com.raghu.TimeSheetServiceRequests;
import com.vehicle.communicator.HttpConnection;

public class TravelToWorkSiteActivity extends BaseActivity implements
		OnClickListener, DialogCallback {

	private Button mStartTravel, mCancel;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		Utils.startTravelTimeRequest = new TimeSheetRequest();
		setContentView(R.layout.travel_to_work_site_screen);
		mStartTravel = (Button) findViewById(R.id.start);
		mStartTravel.setOnClickListener(this);
		mCancel = (Button) findViewById(R.id.button1);
		mCancel.setOnClickListener(this);
	}

	@Override
	public void onContinue() {
		if (Utils.isInternetAvailable(mContext)) {
			JobViewerDBHandler.saveTimeSheet(mContext,
					Utils.startTravelTimeRequest,
					CommsConstant.START_TRAVEL_API);
			executeStartTravelService();
		} else {
			JobViewerDBHandler.saveTimeSheet(mContext,
					Utils.startTravelTimeRequest,
					CommsConstant.START_TRAVEL_API);
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(mContext);
			checkOutRemember.setIsStartedTravel("true");
			JobViewerDBHandler.saveCheckOutRemember(mContext, checkOutRemember);
			Intent intent = new Intent(TravelToWorkSiteActivity.this,
					EndTravelActivity.class);
			intent.putExtra(Constants.STARTED, Constants.TRAVEL_STARTED);
			intent.putExtra(Constants.TIME,
					Utils.startTravelTimeRequest.getStarted_at());
			startActivity(intent);
		}
	}

	@Override
	public void onDismiss() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View view) {
		if (view == mStartTravel)
			new showTimeDialog(this, this, "travel").show();
		else if (view == mCancel)
			finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.RESULT_CODE_CHANGE_TIME
				&& resultCode == RESULT_OK) {
			if (Utils.timeSheetRequest != null
					&& ActivityConstants.TRUE
							.equalsIgnoreCase(Utils.timeSheetRequest
									.getIs_overriden())) {
				Intent intent = new Intent(this, OverrideReasoneDialog.class);
				intent.putExtra("eventType", Constants.BREAK_STARTED);
				startActivityForResult(intent,
						Constants.RESULT_CODE_OVERRIDE_COMMENT);
			} else if (Utils.startTravelTimeRequest != null
					&& ActivityConstants.TRUE
							.equalsIgnoreCase(Utils.startTravelTimeRequest
									.getIs_overriden())) {
				Intent intent = new Intent(this, OverrideReasoneDialog.class);
				intent.putExtra("eventType", Constants.TRAVEL_STARTED);
				startActivityForResult(intent,
						Constants.RESULT_CODE_OVERRIDE_COMMENT);
			} else {
				startEndActvity();
				String time = data.getExtras().get(Constants.TIME).toString();
				Intent intent = new Intent(this, EndTravelActivity.class);
				intent.putExtra(Constants.STARTED, Constants.TRAVEL_STARTED);
				intent.putExtra(Constants.TIME, time);
				startActivity(intent);
			}

		} else if (requestCode == Constants.RESULT_CODE_OVERRIDE_COMMENT
				&& resultCode == RESULT_OK) {

			if (!Utils.isInternetAvailable(this)) {
				JobViewerDBHandler.saveTimeSheet(this,
						Utils.startTravelTimeRequest,
						CommsConstant.START_TRAVEL_API);
				JobViewerDBHandler.getAllTimeSheet(mContext);
				Utils.saveTimeSheetInBackLogTable(
						TravelToWorkSiteActivity.this,
						Utils.startTravelTimeRequest,
						CommsConstant.START_TRAVEL_API, Utils.REQUEST_TYPE_WORK);
				// saveStartTravelInBackLogDb();

				startEndActvity();
			} else {
				executeStartTravelService();
			}
		}
	}

	private void executeStartTravelService() {
		ContentValues data = new ContentValues();
		data.put("started_at", Utils.startTravelTimeRequest.getStarted_at());
		data.put("record_for", Utils.startTravelTimeRequest.getRecord_for());
		data.put("is_inactive", Utils.startTravelTimeRequest.getIs_inactive());
		data.put("is_overriden", Utils.startTravelTimeRequest.getIs_overriden());
		data.put("override_reason",
				Utils.startTravelTimeRequest.getOverride_reason());
		data.put("override_comment",
				Utils.startTravelTimeRequest.getOverride_comment());
		data.put("override_timestamp",
				Utils.startTravelTimeRequest.getOverride_timestamp());
		data.put("reference_id", Utils.startTravelTimeRequest.getReference_id());
		data.put("user_id", Utils.startTravelTimeRequest.getUser_id());
		Utils.startProgress(this);
		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.START_TRAVEL_API, data, getStartTravelHandler());

	}

	private Handler getStartTravelHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					// String result = (String) msg.obj;
					CheckOutObject checkOutRemember = JobViewerDBHandler
							.getCheckOutRemember(mContext);
					checkOutRemember.setIsStartedTravel("true");
					JobViewerDBHandler.saveCheckOutRemember(mContext,
							checkOutRemember);
					startEndActvity();
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(mContext, exception, "Info");
					Utils.saveTimeSheetInBackLogTable(
							TravelToWorkSiteActivity.this,
							Utils.startTravelTimeRequest,
							CommsConstant.START_TRAVEL_API,
							Utils.REQUEST_TYPE_WORK);
					// saveStartTravelInBackLogDb();
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

	private void startEndActvity() {
		Intent intent = new Intent(this, EndTravelActivity.class);
		intent.putExtra(Constants.STARTED, Constants.TRAVEL_STARTED);
		if (Utils.isNullOrEmpty(Utils.startTravelTimeRequest
				.getOverride_timestamp())) {
			intent.putExtra(Constants.TIME,
					Utils.startTravelTimeRequest.getStarted_at());
		} else {
			intent.putExtra(Constants.TIME,
					Utils.startTravelTimeRequest.getOverride_timestamp());
		}
		startActivity(intent);

	}

}
