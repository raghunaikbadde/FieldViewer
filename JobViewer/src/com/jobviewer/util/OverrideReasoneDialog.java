package com.jobviewer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.lanesgroup.jobviewer.R;

public class OverrideReasoneDialog extends Activity implements OnClickListener,
		OnItemSelectedListener {

	private Button mCancel, mContinue;
	private TextView timeStampValue;
	private Spinner overrideReasonSpinner;
	String eventType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		setContentView(R.layout.override_reason_dialog);
		mCancel = (Button) findViewById(R.id.dialog_cancel);
		overrideReasonSpinner = (Spinner) findViewById(R.id.overrideReasonSpinner);
		eventType = getIntent().getExtras().getString("eventType");
		String[] stringArray = getResources().getStringArray(
				R.array.override_reason_comment);
		List<String> overrideReasonCommentList = new ArrayList<String>();
		Collections.addAll(overrideReasonCommentList, stringArray);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, overrideReasonCommentList);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// attaching data adapter to spinner
		overrideReasonSpinner.setAdapter(dataAdapter);
		overrideReasonSpinner.setOnItemSelectedListener(this);
		timeStampValue = (TextView) findViewById(R.id.timeStampValue);
		if (eventType.equalsIgnoreCase("start")) {
			timeStampValue.setText(Utils.timeSheetRequest
					.getOverride_timestamp());
		} else if (eventType.equalsIgnoreCase("travel")) {
			timeStampValue.setText(Utils.startTravelTimeRequest
					.getOverride_timestamp());
		} else if (eventType.equalsIgnoreCase("End Travel")) {
			timeStampValue.setText(Utils.endTravelTimeRequest
					.getOverride_timestamp());
		} else {
			timeStampValue.setText(Utils.endTimeRequest
					.getOverride_timestamp());
		}

		mCancel.setOnClickListener(this);
		mContinue = (Button) findViewById(R.id.dialog_ok);
		mContinue.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view == mCancel) {
			finish();
		} else if (view == mContinue) {

			Intent intent = new Intent();
			intent.putExtra(Constants.TIME, "");
			intent.putExtra("eventType", eventType);
			setResult(RESULT_OK, intent);
			finish();
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		String item = parent.getItemAtPosition(position).toString();
		if ("start".equalsIgnoreCase(eventType)) {
			Utils.timeSheetRequest.setOverride_comment(item);
			Utils.timeSheetRequest.setOverride_reason(item);
		} else  if ("travel".equalsIgnoreCase(eventType)) {
			Utils.startTravelTimeRequest.setOverride_comment(item);
			Utils.startTravelTimeRequest.setOverride_reason(item);
		} else  if ("End Travel".equalsIgnoreCase(eventType)) {
			Utils.endTravelTimeRequest.setOverride_comment(item);
			Utils.endTravelTimeRequest.setOverride_reason(item);
		} else {
			Utils.endTimeRequest.setOverride_comment(item);
			Utils.endTimeRequest.setOverride_reason(item);
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}
}
