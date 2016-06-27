package com.jobviewer.confined.fragment;

import android.app.Fragment;
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
import com.jobviewer.confined.ConfinedQuestionManager;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.AddPhotosActivity;
import com.lanesgroup.jobviewer.R;
import com.raghu.ShoutOutBackLogRequest;
import com.vehicle.communicator.HttpConnection;

import org.json.JSONObject;

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
		shoutOutBackLogRequest.setStarted_at(getConfinedSpaceAsessementStartedTime(getActivity()));
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
				+ CommsConstant.SURVEY_STORE_API);
		backLogRequest.setRequestClassName("ShoutOutBackLogRequest");
		backLogRequest.setRequestJson(GsonConverter.getInstance()
				.encodeToJsonString(shoutOutBackLogRequest));
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(getActivity(), backLogRequest);
		Utils.StopProgress();
		JobViewerDBHandler.deleteConfinedQuestionSet(getActivity());
		JobViewerDBHandler.deleteWorkWithNoPhotosQuestionSet(getActivity());
		Utils.StopProgress();
		getActivity().finish();
		Intent intent = new Intent(mRootView.getContext(),ActivityPageActivity.class);
		startActivity(intent);

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
		values.put("started_at", getConfinedSpaceAsessementStartedTime(getActivity()));
		values.put("completed_at", Utils.getCurrentDateAndTime());
		values.put("created_by", userProfile.getEmail());
		
		//GPSTracker gpsTracker = new GPSTracker(getActivity());
		
		String encodeToJsonString = GsonConverter.getInstance()
				.encodeToJsonString(
						ConfinedQuestionManager.getInstance()
								.getQuestionMaster());
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
					Utils.StopProgress();
					JobViewerDBHandler.deleteConfinedQuestionSet(getActivity());
					ConfinedEngineerFragment.engineerName1 = null;
					ConfinedEngineerFragment.engineerName2 = null;
					ConfinedEngineerFragment.engineerName3 = null;
					ConfinedEngineerFragment.gasLevel1 = null;
					ConfinedEngineerFragment.gasLevel2 = null;
					ConfinedEngineerFragment.gasLevel3 = null;
					ConfinedEngineerFragment.gasLevel4 = null;
					JobViewerDBHandler.deleteConfinedQuestionSet(getActivity());
					JobViewerDBHandler.deleteWorkWithNoPhotosQuestionSet(getActivity());
					getActivity().finish();
					if(Utils.isConfinedStartedFromAddPhoto(getActivity())){
						Intent addPhotosIntent = new Intent(mRootView.getContext(),AddPhotosActivity.class);
						startActivity(addPhotosIntent);
					}else{
						Intent intent = new Intent(mRootView.getContext(),ActivityPageActivity.class);
						startActivity(intent);
					}
					removeConfinedSpaceAsessementStartedTime(getActivity());
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
	
	private String getConfinedSpaceAsessementStartedTime(Context mContext){
		String str = JobViewerDBHandler.getJSONFlagObject(mContext);
		if(Utils.isNullOrEmpty(str)){
			str = "{}";
		}
		try{
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.has(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME)){				
				return  jsonObject.getString(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME);
			}
			CheckOutObject checkOutRemember2 = JobViewerDBHandler
					.getCheckOutRemember(getActivity());
			if(!Utils.isNullOrEmpty(checkOutRemember2.getJobStartedTime()))
				return checkOutRemember2.getJobStartedTime();
		}catch(Exception e){
			e.printStackTrace();
		}
		return Utils.getCurrentDateAndTime();
	}
	
	private void removeConfinedSpaceAsessementStartedTime(Context mContext){
		String str = JobViewerDBHandler.getJSONFlagObject(mContext);
		if(Utils.isNullOrEmpty(str)){
			str = "{}";
		}
		try{
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.has(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME)){
				jsonObject.remove(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME);
				String jsonString = jsonObject.toString();
				JobViewerDBHandler.saveFlaginJSONObject(getActivity(), jsonString);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
