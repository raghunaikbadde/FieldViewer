package com.lanesgroup.jobviewer.fragment;

import java.util.Date;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
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
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;
import com.jobviwer.service.RiskAssementOverTimeService;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.AddPhotosActivity;
import com.lanesgroup.jobviewer.PollutionActivity;
import com.lanesgroup.jobviewer.R;
import com.raghu.ShoutOutBackLogRequest;
import com.vehicle.communicator.HttpConnection;

public class AssessmentCompleteFragment extends Fragment implements
		OnClickListener {
	private View mRootView;
	private Button doneButton;
	public static PendingIntent asessmentAlarmIntent;
	private boolean shouldPollutionSkip = false;
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
		if (ActivityConstants.EXCAVATION.equalsIgnoreCase(checkOutRemember
				.getAssessmentSelected())) {
			screenTitle.setText(getActivity().getResources().getString(
					R.string.excavation_risk_str));
		} else {
			screenTitle.setText(getActivity().getResources().getString(
					R.string.non_excavation_risk_str));
		}
		doneButton = (Button) mRootView.findViewById(R.id.doneButton);
		doneButton.setOnClickListener(this);
		Bundle bundle = getActivity().getIntent().getExtras();
		if(bundle != null && bundle.containsKey(Constants.UPDATE_PREV_RISK_ASMT_FLAG_POLLUTION_SKIP)){
			if(bundle.getBoolean(Constants.UPDATE_PREV_RISK_ASMT_FLAG_POLLUTION_SKIP)){
				shouldPollutionSkip = true;
			}
		}
		return mRootView;
	}

	@Override
	public void onClick(View v) {
		QuestionManager.getInstance().saveAssessment("work");
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		checkOutRemember.setIsAssessmentCompleted("true");
		JobViewerDBHandler
				.saveCheckOutRemember(getActivity(), checkOutRemember);
		if (Utils.isInternetAvailable(getActivity())) {
			Utils.startProgress(getActivity());
			executeSendDataToServer();
		} else {
			saveInBackLogDB();
			initiateAlarm();
			setAlarmForOverTime();
			saveRiskAssesmentEndTime();
			
			if (ActivityConstants.TRUE.equalsIgnoreCase(checkOutRemember
					.getIsPollutionSelected())) {
				Intent pollutionIntent = new Intent(getActivity(),
						PollutionActivity.class);
				
				if(shouldPollutionSkip){
					pollutionIntent = new Intent(getActivity(),
							AddPhotosActivity.class);
				}
				startActivity(pollutionIntent);
			} else {
				Intent pollutionIntent = new Intent(getActivity(),
						AddPhotosActivity.class);
				startActivity(pollutionIntent);
			}
		}
	}

	private void saveRiskAssesmentEndTime() {
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(getActivity());
		Date date = new Date();
		long millis = date.getTime();
		String riskAssessmentEndTime = String.valueOf(millis);
		breakShiftTravelCall.setRiskAssessmentEndTime(riskAssessmentEndTime);
		JobViewerDBHandler.saveBreakShiftTravelCall(getActivity(), breakShiftTravelCall);
	}

	private void saveInBackLogDB() {
		ShoutOutBackLogRequest shoutOutBackLogRequest = new ShoutOutBackLogRequest();
		CheckOutObject checkOutRemember2 = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
		shoutOutBackLogRequest.setWork_id(checkOutRemember2.getWorkId());
		shoutOutBackLogRequest.setSurvey_type(checkOutRemember2
				.getAssessmentSelected());
		shoutOutBackLogRequest.setRelated_type("Work");
		shoutOutBackLogRequest.setRelated_type_reference(checkOutRemember2
				.getVistecId());
		shoutOutBackLogRequest.setStarted_at(checkOutRemember2
				.getJobStartedTime());
		shoutOutBackLogRequest.setCompleted_at(Utils.getCurrentDateAndTime());
		String encodeToJsonString = GsonConverter.getInstance()
				.encodeToJsonString(
						QuestionManager.getInstance().getQuestionMaster());
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
				+ CommsConstant.SURVEY_STORE_API);
		backLogRequest.setRequestClassName("ShoutOutBackLogRequest");
		backLogRequest.setRequestJson(GsonConverter.getInstance()
				.encodeToJsonString(shoutOutBackLogRequest));
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(getActivity(), backLogRequest);

	}

	private void executeSendDataToServer() {
		ContentValues values = new ContentValues();
		CheckOutObject checkOutRemember2 = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
		if (checkOutRemember2 == null
				|| Utils.isNullOrEmpty(checkOutRemember2.getWorkId())) {
			values.put("work_id", "");
		} else
			values.put("work_id", checkOutRemember2.getWorkId());
		values.put("survey_type", checkOutRemember2.getAssessmentSelected());
		values.put("related_type", "Work");
		if (checkOutRemember2 == null
				|| Utils.isNullOrEmpty(checkOutRemember2.getVistecId())) {
			values.put("related_type_reference", "");
		} else
			values.put("related_type_reference",
					checkOutRemember2.getVistecId());
		values.put("started_at", checkOutRemember2.getJobStartedTime());
		values.put("completed_at", Utils.getCurrentDateAndTime());

		values.put("created_by", userProfile.getEmail());
//		GPSTracker gpsTracker = new GPSTracker(getActivity());
		String encodeToJsonString = GsonConverter.getInstance()
				.encodeToJsonString(
						QuestionManager.getInstance().getQuestionMaster());
		values.put("survey_json", encodeToJsonString);
		Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
				+ CommsConstant.SURVEY_STORE_API, values, getSendSurveyHandler());

	}

	private Handler getSendSurveyHandler() {

		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:

					String result = (String) msg.obj;
					Log.i("Android", result);
					CheckOutObject checkOutRemember = JobViewerDBHandler
							.getCheckOutRemember(getActivity());
					
					if (ActivityConstants.TRUE
							.equalsIgnoreCase(checkOutRemember
									.getIsPollutionSelected())) {						
						Intent pollutionIntent = new Intent(getActivity(),
								PollutionActivity.class);
						if(shouldPollutionSkip){
							pollutionIntent = new Intent(getActivity(),
									AddPhotosActivity.class);
						}
						startActivity(pollutionIntent);
					} else {
						Intent pollutionIntent = new Intent(getActivity(),
								AddPhotosActivity.class);
						startActivity(pollutionIntent);
					}
					saveRiskAssesmentEndTime();
					initiateAlarm();
					setAlarmForOverTime();
					getActivity().finish();
					Utils.StopProgress();
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
	
	private void initiateAlarm() {
		Utils.updateRiskAssessmentOverTimeAlarmMgr = (AlarmManager) getActivity()
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getActivity(), RiskAssementOverTimeService.class);
		asessmentAlarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
	}
	
	private void setAlarmForOverTime() {
		Utils.updateRiskAssessmentOverTimeAlarmMgr.setInexactRepeating(
				AlarmManager.ELAPSED_REALTIME_WAKEUP,
				Utils.RISK_ASSMENET_OVETTIME_ALERT_TOGGLE, Utils.RISK_ASSMENET_OVETTIME_ALERT_INTERVAL,
				asessmentAlarmIntent);
	}
	
	public static void cancelAlarm() {
		if (Utils.updateRiskAssessmentOverTimeAlarmMgr != null) {			
			Utils.updateRiskAssessmentOverTimeAlarmMgr.cancel(AssessmentCompleteFragment.asessmentAlarmIntent);
		}
	}
}
