package com.lanesgroup.jobviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.jobviewer.custom.view.MultiSelectSpinner;
import com.jobviewer.custom.view.MultiSelectSpinner.MultiSpinnerListener;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.ActivityConstants;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pollution_screen);
		initUI();
		updateData();
	}

	private void updateData() {
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
					addLandPollutionData();
				} else {
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
					addWaterPollutionData();
				} else {
					ptwExpandLayout.setVisibility(View.GONE);
				}
			}
		});
	}

	protected void addWaterPollutionData() {
		String[] extentOfLandPollutionArray = getResources().getStringArray(
				R.array.extentOfLandPollutionArray);
		ArrayAdapter<String> extentOfWaterAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item,
				extentOfLandPollutionArray);
		extentOfWaterAdapter
				.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		extentOfWaterSpinner.setAdapter(extentOfWaterAdapter);

		String[] waterBodyArray = getResources().getStringArray(
				R.array.waterBodyArray);
		ArrayAdapter<String> waterBodyAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, waterBodyArray);
		waterBodyAdapter
				.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		waterBodyAffectedSpinner.setAdapter(waterBodyAdapter);

		final ArrayAdapter<String> waterPollutantsAdapter = new ArrayAdapter<String>(
				this, R.layout.simple_spinner_item);
		String[] waterPollutantsArray = getResources().getStringArray(
				R.array.waterPollutantsArray);
		for (int i = 0; i < waterPollutantsArray.length; i++) {
			waterPollutantsAdapter.add(waterPollutantsArray[i]);
		}

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
							}
						}

					}
				});

		boolean[] selectedItems = new boolean[waterPollutantsAdapter.getCount()];
		selectedItems[1] = true; // select second item
		waterPollutantsSpinner.setSelected(selectedItems);

	}

	protected void addLandPollutionData() {
		String[] extentOfLandPollutionArray = getResources().getStringArray(
				R.array.extentOfLandPollutionArray);
		ArrayAdapter<String> extentOfLandPollutionAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item,
				extentOfLandPollutionArray);
		extentOfLandPollutionAdapter
				.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		extentOfLandSpinner.setAdapter(extentOfLandPollutionAdapter);

		String[] landAffectedArray = getResources().getStringArray(
				R.array.landAffectedArray);
		ArrayAdapter<String> landAffectedAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, landAffectedArray);
		landAffectedAdapter
				.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		landAffectedSpinner.setAdapter(landAffectedAdapter);

		final ArrayAdapter<String> landPollutantsAdapter = new ArrayAdapter<String>(
				this, R.layout.simple_spinner_item);
		String[] landPollutantsArray = getResources().getStringArray(
				R.array.landPollutantsArray);
		for (int i = 0; i < landPollutantsArray.length; i++) {
			landPollutantsAdapter.add(landPollutantsArray[i]);
		}

		landPollutantsSpinner.setAdapter(landPollutantsAdapter, false,
				new MultiSpinnerListener() {

					@Override
					public void onItemsSelected(boolean[] selected) {
						StringBuilder builder = new StringBuilder();

						for (int i = 0; i < selected.length; i++) {
							if (selected[i]) {
								builder.append(landPollutantsAdapter.getItem(i))
										.append(" ");
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
			Intent intent = new Intent(v.getContext(), AddPhotosActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}

	}
}
