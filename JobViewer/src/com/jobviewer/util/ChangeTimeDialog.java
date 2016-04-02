package com.jobviewer.util;

import java.sql.Time;
import java.text.Format;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.lanesgroup.jobviewer.R;

public class ChangeTimeDialog extends Activity implements OnClickListener {

	private Button mCancel, mContinue;
	private TimePicker mTimePicker;
	private DatePicker mDatePicker;
	String eventType;
	String eventTypeValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		eventType = (String) getIntent().getExtras().get("eventType");
		this.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		setContentView(R.layout.timestamp_dialog);
		mCancel = (Button) findViewById(R.id.dialog_cancel);
		mCancel.setOnClickListener(this);
		mContinue = (Button) findViewById(R.id.dialog_ok);
		mContinue.setOnClickListener(this);
		mTimePicker = (TimePicker) findViewById(R.id.timePicker);
		mDatePicker = (DatePicker) findViewById(R.id.datePicker);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View view) {
		if (view == mCancel) {
			finish();
		} else if (view == mContinue) {
			String time = mDatePicker.getDayOfMonth()
					+ "/"
					+ mDatePicker.getMonth()
					+ 1
					+ "/"
					+ mDatePicker.getYear()
					+ " "
					+ getTime("" + mTimePicker.getCurrentHour(), ""
							+ mTimePicker.getCurrentMinute());
			if ("start".equalsIgnoreCase(eventType)) {
				Utils.timeSheetRequest.setOverride_timestamp(time);
				eventTypeValue = "start";
			} else if ("travel".equalsIgnoreCase(eventType)) {
				Utils.startTravelTimeRequest.setOverride_timestamp(time);
				eventTypeValue = "travel";
			} else {
				Utils.endTimeRequest.setOverride_timestamp(time);
				eventTypeValue = "endtravel";				
			}
			Intent intent = new Intent();
			intent.putExtra(Constants.TIME, time);
			intent.putExtra("eventType", eventTypeValue);
			setResult(RESULT_OK, intent);
			finish();
		}

	}

	@SuppressWarnings("deprecation")
	private String getTime(String hour, String min) {
		Time tme = new Time(Integer.parseInt(hour), Integer.parseInt(min), 0);
		Format formatter;
		formatter = new SimpleDateFormat("HH:mm a");
		return formatter.format(tme);
	}
}
