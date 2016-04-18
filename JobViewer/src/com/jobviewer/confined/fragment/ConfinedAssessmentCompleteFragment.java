package com.jobviewer.confined.fragment;

import android.app.Fragment;
import android.content.ContentValues;
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
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.confined.ConfinedQuestionManager;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.fragment.ShoutOutActivity;
import com.raghu.ShoutOutBackLogRequest;
import com.vehicle.communicator.HttpConnection;

public class ConfinedAssessmentCompleteFragment extends Fragment implements
		OnClickListener {
	private View mRootView;
	private Button doneButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mRootView = inflater.inflate(R.layout.assessment_complete_fragment,
				container, false);
		TextView screenTitle=(TextView) mRootView.findViewById(R.id.screenTitle);
		screenTitle.setText(getResources().getString(R.string.confined_space_str));
		doneButton = (Button) mRootView.findViewById(R.id.doneButton);
		doneButton.setOnClickListener(this);
		return mRootView;
	}

	@Override
	public void onClick(View v) {
		ConfinedQuestionManager.getInstance().saveAssessment("confined");
		executeConfinedRiskAssessment();
	}

	private void executeConfinedRiskAssessment() {
		Utils.startProgress(getActivity());
		if (Utils.isInternetAvailable(getActivity())) {
			sendAssessmentRequestToServer();
		} else {
			saveInBackLogDB();
		}

	}

	private void saveInBackLogDB() {
		ShoutOutBackLogRequest shoutOutBackLogRequest = new ShoutOutBackLogRequest();
		CheckOutObject checkOutRemember2 = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
		shoutOutBackLogRequest.setWork_id(checkOutRemember2.getWorkId());
		shoutOutBackLogRequest.setSurvey_type("Confined Space Entry");
		shoutOutBackLogRequest.setRelated_type("Work");
		shoutOutBackLogRequest.setRelated_type_reference(checkOutRemember2
				.getVistecId());
		shoutOutBackLogRequest.setStarted_at(ShoutOutActivity.getStartedAt());
		shoutOutBackLogRequest.setCompleted_at(Utils.getCurrentDateAndTime());
		String encodeToJsonString = GsonConverter.getInstance()
				.encodeToJsonString(
						ConfinedQuestionManager.getInstance()
								.getQuestionMaster());
		shoutOutBackLogRequest.setSurvey_json(encodeToJsonString);
		shoutOutBackLogRequest.setCreated_by(userProfile.getEmail());
		shoutOutBackLogRequest.setStatus("Completed");
		GPSTracker gpsTracker = new GPSTracker(getActivity());
		shoutOutBackLogRequest.setLocation_latitude(""
				+ gpsTracker.getLatitude());
		shoutOutBackLogRequest.setLocation_longitude(""
				+ gpsTracker.getLongitude());
		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.HOST
				+ CommsConstant.WORK_CREATE_API);
		backLogRequest.setRequestClassName("ShoutOutBackLogRequest");
		backLogRequest.setRequestJson(GsonConverter.getInstance()
				.encodeToJsonString(shoutOutBackLogRequest));
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(getActivity(), backLogRequest);
		Utils.StopProgress();
		JobViewerDBHandler.deleteConfinedQuestionSet(getActivity());
		Utils.StopProgress();
		((BaseActivity) getActivity()).finish();

	}

	private void sendAssessmentRequestToServer() {
		ContentValues values = new ContentValues();
		CheckOutObject checkOutRemember2 = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
		if (Utils.isNullOrEmpty(checkOutRemember2.getWorkId())) {
			values.put("work_id", "");
		} else
			values.put("work_id", checkOutRemember2.getWorkId());
		values.put("survey_type", "Confined Space Entry");
		values.put("related_type", "Work");
		if (Utils.isNullOrEmpty(checkOutRemember2.getVistecId())) {
			values.put("related_type_reference", "");
		} else
			values.put("related_type_reference",
					checkOutRemember2.getVistecId());
		values.put("started_at", checkOutRemember2.getJobStartedTime());
		values.put("completed_at", Utils.getCurrentDateAndTime());
		values.put("created_by", userProfile.getEmail());
		values.put("status", "Completed");
		GPSTracker gpsTracker = new GPSTracker(getActivity());
		values.put("location_latitude", gpsTracker.getLatitude());
		values.put("location_longitude", gpsTracker.getLongitude());
		String encodeToJsonString = GsonConverter.getInstance()
				.encodeToJsonString(
						ConfinedQuestionManager.getInstance()
								.getQuestionMaster());
		values.put("survey_json", encodeToJsonString);
		Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
				+ CommsConstant.WORK_CREATE_API, values, getSendSurveyHandler());

	}

	private Handler getSendSurveyHandler() {

		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					JobViewerDBHandler.deleteConfinedQuestionSet(getActivity());

					getActivity().finish();
					Log.i("Android", "");
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
