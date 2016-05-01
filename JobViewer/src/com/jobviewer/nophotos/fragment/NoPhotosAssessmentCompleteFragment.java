package com.jobviewer.nophotos.fragment;

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
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.nophotos.WorkWithNoPhotosQuestionManager;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.fragment.ShoutOutActivity;
import com.vehicle.communicator.HttpConnection;

public class NoPhotosAssessmentCompleteFragment extends Fragment implements
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
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		TextView screenTitle = (TextView) mRootView
				.findViewById(R.id.screenTitle);

		TextView questionTitle = (TextView) mRootView
				.findViewById(R.id.questionTitle);
		questionTitle.setText(getResources().getString(
				R.string.persoal_risk_assement_complete_str));
		// TextView question = (TextView) mRootView.findViewById(R.id.question);
		questionTitle.setText(getResources().getString(
				R.string.persoal_riskassement_continue_str));

		TextView tapDone = (TextView) mRootView.findViewById(R.id.tapDone);
		tapDone.setVisibility(View.VISIBLE);

		if (ActivityConstants.EXCAVATION.equalsIgnoreCase(checkOutRemember
				.getAssessmentSelected())) {
			screenTitle.setText(getActivity().getResources().getString(
					R.string.persoal_risk_assement_str));
		} else {
			screenTitle.setText(getActivity().getResources().getString(
					R.string.persoal_risk_assement_str));
		}
		doneButton = (Button) mRootView.findViewById(R.id.doneButton);
		doneButton.setOnClickListener(this);
		return mRootView;
	}

	@Override
	public void onClick(View v) {
		WorkWithNoPhotosQuestionManager.getInstance().saveAssessment("work");
		sendDataToServer();
	}

	private void sendDataToServer() {
		if (Utils.isInternetAvailable(getActivity())) {
			Utils.startProgress(getActivity());
			ContentValues values = new ContentValues();
			CheckOutObject checkOutRemember2 = JobViewerDBHandler
					.getCheckOutRemember(getActivity());
			User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
			if (checkOutRemember2 == null
					|| Utils.isNullOrEmpty(checkOutRemember2.getWorkId())) {
				values.put("work_id", "");
			} else
				values.put("work_id", checkOutRemember2.getWorkId());
			values.put("survey_type", "Personal Risk Assessment");
			values.put("related_type", "Work");
			if (checkOutRemember2 == null
					|| Utils.isNullOrEmpty(checkOutRemember2.getVistecId())) {
				values.put("related_type_reference", "");
			} else
				values.put("related_type_reference",
						checkOutRemember2.getVistecId());
			values.put("started_at", ShoutOutActivity.getStartedAt());
			values.put("completed_at", Utils.getCurrentDateAndTime());

			values.put("created_by", userProfile.getEmail());
			values.put("status", "Completed");
			GPSTracker gpsTracker = new GPSTracker(getActivity());
			values.put("location_latitude", gpsTracker.getLatitude());
			values.put("location_longitude", gpsTracker.getLongitude());
			String encodeToJsonString = GsonConverter.getInstance()
					.encodeToJsonString(
							WorkWithNoPhotosQuestionManager.getInstance()
									.getQuestionMaster());
			values.put("survey_json", encodeToJsonString);
			Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
					+ CommsConstant.SURVEY_STORE_API, values,
					getSendSurveyHandler());
		} else {
			Intent homeIntent = new Intent(getActivity(),
					ActivityPageActivity.class);
			homeIntent.putExtra(Constants.WORK_NO_PHOTOS_HOME, true);
			startActivity(homeIntent);
			// saveInBackLog(obj);
		}

	}

	private Handler getSendSurveyHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:

					String result = (String) msg.obj;
					Log.i("Android", result);
					Utils.StopProgress();
					Intent homeIntent = new Intent(getActivity(),
							ActivityPageActivity.class);
					homeIntent.putExtra(Constants.WORK_NO_PHOTOS_HOME, true);
					startActivity(homeIntent);
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
