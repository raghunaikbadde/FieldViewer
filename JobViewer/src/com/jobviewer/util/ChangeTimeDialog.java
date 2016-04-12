package com.jobviewer.util;

import java.sql.Time;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		mDatePicker.init(year, month, day, null);
		mTimePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
		mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View view) {
		if (view == mCancel) {
			finish();
		} else if (view == mContinue) {
			
			long dateTime = mDatePicker.getCalendarView().getDate();
			Date date = new Date(dateTime);
			
			DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_PICKER_FORMAT);
			String formattedDate = dateFormat.format(date);
			String time=mTimePicker.getCurrentHour()+":"+mTimePicker.getCurrentMinute()+":"+"00 "+formattedDate;
			
			/*String time = mDatePicker.getDayOfMonth()
					+ "/"
					+ mDatePicker.getMonth()
					+ 1
					+ "/"
					+ mDatePicker.getYear()
					+ " "
					+ getTime("" + mTimePicker.getCurrentHour(), ""
							+ mTimePicker.getCurrentMinute());*/
			/*time = Utils.convertTimeOneToAnotherFormat(time,
					Constants.CHANGE_TIME_FORMAT, Constants.TIME_FORMAT);*/
			if ("start".equalsIgnoreCase(eventType)) {
				Utils.timeSheetRequest.setOverride_timestamp(time);
				eventTypeValue = "start";
			} else if ("travel".equalsIgnoreCase(eventType)) {
				Utils.startTravelTimeRequest.setOverride_timestamp(time);
				eventTypeValue = "travel";
			} else if("End Travel".equalsIgnoreCase(eventType)){
				Utils.endTravelTimeRequest.setOverride_timestamp(time);
				eventTypeValue=eventType;
			}else {
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
