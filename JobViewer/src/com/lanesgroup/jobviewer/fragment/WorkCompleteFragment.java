package com.lanesgroup.jobviewer.fragment;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.WorkSuccessActivity;
import com.raghu.WorkRequest;
import com.vehicle.communicator.HttpConnection;

public class WorkCompleteFragment extends Fragment implements OnClickListener{
	
	private ProgressBar mProgress;
	private TextView mProgressStep,mVistecNumber;
	private ImageButton mAddInfo, mStop, mUser, mClickPhoto;
	private Button mSave, mLeaveSite;
	private LinearLayout mCaptureCallingCard;
	private View mRootView;
	private Spinner mSpinner,mSpinnerFlooding;
	private RelativeLayout mSpinnerLayout,mSpinnerLayoutFlooding;
	private TextView mSpinnerSelectedText;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			getActivity().getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			mRootView = inflater.inflate(R.layout.work_complete_screen, container,
					false); 
			Utils.workEndTimeSheetRequest = new TimeSheetRequest();
			return mRootView;
		}
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			// TODO Auto-generated method stub=
			super.onActivityCreated(savedInstanceState);
			initUI();
		}
		private void initUI() {
			mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
			mProgressStep = (TextView) mRootView.findViewById(R.id.progress_step_text);
			mVistecNumber = (TextView) mRootView.findViewById(R.id.vistec_number_text);
			//mVistecNumber.setText(Utils.checkOutObject.getVistecId());
			mAddInfo = (ImageButton) mRootView.findViewById(R.id.detail_imageButton);
			mStop = (ImageButton) mRootView.findViewById(R.id.video_imageButton);
			mUser = (ImageButton) mRootView.findViewById(R.id.user_imageButton);
			mSpinner = (Spinner) mRootView.findViewById(R.id.spinner1);
			mSpinnerFlooding = (Spinner) mRootView.findViewById(R.id.spinner_flood);
			mSpinnerLayout = (RelativeLayout) mRootView.findViewById(R.id.spinnerLayout);
			mSpinnerLayoutFlooding = (RelativeLayout) mRootView.findViewById(R.id.spinnerLayout_flood);
			mSpinnerSelectedText = (TextView) mRootView.findViewById(R.id.spinner_selected);
			
			mSpinnerLayout.setClickable(true);
			mSpinnerLayout.setOnClickListener(this);
			mSpinnerLayoutFlooding.setClickable(true);
			mSpinnerLayoutFlooding.setOnClickListener(this);
			mProgress.setMax(6);
			mProgress.setProgress(5);
			mProgressStep.setText("Step 5 of 6");
			mCaptureCallingCard = (LinearLayout) mRootView.findViewById(R.id.customer_calling_layout);
			mCaptureCallingCard.setClickable(true);
			mCaptureCallingCard.setOnClickListener(this);
			mSave = (Button) mRootView.findViewById(R.id.button1);
			mLeaveSite = (Button) mRootView.findViewById(R.id.button2);
			mLeaveSite.setOnClickListener(this);
			enableNextButton(true);
		}
		@Override
		public void onClick(View view) {
			if(view == mSave){
				
			}else if(view == mLeaveSite){
				//Upload Photos here// if calling card available
				
				if(Utils.isInternetAvailable(getActivity())){
					sendWorkEndTimeSheetToServer();
				} else {
					prepareWorkCompletedRequest();
					storeWorkEndTimeSheetInBackLogDB();
					startWorkSuccessActivity();
				}
				
								
								
			}else if(view == mCaptureCallingCard){
				Intent intent = new Intent(Constants.IMAGE_CAPTURE_ACTION);
				startActivityForResult(intent, Constants.RESULT_CODE);
			} else if(view == mSpinnerLayout){
				openDialog();
			} else if(view == mSpinnerLayoutFlooding){
				openDialog();
			}
		}
		
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
	    	if (requestCode == 500  && resultCode == getActivity().RESULT_OK){
	    		
	    	}
		}
		
		public void openDialog() {
	    	String header = getResources().getString(R.string.activity_type);
	    	Utils.dailogboxSelector(getActivity(), Utils.mActivityList, R.layout.work_complete_dialog, mSpinnerSelectedText, header);
	    } 
		public void enableNextButton(boolean isEnable) {
			if (isEnable) {
				mLeaveSite.setEnabled(true);
				mLeaveSite.setBackgroundResource(R.drawable.red_background);
			} else {
				mLeaveSite.setEnabled(false);
				mLeaveSite.setBackgroundResource(R.drawable.dark_grey_background);
			}

		}
		
		private WorkRequest prepareWorkCompletedRequest(){
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(getActivity());
			User userProfile = JobViewerDBHandler
					.getUserProfile(getActivity());
			WorkRequest workRequest = new WorkRequest();
			workRequest.setStarted_at(Utils.lastest_work_started_at);
			if (checkOutRemember.getVistecId() != null) {
				workRequest.setReference_id(checkOutRemember.getVistecId());
			} else {
				workRequest.setReference_id("");
			}
			workRequest.setEngineer_id(Utils.work_engineer_id);
			workRequest.setStatus(Utils.work_status);
			workRequest.setCompleted_at(Utils.work_completed_at);
			workRequest.setActivity_type("");
			workRequest.setFlooding_status(Utils.work_flooding_status);
			workRequest.setDA_call_out(Utils.work_DA_call_out);
			workRequest.setIs_redline_captured(Utils.work_is_redline_captured);
			GPSTracker tracker = new GPSTracker(getActivity());
			workRequest.setLocation_latitude("" + tracker.getLatitude());
			workRequest.setLocation_longitude("" + tracker.getLongitude());
			workRequest.setCreated_by(userProfile.getEmail());
			BackLogRequest backLogRequest = new BackLogRequest();
			backLogRequest.setRequestApi(CommsConstant.HOST + "/"+CommsConstant.WORK_UPDATE_API+"/"+Utils.work_id);
			backLogRequest.setRequestClassName("WorkRequest");
			backLogRequest.setRequestJson(workRequest.toString());
			backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
			JobViewerDBHandler.saveBackLog(getActivity(), backLogRequest);
			return workRequest;
		}
		
		private void sendWorkEndTimeSheetToServer(){
			ContentValues data = new ContentValues();
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(getActivity());
			User userProfile = JobViewerDBHandler
					.getUserProfile(getActivity());
			Utils.workEndTimeSheetRequest.setRecord_for(userProfile.getEmail());
			Utils.workEndTimeSheetRequest.setIs_inactive("false");
			Utils.workEndTimeSheetRequest.setOverride_reason("");
			Utils.workEndTimeSheetRequest.setOverride_comment("");
			Utils.workEndTimeSheetRequest.setOverride_timestamp(Utils.getCurrentDateAndTime());
			Utils.workEndTimeSheetRequest.setReference_id(checkOutRemember.getVistecId());
			Utils.workEndTimeSheetRequest.setUser_id(userProfile.getEmail());
			CheckOutObject checkOutRemember2 = JobViewerDBHandler.getCheckOutRemember(getActivity());
			data.put("started_at", checkOutRemember2.getJobStartedTime());
			data.put("record_for", userProfile.getEmail());
			data.put("is_inactive", "false");
			data.put("override_reason", "");
			data.put("override_comment", "");
			data.put("override_timestamp", Utils.workEndTimeSheetRequest.getOverride_timestamp());
			data.put("reference_id", checkOutRemember.getVistecId());
			data.put("user_id", userProfile.getEmail());
			Utils.startProgress(getActivity());
			Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
					+ CommsConstant.END_WORK_API, data, getWorkTimeSheetSubmitHandler());

		}
		private void sendWorkCompletedToServer(){
			ContentValues data = new ContentValues();
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(getActivity());
			User userProfile = JobViewerDBHandler
					.getUserProfile(getActivity());
			CheckOutObject checkOutRemember2 = JobViewerDBHandler.getCheckOutRemember(getActivity());
			data.put("started_at",checkOutRemember2.getJobStartedTime());
			if (checkOutRemember.getVistecId() != null) {
				data.put("reference_id", checkOutRemember.getVistecId());
			} else {
				data.put("reference_id", "");
			}
			data.put("engineer_id", Utils.work_engineer_id);
			data.put("status", Utils.work_status);
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
			data.put("location_longitude", tracker.getLatitude());
			data.put("created_by", userProfile.getEmail());
			Utils.startProgress(getActivity());
			Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
					+ CommsConstant.WORK_UPDATE_API+"/"+Utils.work_id, data, getWorkCompletedHandler());
		}
		
		private Handler getWorkCompletedHandler() {

			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case HttpConnection.DID_SUCCEED:
						
						Utils.StopProgress();
						startWorkSuccessActivity();
						break;
					case HttpConnection.DID_ERROR:
						Utils.StopProgress();
						String error = (String) msg.obj;
						VehicleException exception = GsonConverter
								.getInstance()
								.decodeFromJsonString(error, VehicleException.class);
						ExceptionHandler.showException(getActivity(), exception, "Info");
						prepareWorkCompletedRequest();
						storeWorkEndTimeSheetInBackLogDB();
						break;
					default:
						break;
					}
				}

			};
			return handler;
		}
		
		private Handler getWorkTimeSheetSubmitHandler() {

			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case HttpConnection.DID_SUCCEED:
						
						
						if(Utils.isInternetAvailable(getActivity())){
							sendWorkCompletedToServer();
						} else {
							Utils.StopProgress();
							prepareWorkCompletedRequest();							
						}
						break;
					case HttpConnection.DID_ERROR:
						Utils.StopProgress();
						String error = (String) msg.obj;
						VehicleException exception = GsonConverter
								.getInstance()
								.decodeFromJsonString(error, VehicleException.class);
						ExceptionHandler.showException(getActivity(), exception, "Info");
						prepareWorkCompletedRequest();
						storeWorkEndTimeSheetInBackLogDB();
						break;
					default:
						break;
					}
				}

			};
			return handler;
		}
		
		protected void storeWorkEndTimeSheetInBackLogDB() {

			Utils.saveTimeSheetInBackLogTable(getActivity(), Utils.workEndTimeSheetRequest, CommsConstant.END_WORK_API, Utils.REQUEST_TYPE_TIMESHEET);
			
		}
		private void startWorkSuccessActivity() {
			Intent workSuccessIntent = new Intent(getActivity(),WorkSuccessActivity.class);
			startActivity(workSuccessIntent);
		}
		
}
