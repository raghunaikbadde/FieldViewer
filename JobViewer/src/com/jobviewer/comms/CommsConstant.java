package com.jobviewer.comms;

public class CommsConstant {
	
	public static String HOST="http://mohin-fieldviewer-api.dev.aecortech.com/";
	public static String STARTUP_API="api/v1/startup";
	public static String START_SHIFT_API="api/v1/timesheets/shiftstart";
	public static String END_SHIFT_API="api/v1/timesheets/shiftend";
	public static String START_BREAK_API="api/v1/timesheets/breakstart";
	public static String END_BREAK_API="api/v1/timesheets/breakend";
	public static String START_WORK_API="api/v1/timesheets/workstart";
	public static String END_WORK_API="api/v1/timesheets/workend";
	public static String START_TRAINING_API="api/v1/timesheets/trainingstart";
	public static String END_TRAINING_API="api/v1/timesheets/trainingend";
	public static String INACTIVITY_API="api/v1/timesheets/inactivityalert";
	public static String START_ON_CALL_API="api/v1/timesheets/oncallstart";
	public static String END_ON_CALL_API="api/v1/timesheets/oncallend";
	public static String START_TRAVEL_API="api/v1/timesheets/paidtravelstart";
	public static String END_TRAVEL_API="api/v1/timesheets/paidtravelend";
	
	public static String CHECKIN_VEHICLE="api/v1/vehicles/checkin";
	public static String CHECKOUT_VEHICLE="api/v1/vehicles/checkout";
	
	public static String WORK_PHOTO_UPLOAD="api/v1/works/upload";//{work_id};
	public static String WORK_CREATE_API = "api/v1/works/store";
	public static String SURVEY_PHOTO_UPLOAD="api/v1/image/upload";

}
