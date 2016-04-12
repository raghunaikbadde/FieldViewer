package com.lanesgroup.jobviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.custom.view.MultiSelectSpinner;
import com.jobviewer.custom.view.MultiSelectSpinner.MultiSpinnerListener;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GeoLocationCamera;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Utils;
import com.raghu.PollutionReportRequest;
import com.vehicle.communicator.HttpConnection;

public class PollutionActivity extends BaseActivity implements
		View.OnClickListener {
	TextView title_text, number_text, progress_step_text;
	ProgressBar progressBar;
	CheckBox ptlCheckbox, ptwCheckbox;
	LinearLayout ptlExpandLayout, ptwExpandLayout;
	Spinner extentOfLandSpinner, landAffectedSpinner, extentOfWaterSpinner,
			waterBodyAffectedSpinner, indicativeCauseSpinner;
	MultiSelectSpinner landPollutantsSpinner, waterPollutantsSpinner,
			additionalEDSpinner;
	Button nextButton, mTakePicUpStream,mTakePicDownStream,mSaveButton;
	PollutionReportRequest pollutionReportRequest;
	
	EditText mUpStreamEditText, mDownStreamEdiText;
	
	RelativeLayout spinnerLayout, landAffectedLayout, spinnerLayoutExtentOfWater, spinnerLayoutWaterBody, spinnerLayoutAmmonia, spinnerLayoutFishKill,
	spinnerLayoutIndicative;
	TextView landPollution, landAffected, extentOfWater, waterBody, ammonia, fishKill, indicativeCause;
	
	ArrayList<String> stringOfLandPollutants;
	ArrayList<String> stringOfWaterPollutants;
	ArrayList<String> stringOfAdditionalEPD;

	ImageObject upStreamImageObject,downSteamIamgeObject;
	static File file;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pollution_screen);
		initUI();
		updateData();
	}

	private void updateData() {
		pollutionReportRequest = new PollutionReportRequest();  
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(this);
		if (ActivityConstants.EXCAVATION.equalsIgnoreCase(checkOutRemember
				.getAssessmentSelected())) {
			title_text.setText(getResources().getString(
					R.string.excavation_risk_str));
		} else {
			title_text.setText(getResources().getString(
					R.string.non_excavation_risk_str));
		}
		number_text.setText(checkOutRemember.getVistecId());

		progress_step_text.setText(getResources().getString(
				R.string.pollution_progress));
		progressBar.setProgress((100 / 6) * 4);
		ptlCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				validateUserInputs();
				if (isChecked) {
					ptlExpandLayout.setVisibility(View.VISIBLE);
					pollutionReportRequest.setLand_polluted("Yes");
					addLandPollutionData();
				} else {
					pollutionReportRequest.setLand_polluted("No");
					ptlExpandLayout.setVisibility(View.GONE);
				}
			}
		});
		ptwCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				validateUserInputs();
				if (isChecked) {
					ptwExpandLayout.setVisibility(View.VISIBLE);
					pollutionReportRequest.setWater_polluted("Yes");
					addWaterPollutionData();
				} else {
					pollutionReportRequest.setWater_polluted("No");
					ptwExpandLayout.setVisibility(View.GONE);
				}
			}
		});
		

		final ArrayAdapter<String> additionEPDAdapter = new ArrayAdapter<String>(
				this, R.layout.simple_spinner_item);
		String[] additionalEPDArray = getResources().getStringArray(
				R.array.additionalEquipmentDeployed);
		for (int i = 0; i < additionalEPDArray.length; i++) {
			additionEPDAdapter.add(additionalEPDArray[i]);
		}

		stringOfAdditionalEPD = new ArrayList<String>();
		additionalEDSpinner.setAdapter(additionEPDAdapter, false,
				new MultiSpinnerListener() {

					@Override
					public void onItemsSelected(boolean[] selected) {
						StringBuilder builder = new StringBuilder();

						for (int i = 0; i < selected.length; i++) {
							if (selected[i]) {
								builder.append(
										additionEPDAdapter.getItem(i))
										.append(" ");
								stringOfAdditionalEPD.add(additionEPDAdapter.getItem(i));		
								pollutionReportRequest.setEquipment_deployed(stringOfAdditionalEPD);
								validateUserInputs();
							}
						}

					}
				});

		boolean[] selectedEPDItems = new boolean[additionEPDAdapter.getCount()];
		selectedEPDItems[1] = true; // select second item
		additionalEDSpinner.setSelected(selectedEPDItems);
		
		upStreamImageObject = new ImageObject();
		downSteamIamgeObject = new ImageObject();
	}

	protected void addWaterPollutionData() {
		/*final String[] extentOfLandPollutionArray = getResources().getStringArray(
				R.array.extentOfLandPollutionArray);
		ArrayAdapter<String> extentOfWaterAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item,
				extentOfLandPollutionArray);
		extentOfWaterAdapter
				.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		extentOfWaterSpinner.setAdapter(extentOfWaterAdapter);

		extentOfWaterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				pollutionReportRequest.setWater_area(extentOfLandPollutionArray[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				pollutionReportRequest.setWater_area("");
			}
			
		});
		
		final String[] waterBodyArray = getResources().getStringArray(
				R.array.waterBodyArray);
		ArrayAdapter<String> waterBodyAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, waterBodyArray);
		waterBodyAdapter
				.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		waterBodyAffectedSpinner.setAdapter(waterBodyAdapter);
		waterBodyAffectedSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				pollutionReportRequest.setWater_body(waterBodyArray[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});*/

		final ArrayAdapter<String> waterPollutantsAdapter = new ArrayAdapter<String>(
				this, R.layout.simple_spinner_item);
		String[] waterPollutantsArray = getResources().getStringArray(
				R.array.waterPollutantsArray);
		for (int i = 0; i < waterPollutantsArray.length; i++) {
			waterPollutantsAdapter.add(waterPollutantsArray[i]);
		}
		stringOfWaterPollutants = new ArrayList<String>();
		waterPollutantsSpinner.setAdapter(waterPollutantsAdapter, false,
				new MultiSpinnerListener() {

					@Override
					public void onItemsSelected(boolean[] selected) {
						StringBuilder builder = new StringBuilder();

						for (int i = 0; i < selected.length; i++) {
							if (selected[i]) {
								builder.append(
										waterPollutantsAdapter.getItem(i))
										.append(" ");
								stringOfWaterPollutants.add(waterPollutantsAdapter.getItem(i));
								pollutionReportRequest.setWater_pollutants(stringOfWaterPollutants);
								validateUserInputs();
							}
						}

					}
				});

		boolean[] selectedItems = new boolean[waterPollutantsAdapter.getCount()];
		selectedItems[1] = true; // select second item
		waterPollutantsSpinner.setSelected(selectedItems);
		
	}

	protected void addLandPollutionData() {
		/*final String[] extentOfLandPollutionArray = getResources().getStringArray(
				R.array.extentOfLandPollutionArray);
		ArrayAdapter<String> extentOfLandPollutionAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item,
				extentOfLandPollutionArray);
		extentOfLandPollutionAdapter
				.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		extentOfLandSpinner.setAdapter(extentOfLandPollutionAdapter);
		extentOfLandSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				pollutionReportRequest.setLand_area(extentOfLandPollutionArray[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				pollutionReportRequest.setLand_area("");
			}
		});
		final String[] landAffectedArray = getResources().getStringArray(
				R.array.landAffectedArray);
		ArrayAdapter<String> landAffectedAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, landAffectedArray);
		landAffectedAdapter
				.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		landAffectedSpinner.setAdapter(landAffectedAdapter);

		landAffectedSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				pollutionReportRequest.setLand_type(landAffectedArray[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});*/
		final ArrayAdapter<String> landPollutantsAdapter = new ArrayAdapter<String>(
				this, R.layout.simple_spinner_item);
		String[] landPollutantsArray = getResources().getStringArray(
				R.array.landPollutantsArray);
		for (int i = 0; i < landPollutantsArray.length; i++) {
			landPollutantsAdapter.add(landPollutantsArray[i]);
		}
		stringOfLandPollutants =  new ArrayList<String>();
		landPollutantsSpinner.setAdapter(landPollutantsAdapter, false,
				new MultiSpinnerListener() {

					@Override
					public void onItemsSelected(boolean[] selected) {
						StringBuilder builder = new StringBuilder();											
		
						for (int i = 0; i < selected.length; i++) {
							if (selected[i]) {
								builder.append(landPollutantsAdapter.getItem(i))
										.append(" ");			
								stringOfLandPollutants.add(landPollutantsAdapter.getItem(i));
								pollutionReportRequest.setLand_pollutants(stringOfLandPollutants);								
								validateUserInputs();
							}
						}

					}
				});
		boolean[] selectedItems = new boolean[landPollutantsAdapter.getCount()];
		selectedItems[1] = true; // select second item
		landPollutantsSpinner.setSelected(selectedItems);
		
	}

	private void initUI() {
		title_text = (TextView) findViewById(R.id.title_text);
		number_text = (TextView) findViewById(R.id.number_text);
		progress_step_text = (TextView) findViewById(R.id.progress_step_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		ptlCheckbox = (CheckBox) findViewById(R.id.ptlCheckbox);
		mUpStreamEditText = (EditText) findViewById(R.id.upstream_edittext);
		mDownStreamEdiText = (EditText) findViewById(R.id.downstream_edittext);
		mSaveButton = (Button) findViewById(R.id.button1);
		mSaveButton.setOnClickListener(this);
		//extentOfLandSpinner = (Spinner) findViewById(R.id.extentOfLandSpinner);
		//landAffectedSpinner = (Spinner) findViewById(R.id.landAffectedSpinner);
		landPollutantsSpinner = (MultiSelectSpinner) findViewById(R.id.landPollutantsSpinner);
		ptwCheckbox = (CheckBox) findViewById(R.id.ptwCheckbox);
		//extentOfWaterSpinner = (Spinner) findViewById(R.id.extentOfWaterSpinner);
		//waterBodyAffectedSpinner = (Spinner) findViewById(R.id.waterBodyAffectedSpinner);
		waterPollutantsSpinner = (MultiSelectSpinner) findViewById(R.id.waterPollutantsSpinner);
		//indicativeCauseSpinner = (Spinner) findViewById(R.id.indicativeCauseSpinner);
		additionalEDSpinner = (MultiSelectSpinner) findViewById(R.id.additionalEDSpinner);
		ptlExpandLayout = (LinearLayout) findViewById(R.id.ptlExpandLayout);
		ptwExpandLayout = (LinearLayout) findViewById(R.id.ptwExpandLayout);
		nextButton = (Button) findViewById(R.id.nextButton);
		mTakePicUpStream = (Button) findViewById(R.id.takePicture_upstream);
		mTakePicDownStream = (Button) findViewById(R.id.takePicture_downstream);
		mTakePicUpStream.setOnClickListener(this);
		mTakePicDownStream.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		
		spinnerLayout = (RelativeLayout) findViewById(R.id.spinnerLayout);
		spinnerLayout.setOnClickListener(this);
		landPollution = (TextView)findViewById(R.id.landPollution);
		landAffectedLayout = (RelativeLayout)findViewById(R.id.spinnerLayout1);
		landAffectedLayout.setOnClickListener(this);
		landAffected = (TextView)findViewById(R.id.landAffected);
		
		spinnerLayoutExtentOfWater = (RelativeLayout) findViewById(R.id.spinnerLayoutExtentOfWater);
		spinnerLayoutExtentOfWater.setOnClickListener(this);
		extentOfWater = (TextView) findViewById(R.id.extentOfWater);
		
		spinnerLayoutWaterBody = (RelativeLayout) findViewById(R.id.spinnerLayoutWaterBody);
		spinnerLayoutWaterBody.setOnClickListener(this);
		waterBody = (TextView) findViewById(R.id.waterBody);
		
		spinnerLayoutAmmonia = (RelativeLayout) findViewById(R.id.spinnerLayoutAmmonia);
		spinnerLayoutAmmonia.setOnClickListener(this);
		ammonia = (TextView) findViewById(R.id.ammonia);
		
		spinnerLayoutFishKill = (RelativeLayout) findViewById(R.id.spinnerLayoutFishKill);
		spinnerLayoutFishKill.setOnClickListener(this);
		fishKill = (TextView) findViewById(R.id.fishKill);
		
		spinnerLayoutIndicative = (RelativeLayout) findViewById(R.id.spinnerLayoutIndicative);
		spinnerLayoutIndicative.setOnClickListener(this);
		indicativeCause = (TextView) findViewById(R.id.indicativeCause);
		enableNextButton(false);
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {		
		case R.id.button1:
			Intent homeIntent = new Intent(this,ActivityPageActivity.class);
			homeIntent.putExtra(Utils.SHOULD_SHOW_WORK_IN_PROGRESS, true);
			homeIntent.putExtra(Utils.CALLING_ACTIVITY,
					ActivityConstants.POLLUTION_ACTIVITY);			
			startActivity(homeIntent);
			break;
		case R.id.nextButton:
			
			pollutionReportRequest.setLand_pollutants(stringOfLandPollutants);
			pollutionReportRequest.setWater_pollutants(stringOfWaterPollutants);
			
			if(Utils.isInternetAvailable(PollutionActivity.this)){
				Utils.startProgress(PollutionActivity.this);
				sendPollutionReportToServer();
			} else {
				savePollutionReportInBackLogDb();
				Intent addPhotosActivityIntent = new Intent(PollutionActivity.this, AddPhotosActivity.class);
				startActivity(addPhotosActivityIntent);
			}
			break;
		case R.id.spinnerLayout:
			String landPollutionHeader = "Extent of land pollution";
			Utils.dailogboxSelector(this, Utils.mLandPollutionList, R.layout.work_complete_dialog, landPollution, landPollutionHeader);
			break;
		case R.id.spinnerLayout1:
			String landAffectedHeader = "Land type";
			Utils.dailogboxSelector(this, Utils.mLandAffectedList, R.layout.work_complete_dialog, landAffected, landAffectedHeader);
			break;
		case R.id.spinnerLayoutExtentOfWater:
			String extentOfWaterHeader = "Extent of water pollution";
			Utils.dailogboxSelector(this, Utils.mExtentOfWaterList, R.layout.work_complete_dialog, extentOfWater, extentOfWaterHeader);
			break;
		case R.id.spinnerLayoutWaterBody:
			String waterBodyHeader = "Water body";
			Utils.dailogboxSelector(this, Utils.mWaterBodyList, R.layout.work_complete_dialog, waterBody, waterBodyHeader);
			break;
		case R.id.spinnerLayoutIndicative:
			String indicativeHeader = "Indicative Cause";
			Utils.dailogboxSelector(this, Utils.mIndicativeCause, R.layout.work_complete_dialog, indicativeCause, indicativeHeader);			
			break;
		case R.id.spinnerLayoutAmmonia:
			String ammoniaHeader = "Ammonia";
			Utils.dailogboxSelector(this, Utils.mAmmonia, R.layout.work_complete_dialog, ammonia, ammoniaHeader);
			break;
		case R.id.spinnerLayoutFishKill:
			String fishKillHeader = "Fish Kill";
			Utils.dailogboxSelector(this, Utils.mFishKill, R.layout.work_complete_dialog, fishKill, fishKillHeader);
			break;
		case R.id.takePicture_upstream:
			file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "image.jpg");
			Intent intent = new Intent(
					com.jobviewer.util.Constants.IMAGE_CAPTURE_ACTION);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent,
					com.jobviewer.util.Constants.UPSTREAM_RESULT_CODE);
			break;
		case R.id.takePicture_downstream:
			file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "image.jpg");
			Intent imageIntent = new Intent(
					com.jobviewer.util.Constants.IMAGE_CAPTURE_ACTION);
			imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(imageIntent,
					com.jobviewer.util.Constants.DOWNSTREAM_RESULT_CODE);
			break;
		default:
			break;
		}
		validateUserInputs();
	}
	
	private void sendPollutionReportToServer(){
		ContentValues data = new ContentValues();

		if(ptlCheckbox.isChecked()){
			data.put("land_polluted",pollutionReportRequest.getLand_polluted());
			
			pollutionReportRequest.setLand_area(landPollution.getText().toString());		
			data.put("land_area",pollutionReportRequest.getLand_area());
			
			pollutionReportRequest.setLand_type(landAffected.getText().toString());		
			data.put("land_type",pollutionReportRequest.getLand_type());
			 
			if(pollutionReportRequest.getLand_pollutants() == null){
				pollutionReportRequest.setLand_pollutants(new ArrayList<String>());
			}
			
			ArrayList<String> landpollutants = pollutionReportRequest.getLand_pollutants();
			String idList = landpollutants.toString();
			String landPollutantsString = idList.substring(1, idList.length() - 1).replace(", ", ",");			
			data.put("land_pollutants", landPollutantsString );
		} 
		
		if(ptwCheckbox.isChecked()){
		
			data.put("water_polluted",pollutionReportRequest.getWater_polluted());
			
			pollutionReportRequest.setWater_area(extentOfWater.getText().toString());		
			data.put("water_area",pollutionReportRequest.getWater_area());
			
			pollutionReportRequest.setWater_body(waterBody.getText().toString());
			data.put("water_body",pollutionReportRequest.getWater_body());
			
			if(pollutionReportRequest.getWater_pollutants() == null){
				pollutionReportRequest.setWater_pollutants(new ArrayList<String>());		
			}			
			
			ArrayList<String> waterpollutants = pollutionReportRequest.getWater_pollutants();			
			String idList = waterpollutants.toString();
			String waterPollutantsString = idList.substring(1, idList.length() - 1).replace(", ", ",");			
			data.put("water_pollutants", waterPollutantsString );
			
			pollutionReportRequest.setDo_upstream(mUpStreamEditText.getText().toString());
			data.put("do_upstream",pollutionReportRequest.getDo_upstream());
			
			pollutionReportRequest.setDo_downstream(mDownStreamEdiText.getText().toString());
			data.put("do_downstream",pollutionReportRequest.getDo_downstream());
			
			
			if(!Utils.isNullOrEmpty(upStreamImageObject.getImage_string())){
				pollutionReportRequest.setDo_upstream_image(upStreamImageObject.getImageId());
			} else {
				pollutionReportRequest.setDo_upstream_image("");
			}
			data.put("do_upstream_image",pollutionReportRequest.getDo_upstream_image());
			
			
			if(!Utils.isNullOrEmpty(upStreamImageObject.getImage_string())){
				pollutionReportRequest.setDo_downstream_image(downSteamIamgeObject.getImageId());
			} else {
				pollutionReportRequest.setDo_downstream_image("");
			}		
			data.put("do_downstream_image",pollutionReportRequest.getDo_downstream_image());
			
			pollutionReportRequest.setAmmonia(ammonia.getText().toString());
			data.put("ammonia",pollutionReportRequest.getAmmonia());
			
			pollutionReportRequest.setFish_kill(fishKill.getText().toString());
			data.put("fish_kill",pollutionReportRequest.getFish_kill());
			data.put("failed_asset",pollutionReportRequest.getFailed_asset());
		}
		
		pollutionReportRequest.setIndicative_cause(indicativeCause.getText().toString());
		data.put("indicative_cause",pollutionReportRequest.getIndicative_cause());
		
		//TODO: UI is not not yet ready
		
		ArrayList<String> eqDeployedArray = pollutionReportRequest.getEquipment_deployed();
		String idList = eqDeployedArray.toString();
		String additionalEqDeployedString = idList.substring(1, idList.length() - 1).replace(", ", ",");			
		data.put("equipment_deployed", additionalEqDeployedString );
		
		Log.d(Utils.LOG_TAG, " url - : "+CommsConstant.HOST+CommsConstant.POLLUTION_REPORT_UPLOAD+"/"+Utils.work_id);
		Log.d(Utils.LOG_TAG," request "+GsonConverter.getInstance().encodeToJsonString(data));
		
		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.POLLUTION_REPORT_UPLOAD+"/"+Utils.work_id, data, getPollutionReportHandler());
	}

	private Handler getPollutionReportHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					Intent addPhotosActivityIntent = new Intent(PollutionActivity.this, AddPhotosActivity.class);
					startActivity(addPhotosActivityIntent);
					break;
				case HttpConnection.DID_ERROR:
					String error = (String) msg.obj;
					Utils.StopProgress();
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(PollutionActivity.this,
							exception, "Info");
					/*Intent addfailurePhotosActivityIntent = new Intent(PollutionActivity.this, AddPhotosActivity.class);
					startActivity(addfailurePhotosActivityIntent);*/
					savePollutionReportInBackLogDb();
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}
	
	private void savePollutionReportInBackLogDb(){
		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.HOST+"/"+CommsConstant.POLLUTION_REPORT_UPLOAD+"/"+Utils.work_id);
		backLogRequest.setRequestClassName("PollutionReportRequest");
		backLogRequest.setRequestJson(GsonConverter.getInstance().encodeToJsonString(pollutionReportRequest));
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
		JobViewerDBHandler.saveBackLog(getApplicationContext(), backLogRequest);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == com.jobviewer.util.Constants.UPSTREAM_RESULT_CODE && resultCode == RESULT_OK) {
			upStreamImageObject = new ImageObject();
			prepareImageObject(upStreamImageObject);
			mTakePicUpStream.setCompoundDrawablesWithIntrinsicBounds(null,context.getResources().getDrawable(R.drawable.camera_plus_icon),null,null);			
			mTakePicUpStream.setBackgroundColor(context.getResources().getColor(R.color.red));
		} else if  (requestCode == com.jobviewer.util.Constants.DOWNSTREAM_RESULT_CODE && resultCode == RESULT_OK) {
			downSteamIamgeObject = new ImageObject();
			prepareImageObject(downSteamIamgeObject);
			mTakePicDownStream.setCompoundDrawablesWithIntrinsicBounds(null,context.getResources().getDrawable(R.drawable.camera_plus_icon),null,null);
			mTakePicDownStream.setBackgroundColor(context.getResources().getColor(R.color.red));
		}
	}

	private void prepareImageObject(ImageObject imageObject) {
		String generateUniqueID = Utils
				.generateUniqueID(this);
		imageObject.setImageId(generateUniqueID);
		imageObject.setCategory("work");
		
		Bitmap photo = Utils.decodeSampledBitmapFromFile(
				file.getAbsolutePath(), 1000, 700);

		Bitmap rotateBitmap = Utils.rotateBitmap(file.getAbsolutePath(),
				photo);
		
		String currentImageFile = Utils.getRealPathFromURI(
				Uri.fromFile(file), this);
		
		String formatDate = "";
		String geoLocation = "";

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
		String image_exif = formatDate + "," + geoLocation;
		imageObject.setImage_string(Utils
				.bitmapToBase64String(rotateBitmap));
		imageObject.setImage_exif(image_exif);
	}
	
	public void enableNextButton(boolean isEnable) {
		if (isEnable) {
			nextButton.setEnabled(true);
			nextButton.setBackgroundResource(R.drawable.red_background);
			nextButton.setOnClickListener(this);
		} else {
			nextButton.setEnabled(false);
			nextButton.setBackgroundResource(R.drawable.dark_grey_background);
			nextButton.setOnClickListener(null);
		}
	}
	
	public void validateUserInputs(){
		enableNextButton(false);
		if(ptlCheckbox.isChecked() && ptwCheckbox.isChecked()){
			String extentOfLandPollution = landPollution.getText().toString();
			String landEffected = landAffected.getText().toString();
			
			ArrayList<String> langpollutants = null;
			
			try{
				langpollutants = pollutionReportRequest.getLand_pollutants();
			}catch(Exception e){
				
			}
			if(langpollutants == null){
				langpollutants = new ArrayList<String>();
			}
			
			String indicativeCasueStr = indicativeCause.getText().toString();
			
			
			String extentOfWaterPollution = extentOfWater.getText().toString();
			String waterBoddyEffected = waterBody.getText().toString();
			ArrayList<String> waterPollutants = null;
			
			try{
				waterPollutants = pollutionReportRequest.getWater_pollutants();
			}catch(Exception e){
				
			}
			if(waterPollutants == null){
				waterPollutants = new ArrayList<String>();
			}
			Log.d(Utils.LOG_TAG,"BothSelected");
			String ammoniaStr = ammonia.getText().toString();
			String fishKillStr = fishKill.getText().toString();
			if(!extentOfLandPollution.contains("Select") &&
					!landEffected.contains("Select") && langpollutants.size() != 0){
				Log.d(Utils.LOG_TAG,"LandPollutionPassed");
				if(!extentOfWaterPollution.contains("Select") && !waterBoddyEffected.contains("Select")
						&& waterPollutants.size() != 0 && !ammoniaStr.contains("Select")
						&& !fishKillStr.contains("Select")){
					Log.d(Utils.LOG_TAG,"WaterPollutionPassed");
					if(!indicativeCasueStr.contains("Select")){
						Log.d(Utils.LOG_TAG,"IndicativeCausePassed");
						enableNextButton(true);
					}
				}
			}
			Log.d(Utils.LOG_TAG," extentOfLandPollution "+extentOfLandPollution
					+" landEffected "+landEffected + "indicativeCasueStr "+indicativeCasueStr+
					" landPollutants Size "+langpollutants.size());
			
			Log.d(Utils.LOG_TAG, " extentOfWaterPollution "+extentOfWaterPollution
					+" waterBoddyEffected "+waterBoddyEffected +
					 " ammoniaStr "+ammoniaStr 
				+" fishKillStr "+fishKillStr+" indicativeCasueStr "+indicativeCasueStr +" size of water pollutatnat" + waterPollutants.size());
			
		} else {
			if(ptlCheckbox.isChecked()){
				String extentOfLandPollution = landPollution.getText().toString();
				String landEffected = landAffected.getText().toString();
				ArrayList<String> langpollutants = null;
				
				try{
					langpollutants = pollutionReportRequest.getLand_pollutants();
				}catch(Exception e){
					
				}
				if(langpollutants == null){
					langpollutants = new ArrayList<String>();
				}
				String indicativeCasueStr = indicativeCause.getText().toString();
				if(!extentOfLandPollution.contains("Select") &&
						!landEffected.contains("Select") && langpollutants.size() != 0){
					if(!indicativeCasueStr.contains("Select")){
						enableNextButton(true);
					}
				}
				
				Log.d(Utils.LOG_TAG," extentOfLandPollution "+extentOfLandPollution
						+" landEffected "+landEffected + "indicativeCasueStr "+indicativeCasueStr );
			}
			if(ptwCheckbox.isChecked()){
				String extentOfWaterPollution = extentOfWater.getText().toString();
				String waterBoddyEffected = waterBody.getText().toString();
				ArrayList<String> waterPollutants = null;
				
				try{
					waterPollutants = pollutionReportRequest.getWater_pollutants();
				}catch(Exception e){
					
				}
				if(waterPollutants == null){
					waterPollutants = new ArrayList<String>();
				}
				
				String ammoniaStr = ammonia.getText().toString();
				String fishKillStr = fishKill.getText().toString();
				String indicativeCasueStr = indicativeCause.getText().toString();
				if(!extentOfWaterPollution.contains("Select") && !waterBoddyEffected.contains("Select")
						&& waterPollutants.size() != 0 && !ammoniaStr.contains("Select")
						&& !fishKillStr.contains("Select")){
					if(!indicativeCasueStr.contains("Select")){
						enableNextButton(true);
					}
				}		
				
				Log.d(Utils.LOG_TAG, " extentOfWaterPollution "+extentOfWaterPollution
						+" waterBoddyEffected "+waterBoddyEffected +
						 " ammoniaStr "+ammoniaStr 
					+" fishKillStr "+fishKillStr+" indicativeCasueStr "+indicativeCasueStr +" size of water pollutatnat" + waterPollutants.size());
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		Intent homeIntent = new Intent(this,ActivityPageActivity.class);
		homeIntent.putExtra(Utils.SHOULD_SHOW_WORK_IN_PROGRESS, true);
		homeIntent.putExtra(Utils.CALLING_ACTIVITY,
				ActivityConstants.POLLUTION_ACTIVITY);			
		startActivity(homeIntent);		
	}
}