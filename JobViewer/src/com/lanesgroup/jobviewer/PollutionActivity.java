package com.lanesgroup.jobviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.internal.widget.ContentFrameLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.custom.view.MultiSelectSpinner;
import com.jobviewer.custom.view.MultiSelectSpinner.MultiSpinnerListener;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
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
	Button nextButton;
	PollutionReportRequest pollutionReportRequest;
	ArrayList<String> stringOfLandPollutants;
	ArrayList<String> stringOfWaterPollutants;
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
	}

	protected void addWaterPollutionData() {
		final String[] extentOfLandPollutionArray = getResources().getStringArray(
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
		});

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
							}
						}

					}
				});

		boolean[] selectedItems = new boolean[waterPollutantsAdapter.getCount()];
		selectedItems[1] = true; // select second item
		waterPollutantsSpinner.setSelected(selectedItems);

	}

	protected void addLandPollutionData() {
		final String[] extentOfLandPollutionArray = getResources().getStringArray(
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
		});
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
		extentOfLandSpinner = (Spinner) findViewById(R.id.extentOfLandSpinner);
		landAffectedSpinner = (Spinner) findViewById(R.id.landAffectedSpinner);
		landPollutantsSpinner = (MultiSelectSpinner) findViewById(R.id.landPollutantsSpinner);
		ptwCheckbox = (CheckBox) findViewById(R.id.ptwCheckbox);
		extentOfWaterSpinner = (Spinner) findViewById(R.id.extentOfWaterSpinner);
		waterBodyAffectedSpinner = (Spinner) findViewById(R.id.waterBodyAffectedSpinner);
		waterPollutantsSpinner = (MultiSelectSpinner) findViewById(R.id.waterPollutantsSpinner);
		indicativeCauseSpinner = (Spinner) findViewById(R.id.indicativeCauseSpinner);
		additionalEDSpinner = (MultiSelectSpinner) findViewById(R.id.additionalEDSpinner);
		ptlExpandLayout = (LinearLayout) findViewById(R.id.ptlExpandLayout);
		ptwExpandLayout = (LinearLayout) findViewById(R.id.ptwExpandLayout);
		nextButton = (Button) findViewById(R.id.nextButton);
		nextButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nextButton:
			
			pollutionReportRequest.setLand_pollutants(stringOfLandPollutants);
			pollutionReportRequest.setWater_pollutants(stringOfWaterPollutants);
			
			if(Utils.isInternetAvailable(PollutionActivity.this)){
				sendPollutionReportToServer();
			} else {
				savePollutionReportInBackLogDb();
				Intent addPhotosActivityIntent = new Intent(PollutionActivity.this, AddPhotosActivity.class);
				startActivity(addPhotosActivityIntent);
			}
			
			break;

		default:
			break;
		}

	}
	
	private void sendPollutionReportToServer(){
		ContentValues data = new ContentValues();

		data.put("land_polluted",pollutionReportRequest.getLand_polluted());
		data.put("land_area",pollutionReportRequest.getLand_area());
		data.put("land_type",pollutionReportRequest.getLand_type());
		 
		data.put("land_pollutants",Arrays.toString(pollutionReportRequest.getLand_pollutants().toArray()));
		data.put("water_polluted",pollutionReportRequest.getWater_polluted());
		data.put("water_area",pollutionReportRequest.getWater_area());
		data.put("water_body",pollutionReportRequest.getWater_body());		
		data.put("water_pollutants",Arrays.toString(pollutionReportRequest.getWater_pollutants().toArray()));
		data.put("do_upstream",pollutionReportRequest.getDo_upstream());
		data.put("do_downstream",pollutionReportRequest.getDo_downstream());
		data.put("do_upstream_image",pollutionReportRequest.getDo_upstream_image());
		data.put("do_downstream_image",pollutionReportRequest.getDo_downstream_image());
		data.put("ammonia",pollutionReportRequest.getAmmonia());
		data.put("fish_kill",pollutionReportRequest.getFish_kill());
		data.put("indicative_cause",pollutionReportRequest.getIndicative_cause());
		data.put("failed_asset",pollutionReportRequest.getFailed_asset());
		data.put("equipment_deployed",Arrays.toString(pollutionReportRequest.getEquipment_deployed()));
		
		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.POLLUTION_REPORT_UPLOAD+"/"+Utils.work_id, data, getPollutionReportHandler());
	}

	private Handler getPollutionReportHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Intent addPhotosActivityIntent = new Intent(PollutionActivity.this, AddPhotosActivity.class);
					startActivity(addPhotosActivityIntent);
					break;
				case HttpConnection.DID_ERROR:
					Intent addfailurePhotosActivityIntent = new Intent(PollutionActivity.this, AddPhotosActivity.class);
					startActivity(addfailurePhotosActivityIntent);
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
}
