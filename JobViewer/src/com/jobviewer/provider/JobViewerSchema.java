package com.jobviewer.provider;

import com.jobviewer.provider.JobViewerProviderContract.CheckOutRemember;
import com.jobviewer.provider.JobViewerProviderContract.Image;
import com.jobviewer.provider.JobViewerProviderContract.QuestionSetTable;
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

	public static final String CREATE_TABLE_USER = "create table " + TABLE_USER
			+ "(" + User._ID + " integer primary key autoincrement,"
			+ User.FIRST_NAME + " text," + User.LAST_NAME + " text,"
			+ User.EMAIL + " text," + User.USER_ID + " text);";
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
			+ Image.IMAGE_STRING + " text," + Image.IMAGE_CATEGORY + " text,"+ Image.IMAGE_EXIF + " text,"
			+ Image.IMAGE_URL + " text);";

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
			+ CheckOutRemember.CLOCKIN_CHECKOUT_SELECTED_TEXT + " text);";

	public static final String CREATE_TABLE_QUESTION_SET = "create table "
			+ TABLE_QUESTION_SET + "(" + QuestionSetTable._ID
			+ " integer primary key autoincrement,"
			+ QuestionSetTable.WORK_TYPE + " text,"
			+ QuestionSetTable.QUESTION_SET + " text,"
			+ QuestionSetTable.BACK_STACK + " text);";
}
