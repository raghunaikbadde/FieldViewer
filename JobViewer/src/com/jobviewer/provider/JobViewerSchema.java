package com.jobviewer.provider;

import com.jobviewer.provider.JobViewerProviderContract.AddPhotosScreenSavedImages;
import com.jobviewer.provider.JobViewerProviderContract.BackLogTable;
import com.jobviewer.provider.JobViewerProviderContract.BreakTravelShiftCallTable;
import com.jobviewer.provider.JobViewerProviderContract.CheckOutRemember;
import com.jobviewer.provider.JobViewerProviderContract.ConfinedQuestionSetTable;
import com.jobviewer.provider.JobViewerProviderContract.Image;
import com.jobviewer.provider.JobViewerProviderContract.ImageSendStatusTable;
import com.jobviewer.provider.JobViewerProviderContract.QuestionSetTable;
import com.jobviewer.provider.JobViewerProviderContract.ShoutAboutSafetyTable;
import com.jobviewer.provider.JobViewerProviderContract.StartTrainingTable;
import com.jobviewer.provider.JobViewerProviderContract.TimeSheet;
import com.jobviewer.provider.JobViewerProviderContract.User;

public interface JobViewerSchema {

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "jobviewerdb";

	public static final String TABLE_USER = "User";
	public static final String TABLE_TIMESHEET = "Timesheet";
	public static final String TABLE_IMAGE = "Images";
	public static final String TABLE_CHECK_OUT_REMEMBER = "CheckOutRemember";
	public static final String TABLE_QUESTION_SET = "QuestionSetTable";
	public static final String TABLE_BACK_LOG = "BackLogTable";
	public static final String TABLE_ADD_PHOTOS_SCREEN_SAVED_IMAGES = "AddPhotosScreenSavedImages";
	public static final String TABLE_SHOUT_ABOUT_SAFETY = "ShoutAboutSafetyTable";
	public static final String TABLE_START_TRAINING = "StartTrainingTable";
	public static final String TABLE_BREAK_TRAVEL_SHIFT_CALL = "BreakTravelShiftCallTable";
	public static final String TABLE_IMAGE_SEND_STATUS = "ImageSendStatusTable";
	public static final String TABLE_CONFINED_QUESTION_SET = "ConfinedQuestionSetTable";

	public static final String CREATE_TABLE_USER = "create table " + TABLE_USER
			+ "(" + User._ID + " integer primary key autoincrement,"
			+ User.FIRST_NAME + " text," + User.LAST_NAME + " text,"
			+ User.EMAIL + " text," + User.USER_ID + " text);";

	public static final String CREATE_TABLE_IMAGE_SEND_STATUS = "create table "
			+ TABLE_IMAGE_SEND_STATUS + "(" + ImageSendStatusTable._ID
			+ ImageSendStatusTable.IMAGE_SEND_SATTUS + " text);";
	public static final String CREATE_TABLE_TIMESHEET = "create table "
			+ TABLE_TIMESHEET + "(" + TimeSheet._ID
			+ " integer primary key autoincrement," + TimeSheet.STARTED_AT
			+ " text," + TimeSheet.RECORD_FOR + " text,"
			+ TimeSheet.IS_INACTIVE + " text," + TimeSheet.IS_OVERRIDEN
			+ " text," + TimeSheet.OVERRIDE_REASON + " text,"
			+ TimeSheet.OVERRIDE_COMMENT + " text,"
			+ TimeSheet.OVERRIDE_TIMESTAMP + " text," + TimeSheet.REFERENCE_ID
			+ " text," + TimeSheet.USER_ID + " text," + TimeSheet.API_URL
			+ " text);";
	public static final String CREATE_TABLE_IMAGES = "create table "
			+ TABLE_IMAGE + "(" + Image._ID
			+ " integer primary key autoincrement," + Image.IMAGE_ID + " text,"
			+ Image.IMAGE_STRING + " text," + Image.IMAGE_CATEGORY + " text,"
			+ Image.IMAGE_EXIF + " text," + Image.IMAGE_URL + " text);";

	public static final String CREATE_TABLE_CHECK_OUT_REMEMBER = "create table "
			+ TABLE_CHECK_OUT_REMEMBER
			+ "("
			+ CheckOutRemember._ID
			+ " integer primary key autoincrement,"
			+ CheckOutRemember.JOB_SELECTED
			+ " text,"
			+ CheckOutRemember.IS_CHECKED_OUT_SELECTED
			+ " text,"
			+ CheckOutRemember.VEHICLE_REGISTRATION
			+ " text,"
			+ CheckOutRemember.MILAGE
			+ " text,"
			+ CheckOutRemember.REMEMBER_MY_SELECTION
			+ " text,"
			+ CheckOutRemember.JOB_STARTED_TIME
			+ " text,"
			+ CheckOutRemember.ASSESSMENT_SELECTED
			+ " text,"
			+ CheckOutRemember.IS_TRAVEL_STARTED
			+ " text,"
			+ CheckOutRemember.IS_TRAVEL_END
			+ " text,"
			+ CheckOutRemember.IS_POLLUTION_SELECTED
			+ " text,"
			+ CheckOutRemember.VISTEC_ID
			+ " text,"
			+ CheckOutRemember.VISTEC_IMAGE_ID
			+ " text,"
			+ CheckOutRemember.ASSESSMENT_REMEMBER_SELECTED
			+ " text,"
			+ CheckOutRemember.IS_ASSESSMENT_COMPLETED
			+ " text,"
			+ CheckOutRemember.WORK_ID
			+ " text,"
			+ CheckOutRemember.TRAVEL_STARTED_TIME
			+ " text,"
			+ CheckOutRemember.CLOCKIN_CHECKOUT_SELECTED_TEXT + " text);";

	public static final String CREATE_TABLE_QUESTION_SET = "create table "
			+ TABLE_QUESTION_SET + "(" + QuestionSetTable._ID
			+ " integer primary key autoincrement,"
			+ QuestionSetTable.WORK_TYPE + " text,"
			+ QuestionSetTable.QUESTION_SET + " text,"
			+ QuestionSetTable.BACK_STACK + " text);";
	
	public static final String CREATE_TABLE_CONFINED_QUESTION_SET = "create table "
			+ TABLE_CONFINED_QUESTION_SET + "(" + ConfinedQuestionSetTable._ID
			+ " integer primary key autoincrement,"
			+ ConfinedQuestionSetTable.WORK_TYPE + " text,"
			+ ConfinedQuestionSetTable.QUESTION_SET + " text,"
			+ ConfinedQuestionSetTable.BACK_STACK + " text);";

	public static final String CREATE_TABLE_BACK_LOG = "create table "
			+ TABLE_BACK_LOG + "(" + QuestionSetTable._ID
			+ " integer primary key autoincrement," + BackLogTable.REQUEST_TYPE
			+ " text," + BackLogTable.REQUEST_JSON + " text,"
			+ BackLogTable.REQUEST_API + " text,"
			+ BackLogTable.REQUEST_CLASS_NAME + " text);";

	public static final String CREATE_TABLE_ADD_PHOTOS_SCREEN_SAVED_IMAGES = "create table "
			+ TABLE_ADD_PHOTOS_SCREEN_SAVED_IMAGES
			+ "("
			+ QuestionSetTable._ID
			+ " integer primary key autoincrement,"
			+ AddPhotosScreenSavedImages.IMAGE_ID
			+ " text,"
			+ AddPhotosScreenSavedImages.IMAGE_STRING
			+ " text,"
			+ AddPhotosScreenSavedImages.IMAGE_URL
			+ " text,"
			+ AddPhotosScreenSavedImages.IMAGE_CATEGORY
			+ " text,"
			+ AddPhotosScreenSavedImages.IMAGE_EXIF + " text);";

	public static final String CREATE_TABLE_SHOUT_ABOUT_SAFETY = "create table "
			+ TABLE_SHOUT_ABOUT_SAFETY
			+ "("
			+ ShoutAboutSafetyTable._ID
			+ " integer primary key autoincrement,"
			+ ShoutAboutSafetyTable.OPTION_SELECTED
			+ " text,"
			+ ShoutAboutSafetyTable.STARTEDAT
			+ " text,"
			+ ShoutAboutSafetyTable.QUESTION_SET + " text);";

	public static final String CREATE_TABLE_START_TRAINING = "create table "
			+ TABLE_START_TRAINING + "(" + StartTrainingTable._ID
			+ " integer primary key autoincrement,"
			+ StartTrainingTable.IS_TRAINING_STARTED + " text,"
			+ StartTrainingTable.TRAINING_START_TIME + " text,"
			+ StartTrainingTable.TRAINING_END_TIME + " text);";

	public static final String CREATE_TABLE_BREAK_TRAVEL_SHIFT_CALL = "create table "
			+ TABLE_BREAK_TRAVEL_SHIFT_CALL
			+ "("
			+ User._ID
			+ " integer primary key autoincrement,"
			+ BreakTravelShiftCallTable.IS_BREAK_STARTED
			+ " text,"
			+ BreakTravelShiftCallTable.BREAK_START_TIME
			+ " text,"
			+ BreakTravelShiftCallTable.BREAK_END_TIME
			+ " text,"
			+ BreakTravelShiftCallTable.NUMBER_OF_BREAKS
			+ " INT,"
			+ BreakTravelShiftCallTable.IS_CALL_STARTED
			+ " text"
			+ ","
			+ BreakTravelShiftCallTable.CALL_START_TIME
			+ " text,"
			+ BreakTravelShiftCallTable.CALL_END_TIME
			+ " text,"
			+ BreakTravelShiftCallTable.IS_SHIFT_STARTED
			+ " text"
			+ ","
			+ BreakTravelShiftCallTable.SHIFT_START_TIME
			+ " text,"
			+ BreakTravelShiftCallTable.SHIFT_END_TIME
			+ " text,"
			+ BreakTravelShiftCallTable.IS_TRAVEL_STARTED
			+ " text"
			+ ","
			+ BreakTravelShiftCallTable.TRAVEL_START_TIME
			+ " text,"
			+ BreakTravelShiftCallTable.TRAVEL_END_TIME + " text,"
			+ BreakTravelShiftCallTable.WORK_START_TIME+ " text,"
			+ BreakTravelShiftCallTable.WORK_END_TIME + " text);";
}
