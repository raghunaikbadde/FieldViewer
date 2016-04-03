package com.jobviewer.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class JobViewerProviderContract {
	public static final String AUTHORITY = "com.jobviewer.contentprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final String SELECTION_ID_BASED = BaseColumns._ID + " = ? ";
	public static final String SELECTION_SOURCE_ID_BASED = SourceIdAware.SOURCE_ID
			+ " = ? ";

	public static interface SourceIdAware extends BaseColumns {

		/**
		 * The unique source id for an entity.
		 */
		public static final String SOURCE_ID = "sourceid";
	}

	public static final class User implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				JobViewerProviderContract.CONTENT_URI, "User");
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_WALLET_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_USER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer.user";

		public static final String FIRST_NAME = "firstname";
		public static final String LAST_NAME = "lastname";
		public static final String EMAIL = "email";
		public static final String USER_ID = "userid";

	}

	public static final class TimeSheet implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				JobViewerProviderContract.CONTENT_URI, "Timesheet");
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_WALLET_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_USER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer.timesheet";

		public static final String STARTED_AT = "started_at";
		public static final String RECORD_FOR = "record_for";
		public static final String IS_INACTIVE = "is_inactive";
		public static final String IS_OVERRIDEN = "is_overriden";
		public static final String OVERRIDE_REASON = "override_reason";
		public static final String OVERRIDE_COMMENT = "override_comment";
		public static final String OVERRIDE_TIMESTAMP = "override_timestamp";
		public static final String REFERENCE_ID = "reference_id";
		public static final String USER_ID = "user_id";
		public static final String API_URL = "api_url";

	}

	public static final class Image implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				JobViewerProviderContract.CONTENT_URI, "Images");
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_WALLET_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_USER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer.images";

		public static final String IMAGE_ID = "image_id";
		public static final String IMAGE_STRING = "image_string";
		public static final String IMAGE_URL = "image_url";
		public static final String IMAGE_CATEGORY="category";
		public static final String IMAGE_EXIF="image_exif";

	}

	public static final class CheckOutRemember implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				JobViewerProviderContract.CONTENT_URI, "CheckOutRemember");
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_WALLET_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_USER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer.checkOutRemember";

		public static final String JOB_SELECTED = "jobSelected";
		public static final String IS_CHECKED_OUT_SELECTED = "isChecekOutSelected";
		public static final String CLOCKIN_CHECKOUT_SELECTED_TEXT = "clockInCheckOutSelectedText";
		public static final String VEHICLE_REGISTRATION = "vehicleRegistration";
		public static final String MILAGE = "milage";
		public static final String REMEMBER_MY_SELECTION = "rememberMySelection";
		public static final String JOB_STARTED_TIME = "jobStartedTime";
		public static final String ASSESSMENT_SELECTED = "assessmentSelected";
		public static final String ASSESSMENT_REMEMBER_SELECTED="isAssessmentRemember";
		public static final String IS_TRAVEL_STARTED = "isStartedTravel";
		public static final String IS_TRAVEL_END = "isTravelEnd";
		public static final String IS_POLLUTION_SELECTED = "isPollutionSelected";
		public static final String VISTEC_ID = "vistecId";
		public static final String VISTEC_IMAGE_ID="vistecImageId";
		public static final String IS_ASSESSMENT_COMPLETED="isAssessmentCompleted";
		public static final String WORK_ID="workId";
	}
	
	public static final class BackLogTable implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				JobViewerProviderContract.CONTENT_URI, "BackLogTable");
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_WALLET_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_USER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer.backLogTable";

		public static final String REQUEST_TYPE = "requestType";
		public static final String REQUEST_JSON = "requestJson";
		public static final String REQUEST_API = "requestApi";
		public static final String REQUEST_CLASS_NAME = "requestClassName";
	}
	
	public static final class QuestionSetTable implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				JobViewerProviderContract.CONTENT_URI, "QuestionSetTable");
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_WALLET_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_USER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer.questionSetTable";

		public static final String WORK_TYPE ="wokType";
		public static final String QUESTION_SET="questionJson";
		public static final String BACK_STACK="backStack";
	}
	
	public static final class AddPhotosScreenSavedImages implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				JobViewerProviderContract.CONTENT_URI, "AddPhotosScreenSavedImages");
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_WALLET_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_USER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer.addPhotosScreenSavedImages";

		public static final String IMAGE_ID ="imageId";
		public static final String IMAGE_STRING="image_string";
		public static final String IMAGE_URL="image_url";
		public static final String IMAGE_CATEGORY="category";
		public static final String IMAGE_EXIF="image_exif";
	}
	
	public static final class ShoutAboutSafetyTable implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				JobViewerProviderContract.CONTENT_URI, "ShoutAboutSafetyTable");
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_WALLET_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_USER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer.ShoutAboutSafetyTable";

		public static final String OPTION_SELECTED ="optionSelected";
		public static final String QUESTION_SET="questionSet";
		public static final String STARTEDAT="StartedAt";
	}
	
	public static final class StartTrainingTable implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				JobViewerProviderContract.CONTENT_URI, "StartTrainingTable");
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_WALLET_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_USER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer.StartTrainingTable";

		public static final String IS_TRAINING_STARTED ="isTrainingStarted";
		public static final String TRAINING_START_TIME="trainingStartTime";
		public static final String TRAINING_END_TIME="trainingEndTime";
	}

	public static final class BreakTravelShiftCallTable implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				JobViewerProviderContract.CONTENT_URI, "BreakTravelShiftCallTable");
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_WALLET_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer";
		public static final String CONTENT_USER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jobviewer.BreakTravelShiftCallTable";

		public static final String IS_BREAK_STARTED ="isBreakStarted";
		public static final String BREAK_START_TIME ="breakStartedTime";		
		public static final String BREAK_END_TIME ="breakEndTime";
		
		public static final String IS_SHIFT_STARTED ="isShiftStarted";
		public static final String SHIFT_START_TIME ="shiftStartTime";
		public static final String SHIFT_END_TIME ="shiftEndTime";
		
		public static final String IS_CALL_STARTED ="isCallStarted";
		public static final String CALL_START_TIME ="callStartTime";
		public static final String CALL_END_TIME ="callEndTime";
		
		public static final String NUMBER_OF_BREAKS ="noOfBreaks";
		
		public static final String IS_TRAVEL_STARTED="isTravelStarted";
		public static final String TRAVEL_START_TIME ="travelStartedTime";		
		public static final String TRAVEL_END_TIME ="travelEndTime";
	}
	
}