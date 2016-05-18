package com.lanesgroup.jobviewer.fragment;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.util.ConfirmStopDialog;
import com.jobviewer.util.ConfirmStopDialog.ConfirmStopWork;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.R;
import com.vehicle.communicator.HttpConnection;

public class StopFragment extends Fragment implements OnClickListener,
		ConfirmStopWork {

	private View mRootView;
	private Button mStopButton;
	private Button mResumeButton;
	private ConfirmStopDialog mConfirmStopDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mRootView = inflater.inflate(R.layout.stop_work_screen, container,
				false);
		mStopButton = (Button) mRootView.findViewById(R.id.button1);
		mResumeButton = (Button) mRootView.findViewById(R.id.button2);
		mStopButton.setOnClickListener(this);
		mResumeButton.setOnClickListener(this);
		return mRootView;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == mStopButton.getId()) {
			// Popup dialog to report to field manager
			//
			mConfirmStopDialog = new ConfirmStopDialog(v.getContext(),
					StopFragment.this, Constants.END_TRAINING);
			mConfirmStopDialog.show();

		} else if (v.getId() == mResumeButton.getId()) {
			QuestionManager.getInstance().loadPreviousFragmentOnResume();

		}
	}

	private void startEndMethod() {
		Intent appPageActivityIntent = new Intent(getActivity(),
				ActivityPageActivity.class);
		appPageActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(appPageActivityIntent);
	}

	@Override
	public void onConfirmStopWork(String reason) {
		Log.d(Utils.LOG_TAG, "reason for stopping work " + reason);
		sendWorkCompletedToServer();
	}

	@Override
	public void onDismissStopWork() {
		mConfirmStopDialog.dismiss();
	}

	private void sendWorkCompletedToServer() {
		ContentValues data = new ContentValues();
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
		CheckOutObject checkOutRemember2 = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		insertWorkEndTimeIntoHoursCalculator();
		data.put("started_at", checkOutRemember2.getJobStartedTime());
		if (checkOutRemember.getVistecId() != null) {
			data.put("reference_id", checkOutRemember.getVistecId());
		} else {
			data.put("reference_id", "");
		}
		data.put("engineer_id", Utils.work_engineer_id);
		data.put("status", Utils.work_status_stopped);
		data.put("completed_at", Utils.getCurrentDateAndTime());
		data.put("activity_type", "");
		if (Utils.isNullOrEmpty(Utils.work_flooding_status)) {
			data.put("flooding_status", "");
		} else
			data.put("flooding_status", Utils.work_flooding_status);
		data.put("DA_call_out", Utils.work_DA_call_out);
		data.put("is_redline_captured", false);
		GPSTracker tracker = new GPSTracker(getActivity());
		data.put("location_latitude", tracker.getLatitude());
		data.put("location_longitude", tracker.getLongitude());
		data.put("created_by", userProfile.getEmail());
		Utils.startProgress(getActivity());
		try {
			Utils.work_id = checkOutRemember.getWorkId();
		} catch (Exception e) {
		}
		Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
				+ CommsConstant.WORK_UPDATE_API + "/" + Utils.work_id, data,
				getWorkCompletedHandler());
	}

	private void insertWorkEndTimeIntoHoursCalculator() {
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler
				.getBreakShiftTravelCall(getActivity());
		breakShiftTravelCall.setWorkEndTime(String.valueOf(System
				.currentTimeMillis()));
		JobViewerDBHandler.saveBreakShiftTravelCall(getActivity(),
				breakShiftTravelCall);
	}

	private Handler getWorkCompletedHandler() {

		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					startEndMethod();
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(getActivity(), exception,
							"Info");
					break;
				default:
					break;
				}
			}

		};
		return handler;
	}
}
