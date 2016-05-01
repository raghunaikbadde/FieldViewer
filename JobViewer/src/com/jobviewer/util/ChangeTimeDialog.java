package com.jobviewer.util;

import java.sql.Time;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.lanesgroup.jobviewer.R;

public class ChangeTimeDialog extends Activity implements OnClickListener {

	private Button mCancel, mContinue;
	private TimePicker mTimePicker;
	private DatePicker mDatePicker;
	private CheckBox mCheckOverride;
	String eventType;
	String eventTypeValue;
	String errorMsg;
	private boolean mIsChecked;
	
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
		mCheckOverride = (CheckBox) findViewById(R.id.checkBox1);
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
		String eventType1=null;
		if (view == mCancel) {
			finish();
		} else if (view == mContinue) {

			long dateTime = mDatePicker.getCalendarView().getDate();
			Date date = new Date(dateTime);

			DateFormat dateFormat = new SimpleDateFormat(
					Constants.DATE_PICKER_FORMAT);
			String formattedDate = dateFormat.format(date);
			Integer currentMinute = mTimePicker.getCurrentMinute();
			String minute = "";
			if(currentMinute < 10){
				minute = "0"+String.valueOf(currentMinute);
			} else {
				minute = String.valueOf(currentMinute);
			}
			Integer currentHour = mTimePicker.getCurrentHour();
			String hour = "";
			if(currentHour < 10){
				hour = "0"+String.valueOf(currentHour);
						
			} else{
				hour = String.valueOf(currentHour);
			}
			String time = hour + ":"
					+ minute + ":" + "00 "
					+ formattedDate;

			boolean isValidTime = validateTime(view.getContext(),time);
			if (isValidTime) {
				if ("start".equalsIgnoreCase(eventType)) {
					Utils.timeSheetRequest.setOverride_timestamp(time);
					eventTypeValue = "start";
				} else if ("travel".equalsIgnoreCase(eventType)) {
					Utils.startTravelTimeRequest.setOverride_timestamp(time);
					Log.d(Utils.LOG_TAG," override start travel time stamp "+time);
					eventTypeValue = "travel";
				} else if ("End Travel".equalsIgnoreCase(eventType)) {
					Utils.endTravelTimeRequest.setOverride_timestamp(time);
					Log.d(Utils.LOG_TAG," override end travel time stamp "+time);					
					eventTypeValue = eventType;
				} else if("ClockIn".equalsIgnoreCase(eventType)){
					eventType1=(String) getIntent().getExtras().get("eventType1");
					if (eventType1.equalsIgnoreCase(Utils.SHIFT_START)) {
						Utils.startShiftTimeRequest.setOverride_timestamp(time);
					}else{
						Utils.callStartTimeRequest.setOverride_timestamp(time);
					}
					eventTypeValue=eventType;
				} else {
					Utils.endTimeRequest.setOverride_timestamp(time);
					eventTypeValue = "endtravel";
				}
				Intent intent = new Intent();
				intent.putExtra(Constants.TIME, time);
				intent.putExtra("eventType", eventTypeValue);
				if (!Utils.isNullOrEmpty(eventType1)) {
					intent.putExtra("eventType1", eventType1);
				}
				setResult(RESULT_OK, intent);
				finish();
			} else {
				Toast.makeText(view.getContext(), errorMsg, Toast.LENGTH_SHORT)
						.show();
			}

		}
		
		

	}

	private boolean validateTime(Context context, String time) {
		if ("start".equalsIgnoreCase(eventType)) {
			Utils.timeSheetRequest.setOverride_timestamp(time);
		} else if ("travel".equalsIgnoreCase(eventType)) {
			CheckOutObject checkOutRemember = JobViewerDBHandler.getCheckOutRemember(context);
			if (!Utils.checkIfStartDateIsGreater(checkOutRemember.getJobStartedTime(), time)) {
				errorMsg=context.getResources().getString(R.string.dateAndTimeMustAfterShiftStart)+" ("+checkOutRemember.getJobStartedTime()+")";
				return false;
			}else if(!Utils.checkIfStartDateIsGreater(time,Utils.getCurrentDateAndTime())){
				errorMsg=context.getResources().getString(R.string.pastDateValidationErrorMsg);
				return false;
			}
			
		} else if ("End Travel".equalsIgnoreCase(eventType)) {
			CheckOutObject checkOutRemember = JobViewerDBHandler.getCheckOutRemember(context);
			if (!Utils.checkIfStartDateIsGreater(checkOutRemember.getTravelStartedTime(), time)) {
				errorMsg=context.getResources().getString(R.string.dateTimeShouldBeAfterTravelErrorMsg)+" ("+checkOutRemember.getTravelStartedTime()+")";
				return false;
			}else if(!Utils.checkIfStartDateIsGreater(time,Utils.getCurrentDateAndTime())){
				errorMsg=context.getResources().getString(R.string.pastDateValidationErrorMsg);
				return false;
			}
		} else if ("ClockIn".equalsIgnoreCase(eventType)) {
			if(!Utils.checkIfStartDateIsGreater(time,Utils.getCurrentDateAndTime())){
				errorMsg=context.getResources().getString(R.string.pastDateValidationErrorMsg);
				return false;
			} else{
				String eventType1=(String) getIntent().getExtras().get("eventType1");
				if (eventType1.equalsIgnoreCase(Utils.SHIFT_START)) {
					Utils.startShiftTimeRequest.setOverride_timestamp(time);
				}else{
					Utils.callStartTimeRequest.setOverride_timestamp(time);
				}
				
			}
		} else {
			Utils.endTimeRequest.setOverride_timestamp(time);
			eventTypeValue = "endtravel";
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	private String getTime(String hour, String min) {
		Time tme = new Time(Integer.parseInt(hour), Integer.parseInt(min), 0);
		Format formatter;
		formatter = new SimpleDateFormat("HH:mm a");
		return formatter.format(tme);
	}
}
