package com.lanesgroup.jobviewer.fragment;

import java.io.File;
import java.io.IOException;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GeoLocationCamera;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ConfirmDialog;
import com.jobviewer.util.ConfirmDialog.ConfirmDialogCallback;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.WorkSuccessActivity;
import com.raghu.WorkRequest;
import com.vehicle.communicator.HttpConnection;

public class WorkCompleteFragment extends Fragment implements OnClickListener,ConfirmDialogCallback,TextWatcher {

	private ProgressBar mProgress;
	private TextView mProgressStep, mVistecNumber;
	private ImageButton mAddInfo, mStop, mUser, mClickPhoto;
	private Button mSave, mLeaveSite;
	private LinearLayout mCaptureCallingCard,mTapToCallDa;
	private View mRootView;
	private RelativeLayout mSpinnerLayout, mSpinnerLayoutFlooding;
	private TextView mSpinnerSelectedText, mSpinnerSelectedFloodedText;
	private EditText mPipeDiameterEditText,mPipeLengthEditText;
	private RadioButton radioOne,radioTwo;
	private RadioGroup radioGroup;
	static File file;	
	private String mPipeDiameter,mPipeLength;
	private String selectedActivityText = "";
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
		super.onActivityCreated(savedInstanceState);
		initUI();
	}
	private void radioButtonChangedListeners() {
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(validateUserInputs()){
					enableNextButton(true);
				} else{
					enableNextButton(false);
				}
			}
		});
	}

	private void initUI() {
		mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
		mProgressStep = (TextView) mRootView
				.findViewById(R.id.progress_step_text);
		mVistecNumber = (TextView) mRootView
				.findViewById(R.id.vistec_number_text);
		if(Utils.checkOutObject == null){
			Utils.checkOutObject = JobViewerDBHandler.getCheckOutRemember(getActivity());
		}
		mPipeDiameterEditText = (EditText) mRootView.findViewById(R.id.enter_pipe_edittext);
		mPipeLengthEditText = (EditText) mRootView.findViewById(R.id.enter_length_edittext);
		mPipeDiameterEditText.addTextChangedListener(this);
		mPipeLengthEditText.addTextChangedListener(this);
		mVistecNumber.setText(Utils.checkOutObject.getVistecId());
		mAddInfo = (ImageButton) mRootView
				.findViewById(R.id.detail_imageButton);
		mStop = (ImageButton) mRootView.findViewById(R.id.video_imageButton);
		mUser = (ImageButton) mRootView.findViewById(R.id.user_imageButton);
		mSpinnerLayout = (RelativeLayout) mRootView
				.findViewById(R.id.spinnerLayout);
		mSpinnerLayoutFlooding = (RelativeLayout) mRootView
				.findViewById(R.id.spinnerLayout_flood);
		mSpinnerSelectedText = (TextView) mRootView
				.findViewById(R.id.spinner_selected);
		mSpinnerSelectedFloodedText = (TextView) mRootView
				.findViewById(R.id.spinner_selected_flood);

		mSpinnerLayout.setClickable(true);
		mSpinnerLayout.setOnClickListener(this);
		mSpinnerLayoutFlooding.setClickable(true);
		mSpinnerLayoutFlooding.setOnClickListener(this);
		mProgress.setMax(6);
		mProgress.setProgress(5);
		mProgressStep.setText("Step 5 of 6");
		mCaptureCallingCard = (LinearLayout) mRootView
				.findViewById(R.id.customer_calling_layout);
		mTapToCallDa = (LinearLayout) mRootView
				.findViewById(R.id.tap_to_call_layout);
		mTapToCallDa.setOnClickListener(this);
		mTapToCallDa.setBackgroundResource(R.drawable.red_background);
		mCaptureCallingCard.setClickable(true);
		mCaptureCallingCard.setOnClickListener(this);
		mSave = (Button) mRootView.findViewById(R.id.button1);
		mLeaveSite = (Button) mRootView.findViewById(R.id.button2);
		
		mLeaveSite.setOnClickListener(this);
		radioGroup = (RadioGroup)mRootView.findViewById(R.id.radioGroup1);
		radioOne = (RadioButton) mRootView.findViewById(R.id.radio1);
		radioTwo  = (RadioButton) mRootView.findViewById(R.id.radio2);
		
		CheckOutObject checkOutObject = JobViewerDBHandler.getCheckOutRemember(getActivity());		
		if(checkOutObject.getIsPollutionSelected().equalsIgnoreCase(ActivityConstants.TRUE)){
			mSpinnerSelectedText.setText(getActivity().getResources().getString(R.string.activityTypePollution));
			mSpinnerLayout.setClickable(false);
			selectedActivityText = "Pollution";
		}
		
		radioButtonChangedListeners();		
	}

	@Override
	public void onClick(View view) {
		checkAndEnableNextButton();
		if (view == mSave) {
			
		} else if (view == mLeaveSite) {
			// Upload Photos here// if calling card available
			selectedActivityText = mSpinnerSelectedText.getText().toString();
			if (Utils.isInternetAvailable(getActivity())) {
				sendWorkEndTimeSheetToServer();
			} else {
				prepareWorkCompletedRequest();
				storeWorkEndTimeSheetInBackLogDB();
				startWorkSuccessActivity();
			}

		} else if (view == mCaptureCallingCard) {
			Intent intent = new Intent(Constants.IMAGE_CAPTURE_ACTION);
			file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "image.jpg");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent, Constants.RESULT_CODE);
			Toast.makeText(getActivity(), this.getResources().getString(R.string.capture_calling_Card),Toast.LENGTH_LONG).show();
		} else if (view == mSpinnerLayout) {
			String header = getResources().getString(R.string.activity_type);
			mSpinnerSelectedText.addTextChangedListener(this);
			Utils.dailogboxSelector(getActivity(), Utils.mActivityList,
					R.layout.work_complete_dialog, mSpinnerSelectedText, header);
			
		} else if (view == mSpinnerLayoutFlooding) {
			String header = getResources().getString(R.string.activity_type);
			Utils.dailogboxSelector(getActivity(), Utils.mFloodingList,
					R.layout.work_complete_dialog, mSpinnerSelectedFloodedText,
					header);
		} else if(view == mTapToCallDa){
			Intent phoneIntent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+getResources().getString(R.string.callDAMobileNumber)));
			startActivityForResult(phoneIntent, Constants.TAP_DA_PHONE_CALL_REQUEST_CODE);
		}		
		checkAndEnableNextButton();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 500 && resultCode == getActivity().RESULT_OK) {
			String imageString = null;
			Bitmap photo = Utils.decodeSampledBitmapFromFile(
					file.getAbsolutePath(), 1000, 700);

			Bitmap rotateBitmap = Utils.rotateBitmap(file.getAbsolutePath(),
					photo);
			String currentImageFile = Utils.getRealPathFromURI(
					Uri.fromFile(file), getActivity());
			String formatDate = "";
			String geoLocation = "";
			GPSTracker tracker = new GPSTracker(getActivity());
			
			try {
				ExifInterface exif = new ExifInterface(currentImageFile);
				String picDateTime = exif
						.getAttribute(ExifInterface.TAG_DATETIME);
				formatDate = Utils.formatDate(picDateTime);
				GeoLocationCamera geoLocationCamera = new GeoLocationCamera(
						exif);
				geoLocation = geoLocationCamera.toString();

				Log.i("Android", "formatDateFromOnetoAnother   :" + formatDate);
				Log.i("Android", "geoLocation   :" + geoLocation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			geoLocation = formatDate + tracker.getLatitude()+";"+tracker.getLongitude();
			String image_exif = formatDate + "," + geoLocation;
			ImageObject imageObject = new ImageObject();
			String generateUniqueID = Utils
					.generateUniqueID(getActivity());
			imageObject.setImageId(generateUniqueID);
			imageObject.setCategory("Pollution");
			imageObject.setImage_exif(image_exif);
			imageObject.setImage_string(Utils
					.bitmapToBase64String(rotateBitmap));			
			JobViewerDBHandler.saveImage(getActivity(), imageObject);
			
		}
		if (requestCode == Constants.TAP_DA_PHONE_CALL_REQUEST_CODE) {
			ConfirmDialog confirmDialog = new ConfirmDialog(getActivity(), this, Constants.TAP_DA_PHONE_CALL);
			confirmDialog.show();
			
		}
		
		checkAndEnableNextButton();
	}

	public void openDialog() {
		String header = getResources().getString(R.string.activity_type);
		Utils.dailogboxSelector(getActivity(), Utils.mActivityList,
				R.layout.work_complete_dialog, mSpinnerSelectedText, header);
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
	
	private WorkRequest prepareWorkCompletedRequest() {
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
		WorkRequest workRequest = new WorkRequest();
		insertWorkEndTimeIntoHoursCalculator();
		workRequest.setStarted_at(Utils.lastest_work_started_at);
		if (checkOutRemember.getVistecId() != null) {
			workRequest.setReference_id(checkOutRemember.getVistecId());
		} else {
			workRequest.setReference_id("");
		}
		workRequest.setEngineer_id(Utils.work_engineer_id);
		workRequest.setStatus(Utils.work_status_completed);
		workRequest.setCompleted_at(Utils.getCurrentDateAndTime());
		workRequest.setActivity_type(selectedActivityText);
		workRequest.setFlooding_status(Utils.work_flooding_status);
		workRequest.setDA_call_out(Utils.work_DA_call_out);
		workRequest.setIs_redline_captured(Utils.work_is_redline_captured);
		GPSTracker tracker = new GPSTracker(getActivity());
		workRequest.setLocation_latitude("" + tracker.getLatitude());
		workRequest.setLocation_longitude("" + tracker.getLongitude());
		workRequest.setCreated_by(userProfile.getEmail());
		BackLogRequest backLogRequest = new BackLogRequest();
		Utils.work_id = checkOutRemember.getWorkId();
		backLogRequest.setRequestApi(CommsConstant.HOST + "/"
				+ CommsConstant.WORK_UPDATE_API + "/" + Utils.work_id);
		backLogRequest.setRequestClassName("WorkRequest");
		backLogRequest.setRequestJson(GsonConverter.getInstance().encodeToJsonString(workRequest));
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(getActivity(), backLogRequest);
		return workRequest;
	}

	private void sendWorkEndTimeSheetToServer() {
		ContentValues data = new ContentValues();
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
		Utils.workEndTimeSheetRequest.setRecord_for(userProfile.getEmail());
		Utils.workEndTimeSheetRequest.setIs_inactive("false");
		Utils.workEndTimeSheetRequest.setOverride_reason("");
		Utils.workEndTimeSheetRequest.setOverride_comment("");
		Utils.workEndTimeSheetRequest.setOverride_timestamp(Utils
				.getCurrentDateAndTime());
		Utils.workEndTimeSheetRequest.setReference_id(checkOutRemember
				.getVistecId());
		Utils.workEndTimeSheetRequest.setUser_id(userProfile.getEmail());
		CheckOutObject checkOutRemember2 = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		data.put("started_at", checkOutRemember2.getJobStartedTime());
		data.put("record_for", userProfile.getEmail());
		data.put("is_inactive", "false");
			
		if(Utils.isNullOrEmpty(Utils.workEndTimeSheetRequest.getOverride_reason())){
			data.put("override_reason", "");
		}else{			
			data.put("override_reason", Utils.workEndTimeSheetRequest.getOverride_reason());
		}
		if(Utils.isNullOrEmpty(Utils.workEndTimeSheetRequest.getOverride_comment())){
			data.put("override_comment", "");
		} else{
			data.put("override_comment", Utils.workEndTimeSheetRequest.getOverride_comment());
		}
			
		data.put("override_timestamp",
		Utils.workEndTimeSheetRequest.getOverride_timestamp());
		data.put("reference_id", checkOutRemember.getVistecId());
		data.put("user_id", userProfile.getEmail());
		Utils.startProgress(getActivity());
		if(Utils.isInternetAvailable(getActivity())){
			Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
					+ CommsConstant.END_WORK_API, data,
					getWorkTimeSheetSubmitHandler());
		} else {
			prepareWorkCompletedRequest();
			storeWorkEndTimeSheetInBackLogDB();
			startWorkSuccessActivity();
		}
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
		data.put("status", Utils.work_status_completed);
		data.put("completed_at", Utils.getCurrentDateAndTime());
		data.put("activity_type", selectedActivityText);
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
		try{
			Utils.work_id = checkOutRemember.getWorkId();
		}catch(Exception e){}
		Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
				+ CommsConstant.WORK_UPDATE_API + "/" + Utils.work_id, data,
				getWorkCompletedHandler());
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
					ExceptionHandler.showException(getActivity(), exception,
							"Info");
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

					if (Utils.isInternetAvailable(getActivity())) {
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
					ExceptionHandler.showException(getActivity(), exception,
							"Info");
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

		Utils.saveTimeSheetInBackLogTable(getActivity(),
				Utils.workEndTimeSheetRequest, CommsConstant.END_WORK_API,
				Utils.REQUEST_TYPE_TIMESHEET);

	}

	private void startWorkSuccessActivity() {
		getActivity().finish();
		Intent workSuccessIntent = new Intent(getActivity(),
				WorkSuccessActivity.class);
		startActivity(workSuccessIntent);
	}
	
	private void insertWorkEndTimeIntoHoursCalculator() {
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(getActivity());
		breakShiftTravelCall.setWorkEndTime(String.valueOf(System.currentTimeMillis()));
		JobViewerDBHandler.saveBreakShiftTravelCall(getActivity(), breakShiftTravelCall);
	}

	@Override
	public void onConfirmStartTraining() {
		mTapToCallDa.setVisibility(View.GONE);
		Utils.work_DA_call_out="Call Made";
	}

	@Override
	public void onConfirmDismiss() {
				
	}
	
	private boolean validateUserInputs(){
		boolean validInputs = true;
		if(mSpinnerSelectedText.getText().toString().contains("Select")){
			validInputs = false;
		}
		
		if(mPipeDiameterEditText.getVisibility() == View.VISIBLE){
			mPipeDiameter = mPipeDiameterEditText.getText().toString();
			if(Utils.isNullOrEmpty(mPipeDiameter)){
				mPipeDiameterEditText.setError(this.getResources().getString(R.string.diameterRequired));
				enableNextButton(false);
				validInputs = false;
			}
		}
		
		if( mPipeLengthEditText.getVisibility() == View.VISIBLE){
			mPipeLength = mPipeLengthEditText.getText().toString();
			if(Utils.isNullOrEmpty(mPipeLength)){
				mPipeLengthEditText.setError(this.getResources().getString(R.string.diameterRequired));
				enableNextButton(false);
				validInputs = false;
			}	
		}
		
		if(mSpinnerSelectedFloodedText.getText().toString().contains("Select")){
			validInputs = false;
		} else{
			Utils.work_flooding_status = mSpinnerSelectedFloodedText.getText().toString();
		}
		if(!(radioOne.isChecked() || radioTwo.isChecked())){
			validInputs = false;
		}
		return validInputs;
	}

	@Override
	public void afterTextChanged(Editable s) {
		//makePipeDiameterAndLengthInvisible();
		if(mSpinnerSelectedText.getText().toString().contains(this.getResources().getString(R.string.cctv))){
			updatePipeDiamterAndLength();
		}
		
		if(mSpinnerSelectedText.getText().toString().contains(this.getResources().getString(R.string.line_clean))){
			updatePipeDiamterAndLength();
		}
		
		if(!mSpinnerSelectedText.getText().toString().contains(this.getResources().getString(R.string.cctv))&&
				!mSpinnerSelectedText.getText().toString().contains(this.getResources().getString(R.string.line_clean))){
			makePipeDiameterAndLengthInvisible();
		}
	}

	private void makePipeDiameterAndLengthInvisible() {
		mPipeDiameterEditText.setVisibility(View.GONE);
		mPipeLengthEditText.setVisibility(View.GONE);
		checkAndEnableNextButton();
	}

	private void checkAndEnableNextButton() {
		if(validateUserInputs()){
			enableNextButton(true);
		} else{
			enableNextButton(false);
		}
	}

	private void updatePipeDiamterAndLength() {
		if(mPipeDiameterEditText.getVisibility()==View.VISIBLE){
			mPipeDiameter = mPipeDiameterEditText.getText().toString();
		} else{
			mPipeDiameterEditText.setVisibility(View.VISIBLE);
		}
		if(mPipeLengthEditText.getVisibility()==View.VISIBLE){
			mPipeLength = mPipeLengthEditText.getText().toString();
		} else {
			mPipeLengthEditText.setVisibility(View.VISIBLE);
		}
		checkAndEnableNextButton();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
	}

}
