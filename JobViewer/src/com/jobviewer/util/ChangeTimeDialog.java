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
import android.graphics.drawable.ColorDrawable;
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

import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.lanesgroup.jobviewer.R;

public class ChangeTimeDialog extends Activity implements OnClickListener {

	private Button mCancel, mContinue;
	private TimePicker mTimePicker;
	private DatePicker mDatePicker;
	private CheckBox mCheckOverride;
	private String eventType;
	private String eventTypeValue;
	private String errorMsg;
	private boolean mIsChecked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		eventType = (String) getIntent().getExtras().get("eventType");
		this.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		setContentView(R.layout.timestamp_dialog);
		mCancel = (Button) findViewById(R.id.dialog_cancel);
		mCancel.setOnClickListener(this);
		mContinue = (Button) findViewById(R.id.dialog_ok);
		mContinue.setOnClickListener(this);
		mTimePicker = (TimePicker) findViewById(R.id.timePicker);
		mDatePicker = (DatePicker) findViewById(R.id.datePicker);
		mCheckOverride = (CheckBox) findViewById(R.id.checkBox1);
		mTimePicker.setIs24HourView(true);
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
		String eventType1 = null;
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
			if (currentMinute < 10) {
				minute = "0" + String.valueOf(currentMinute);
			} else {
				minute = String.valueOf(currentMinute);
			}
			Integer currentHour = mTimePicker.getCurrentHour();
			String hour = "";
			if (currentHour < 10) {
				hour = "0" + String.valueOf(currentHour);

			} else {
				hour = String.valueOf(currentHour);
			}
			String time = hour + ":" + minute + ":" + "00 " + formattedDate;

			boolean isValidTime = validateTime(view.getContext(), time);
			if (isValidTime) {
				if ("start".equalsIgnoreCase(eventType)) {
					Utils.timeSheetRequest
							.setIs_overriden(ActivityConstants.TRUE);
					Utils.timeSheetRequest.setOverride_timestamp(time);
					eventTypeValue = "start";
				} else if ("travel".equalsIgnoreCase(eventType)) {
					Utils.startTravelTimeRequest
							.setIs_overriden(ActivityConstants.TRUE);
					Utils.startTravelTimeRequest.setOverride_timestamp(time);
					Log.d(Utils.LOG_TAG, " override start travel time stamp "
							+ time);
					eventTypeValue = "travel";
				} else if ("End Travel".equalsIgnoreCase(eventType)) {
					Utils.endTravelTimeRequest
							.setIs_overriden(ActivityConstants.TRUE);
					Utils.endTravelTimeRequest.setOverride_timestamp(time);
					Log.d(Utils.LOG_TAG, " override end travel time stamp "
							+ time);
					eventTypeValue = eventType;
				} else if ("ClockIn".equalsIgnoreCase(eventType)) {
					eventType1 = (String) getIntent().getExtras().get(
							"eventType1");
					if (eventType1.equalsIgnoreCase(Utils.SHIFT_START)) {
						Utils.startShiftTimeRequest
								.setIs_overriden(ActivityConstants.TRUE);
						Utils.startShiftTimeRequest.setOverride_timestamp(time);
					} else {
						Utils.callStartTimeRequest
								.setIs_overriden(ActivityConstants.TRUE);
						Utils.callStartTimeRequest.setOverride_timestamp(time);
					}
					eventTypeValue = eventType;
				} else if ("EndOnCall".equalsIgnoreCase(eventType)) {
					eventTypeValue = eventType;
					eventType1 = (String) getIntent().getExtras().get(
							"eventType1");
					Utils.callEndTimeRequest
							.setIs_overriden(ActivityConstants.TRUE);
					Utils.callEndTimeRequest.setOverride_timestamp(time);
				} else if ("EndShift".equalsIgnoreCase(eventType)) {
					eventTypeValue = eventType;
					eventType1 = (String) getIntent().getExtras().get(
							"eventType1");
					Utils.endShiftRequest
							.setIs_overriden(ActivityConstants.TRUE);
					Utils.endShiftRequest.setOverride_timestamp(time);
				} else {
					Utils.endTimeRequest
							.setIs_overriden(ActivityConstants.TRUE);
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
			BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler
					.getBreakShiftTravelCall(context);
			String shiftStartTime = Utils
					.formattedDateFromMillis(breakShiftTravelCall
							.getShiftStartTime());
			String presentTime = Utils.getCurrentDateAndTime();

			if (!Utils.checkIfStartDateIsGreater(shiftStartTime, time)) {
				errorMsg = context.getResources().getString(
						R.string.dateAndTimeMustAfterShiftStart)
						+ " (" + shiftStartTime + ")";
				return false;
			} else if (!Utils.checkIfStartDateIsGreater(time, presentTime)) {
				errorMsg = context.getResources().getString(
						R.string.pastDateValidationErrorMsg);
				return false;
			}
			Utils.timeSheetRequest.setOverride_timestamp(time);
		} else if ("End Break".equalsIgnoreCase(eventType)) {
			BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler
					.getBreakShiftTravelCall(context);
			String breakStartTime = breakShiftTravelCall.getBreakStartedTime();
			String presentTime = Utils.getCurrentDateAndTime();

			if (!Utils.checkIfStartDateIsGreater(breakStartTime, time)) {
				errorMsg = context.getResources().getString(
						R.string.dateAndTimeMustAfterBreakStart)
						+ " (" + breakStartTime + ")";
				return false;
			} else if (!Utils.checkIfStartDateIsGreater(time, presentTime)) {
				errorMsg = context.getResources().getString(
						R.string.pastDateValidationErrorMsg);
				return false;
			}
			Utils.timeSheetRequest.setOverride_timestamp(time);
		} else if ("EndShift".equalsIgnoreCase(eventType)) {
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(context);

			String shiftStartTime = checkOutRemember.getJobStartedTime();
			String presentTime = Utils.getCurrentDateAndTime();
			if (!Utils.checkIfStartDateIsGreater(
					checkOutRemember.getJobStartedTime(), time)) {
				errorMsg = context.getResources().getString(
						R.string.dateAndTimeMustAfterShiftStart)
						+ " (" + checkOutRemember.getJobStartedTime() + ")";
				return false;
			} else if (!Utils.checkIfStartDateIsGreater(time, presentTime)) {
				errorMsg = context.getResources().getString(
						R.string.pastDateValidationErrorMsg);
				return false;
			}

			Utils.endShiftRequest.setOverride_timestamp(time);
		} else if ("travel".equalsIgnoreCase(eventType)) {
			BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler
					.getBreakShiftTravelCall(context);
			String timeToComparre = "";
			if (!Utils.isNullOrEmpty(breakShiftTravelCall.getCallStartTime()))
				timeToComparre = Utils
						.formattedDateFromMillis(breakShiftTravelCall
								.getCallStartTime());
			else
				timeToComparre = Utils
						.formattedDateFromMillis(breakShiftTravelCall
								.getShiftStartTime());
			if (!Utils.checkIfStartDateIsGreater(timeToComparre, time)) {
				errorMsg = context.getResources().getString(
						R.string.dateAndTimeMustAfterShiftStart)
						+ " (" + timeToComparre + ")";
				return false;
			} else if (!Utils.checkIfStartDateIsGreater(time,
					Utils.getCurrentDateAndTime())) {
				errorMsg = context.getResources().getString(
						R.string.pastDateValidationErrorMsg);
				return false;
			}

		} else if ("End Travel".equalsIgnoreCase(eventType)) {
			BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler
					.getBreakShiftTravelCall(context);
			String timeToComparre = "";

			if (Utils
					.isNullOrEmpty(breakShiftTravelCall.getTravelStartedTime())) {
				CheckOutObject checkOutObject = JobViewerDBHandler
						.getCheckOutRemember(context);
				timeToComparre = checkOutObject.getTravelStartedTime();
			} else
				timeToComparre = Utils
						.formattedDateFromMillis(breakShiftTravelCall
								.getTravelStartedTime());
			if (!Utils.checkIfStartDateIsGreater(timeToComparre, time)) {
				errorMsg = context.getResources().getString(
						R.string.dateTimeShouldBeAfterTravelErrorMsg)
						+ " (" + timeToComparre + ")";
				return false;
			} else if (!Utils.checkIfStartDateIsGreater(time,
					Utils.getCurrentDateAndTime())) {
				errorMsg = context.getResources().getString(
						R.string.pastDateValidationErrorMsg);
				return false;
			}
		} else if ("ClockIn".equalsIgnoreCase(eventType)) {
			if (!Utils.checkIfStartDateIsGreater(time,
					Utils.getCurrentDateAndTime())) {
				errorMsg = context.getResources().getString(
						R.string.pastDateValidationErrorMsg);
				return false;
			} else {
				String eventType1 = (String) getIntent().getExtras().get(
						"eventType1");
				if (!Utils.isNullOrEmpty(eventType1)) {
					if (eventType1.equalsIgnoreCase(Utils.SHIFT_START)) {
						Utils.startShiftTimeRequest
								.setIs_overriden(ActivityConstants.TRUE);
						Utils.startShiftTimeRequest.setOverride_timestamp(time);
					} else {
						Utils.callStartTimeRequest
								.setIs_overriden(ActivityConstants.TRUE);
						Utils.callStartTimeRequest.setOverride_timestamp(time);
					}
				}

			}
		} else if ("EndOnCall".equalsIgnoreCase(eventType)) {
			String timeToCompare = Utils
					.formattedDateFromMillis(JobViewerDBHandler
							.getBreakShiftTravelCall(context)
							.getCallStartTime());
			String presentTime = Utils.getCurrentDateAndTime();
			if (!Utils.checkIfStartDateIsGreater(timeToCompare, time)) {
				errorMsg = context.getResources().getString(
						R.string.dateAndTimeMustAfterCallStart)
						+ " (" + timeToCompare + ")";
				return false;
			} else if (!Utils.checkIfStartDateIsGreater(time, presentTime)) {
				errorMsg = context.getResources().getString(
						R.string.pastDateValidationErrorMsg);
				return false;
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
