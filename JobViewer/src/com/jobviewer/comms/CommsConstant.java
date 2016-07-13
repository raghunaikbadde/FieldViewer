package com.jobviewer.comms;

public class CommsConstant {

	public static final String HOST = "http://mohin-fieldviewer-api.dev.aecortech.com/";
	// public static final String HOST = "http://api.qa-jobviewer.lanes-i.com/";
	public static final String STARTUP_API = "api/v1/startup";
	public static final String START_SHIFT_API = "api/v1/timesheets/shiftstart";
	public static final String END_SHIFT_API = "api/v1/timesheets/shiftend";
	public static final String START_BREAK_API = "api/v1/timesheets/breakstart";
	public static final String END_BREAK_API = "api/v1/timesheets/breakend";
	public static final String START_WORK_API = "api/v1/timesheets/workstart";
	public static final String END_WORK_API = "api/v1/timesheets/workend";
	public static final String START_TRAINING_API = "api/v1/timesheets/trainingstart";
	public static final String END_TRAINING_API = "api/v1/timesheets/trainingend";
	public static final String INACTIVITY_API = "api/v1/timesheets/inactivityalert";
	public static final String START_ON_CALL_API = "api/v1/timesheets/oncallstart";
	public static final String END_ON_CALL_API = "api/v1/timesheets/oncallend";
	public static final String START_TRAVEL_API = "api/v1/timesheets/paidtravelstart";
	public static final String END_TRAVEL_API = "api/v1/timesheets/paidtravelend";

	public static final String CHECKIN_VEHICLE = "api/v1/vehicles/checkin";
	public static final String CHECKOUT_VEHICLE = "api/v1/vehicles/checkout";
	public static final String VEHICLE_REGISTRATIONS = "api/v1/vehicles/registrations";

	public static final String WORK_PHOTO_UPLOAD = "api/v1/works/upload";// {work_id};
	public static final String WORK_CREATE_API = "api/v1/works/store";
	public static final String WORK_UPDATE_API = "api/v1/works/update";
	public static final String SURVEY_PHOTO_UPLOAD = "api/v1/image/upload";
	public static final String POLLUTION_REPORT_UPLOAD = "api/v1/pollution/store";
	public static final String SYNC_API = "api/v1/sync";
	public static final String SURVEY_STORE_API = "api/v1/surveys/store";

}
