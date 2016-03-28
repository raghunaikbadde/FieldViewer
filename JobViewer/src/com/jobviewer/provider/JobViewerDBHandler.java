package com.jobviewer.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.db.objects.SurveyJson;
import com.jobviewer.db.objects.TimeSheet;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.User;

public class JobViewerDBHandler {

	public static void saveUserDetail(Context context, User userProfile) {
		deleteUserProfile(context);
		ContentValues values = new ContentValues();
		values.put(JobViewerProviderContract.User.FIRST_NAME,
				userProfile.getFirstname());
		values.put(JobViewerProviderContract.User.LAST_NAME,
				userProfile.getLastname());
		values.put(JobViewerProviderContract.User.EMAIL, userProfile.getEmail());
		values.put(JobViewerProviderContract.User.USER_ID,
				userProfile.getUserid());
		context.getContentResolver().insert(
				JobViewerProviderContract.User.CONTENT_URI, values);
	}

	public static void deleteUserProfile(Context context) {
		context.getContentResolver().delete(
				JobViewerProviderContract.User.CONTENT_URI, null, null);
	}

	public static User getUserProfile(Context context) {
		Cursor cursor = context.getContentResolver().query(
				JobViewerProviderContract.User.CONTENT_URI, null, null, null,
				null);
		User userProfile = null;
		if (cursor != null && cursor.moveToFirst()) {
			userProfile = new User();
			userProfile.setLastname(cursor.getString(cursor
					.getColumnIndex(JobViewerProviderContract.User.LAST_NAME)));
			userProfile
					.setFirstname(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.User.FIRST_NAME)));
			userProfile.setEmail(cursor.getString(cursor
					.getColumnIndex(JobViewerProviderContract.User.EMAIL)));
			userProfile.setUserid(cursor.getString(cursor
					.getColumnIndex(JobViewerProviderContract.User.USER_ID)));

		}
		cursor.close();
		return userProfile;
	}

	public static void saveTimeSheet(Context context,
			TimeSheetRequest timeSheetRequest, String apiUrl) {
		ContentValues values = new ContentValues();
		values.put(JobViewerProviderContract.TimeSheet.STARTED_AT,
				timeSheetRequest.getStarted_at());
		values.put(JobViewerProviderContract.TimeSheet.RECORD_FOR,
				timeSheetRequest.getRecord_for());
		values.put(JobViewerProviderContract.TimeSheet.IS_INACTIVE,
				timeSheetRequest.getIs_inactive());
		values.put(JobViewerProviderContract.TimeSheet.IS_OVERRIDEN,
				timeSheetRequest.getIs_overriden());
		values.put(JobViewerProviderContract.TimeSheet.OVERRIDE_REASON,
				timeSheetRequest.getOverride_reason());
		values.put(JobViewerProviderContract.TimeSheet.OVERRIDE_COMMENT,
				timeSheetRequest.getOverride_comment());
		values.put(JobViewerProviderContract.TimeSheet.OVERRIDE_TIMESTAMP,
				timeSheetRequest.getOverride_timestamp());
		values.put(JobViewerProviderContract.TimeSheet.REFERENCE_ID,
				timeSheetRequest.getReference_id());
		values.put(JobViewerProviderContract.TimeSheet.USER_ID,
				timeSheetRequest.getUser_id());
		values.put(JobViewerProviderContract.TimeSheet.API_URL, apiUrl);

		context.getContentResolver().insert(
				JobViewerProviderContract.TimeSheet.CONTENT_URI, values);
	}

	public static void deleteTimeSheet(Context context, String startedAt) {
		String selectionClause = JobViewerProviderContract.TimeSheet.STARTED_AT
				+ " LIKE ?";
		String[] selectionArgs = { startedAt };
		context.getContentResolver().delete(
				JobViewerProviderContract.TimeSheet.CONTENT_URI,
				selectionClause, selectionArgs);
	}

	public static List<TimeSheet> getAllTimeSheet(Context context) {
		List<TimeSheet> timeSheetList = new ArrayList<TimeSheet>();

		Cursor cursor = context.getContentResolver().query(
				JobViewerProviderContract.TimeSheet.CONTENT_URI, null, null,
				null, null);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				TimeSheet timeSheet = new TimeSheet();
				timeSheet
						.setApi_url(cursor.getString(cursor
								.getColumnIndex(JobViewerProviderContract.TimeSheet.API_URL)));
				timeSheet.setTimeSheetRequest(new TimeSheetRequest());

				timeSheet
						.getTimeSheetRequest()
						.setStarted_at(
								cursor.getString(cursor
										.getColumnIndex(JobViewerProviderContract.TimeSheet.STARTED_AT)));

				timeSheet
						.getTimeSheetRequest()
						.setRecord_for(
								cursor.getString(cursor
										.getColumnIndex(JobViewerProviderContract.TimeSheet.RECORD_FOR)));

				timeSheet
						.getTimeSheetRequest()
						.setIs_inactive(
								cursor.getString(cursor
										.getColumnIndex(JobViewerProviderContract.TimeSheet.IS_INACTIVE)));

				timeSheet
						.getTimeSheetRequest()
						.setIs_overriden(
								cursor.getString(cursor
										.getColumnIndex(JobViewerProviderContract.TimeSheet.IS_OVERRIDEN)));

				timeSheet
						.getTimeSheetRequest()
						.setOverride_reason(
								cursor.getString(cursor
										.getColumnIndex(JobViewerProviderContract.TimeSheet.OVERRIDE_REASON)));

				timeSheet
						.getTimeSheetRequest()
						.setOverride_comment(
								cursor.getString(cursor
										.getColumnIndex(JobViewerProviderContract.TimeSheet.OVERRIDE_COMMENT)));
				timeSheet
						.getTimeSheetRequest()
						.setOverride_timestamp(
								cursor.getString(cursor
										.getColumnIndex(JobViewerProviderContract.TimeSheet.OVERRIDE_TIMESTAMP)));
				timeSheet
						.getTimeSheetRequest()
						.setReference_id(
								cursor.getString(cursor
										.getColumnIndex(JobViewerProviderContract.TimeSheet.REFERENCE_ID)));

				timeSheetList.add(timeSheet);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return timeSheetList;
	}

	public static void saveImage(Context context, ImageObject image) {
		ContentValues values = new ContentValues();
		values.put(JobViewerProviderContract.Image.IMAGE_ID, image.getImageId());
		values.put(JobViewerProviderContract.Image.IMAGE_STRING,
				image.getImage_string());
		values.put(JobViewerProviderContract.Image.IMAGE_URL,
				image.getImage_url());
		context.getContentResolver().insert(
				JobViewerProviderContract.Image.CONTENT_URI, values);
	}

	public static void deleteImageById(Context context, String imageId) {
		context.getContentResolver().delete(
				JobViewerProviderContract.Image.CONTENT_URI,
				JobViewerProviderContract.Image.IMAGE_ID + "=?",
				new String[] { imageId });
	}

	public static ImageObject getImageById(Context context, String id) {
		ImageObject imageObject = null;
		String selection = JobViewerProviderContract.Image.IMAGE_ID + "=" + "?";
		String[] selectionArgs = new String[] { id };
		Cursor cursor = context.getContentResolver().query(
				JobViewerProviderContract.Image.CONTENT_URI, null, selection,
				selectionArgs, null);

		if (cursor != null && cursor.moveToFirst()) {
			imageObject = new ImageObject();
			imageObject.setImageId(cursor.getString(cursor
					.getColumnIndex(JobViewerProviderContract.Image.IMAGE_ID)));
			imageObject
					.setImage_string(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.Image.IMAGE_STRING)));
			imageObject
					.setImage_url(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.Image.IMAGE_URL)));
		}
		cursor.close();
		return imageObject;
	}

	public static List<ImageObject> getAllSavedImages(Context context) {
		List<ImageObject> imageList = new ArrayList<ImageObject>();

		Cursor cursor = context.getContentResolver().query(
				JobViewerProviderContract.Image.CONTENT_URI, null, null, null,
				null);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				ImageObject imageObject = new ImageObject();
				imageObject
						.setImageId(cursor.getString(cursor
								.getColumnIndex(JobViewerProviderContract.Image.IMAGE_ID)));
				imageObject
						.setImage_string(cursor.getString(cursor
								.getColumnIndex(JobViewerProviderContract.Image.IMAGE_STRING)));
				imageObject
						.setImage_url(cursor.getString(cursor
								.getColumnIndex(JobViewerProviderContract.Image.IMAGE_URL)));
				imageList.add(imageObject);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return imageList;
	}

	public static void saveCheckOutRemember(Context context,
			CheckOutObject checkOutObject) {
		deleteCheckOutRemember(context);
		ContentValues values = new ContentValues();
		values.put(JobViewerProviderContract.CheckOutRemember.JOB_SELECTED,
				checkOutObject.getJobSelected());
		values.put(
				JobViewerProviderContract.CheckOutRemember.IS_CHECKED_OUT_SELECTED,
				checkOutObject.getIsChecekOutSelected());
		values.put(
				JobViewerProviderContract.CheckOutRemember.CLOCKIN_CHECKOUT_SELECTED_TEXT,
				checkOutObject.getClockInCheckOutSelectedText());
		values.put(
				JobViewerProviderContract.CheckOutRemember.VEHICLE_REGISTRATION,
				checkOutObject.getVehicleRegistration());
		values.put(JobViewerProviderContract.CheckOutRemember.MILAGE,
				checkOutObject.getMilage());
		values.put(
				JobViewerProviderContract.CheckOutRemember.REMEMBER_MY_SELECTION,
				checkOutObject.getRememberMySelection());
		values.put(JobViewerProviderContract.CheckOutRemember.JOB_STARTED_TIME,
				checkOutObject.getJobStartedTime());
		values.put(
				JobViewerProviderContract.CheckOutRemember.ASSESSMENT_SELECTED,
				checkOutObject.getAssessmentSelected());
		values.put(
				JobViewerProviderContract.CheckOutRemember.ASSESSMENT_REMEMBER_SELECTED,
				checkOutObject.getIsAssessmentRemember());
		values.put(
				JobViewerProviderContract.CheckOutRemember.IS_TRAVEL_STARTED,
				checkOutObject.getIsStartedTravel());
		values.put(JobViewerProviderContract.CheckOutRemember.IS_TRAVEL_END,
				checkOutObject.getIsTravelEnd());
		values.put(
				JobViewerProviderContract.CheckOutRemember.IS_POLLUTION_SELECTED,
				checkOutObject.getIsPollutionSelected());
		values.put(JobViewerProviderContract.CheckOutRemember.VISTEC_ID,
				checkOutObject.getVistecId());
		values.put(JobViewerProviderContract.CheckOutRemember.VISTEC_IMAGE_ID,
				checkOutObject.getVistectImageId());
		values.put(
				JobViewerProviderContract.CheckOutRemember.IS_ASSESSMENT_COMPLETED,
				checkOutObject.getIsAssessmentCompleted());
		context.getContentResolver().insert(
				JobViewerProviderContract.CheckOutRemember.CONTENT_URI, values);
	}

	public static void deleteCheckOutRemember(Context context) {
		context.getContentResolver().delete(
				JobViewerProviderContract.CheckOutRemember.CONTENT_URI, null,
				null);
	}

	public static CheckOutObject getCheckOutRemember(Context context) {
		Cursor cursor = context.getContentResolver().query(
				JobViewerProviderContract.CheckOutRemember.CONTENT_URI, null,
				null, null, null);
		CheckOutObject checkOutObject = null;
		if (cursor != null && cursor.moveToFirst()) {
			checkOutObject = new CheckOutObject();
			checkOutObject
					.setJobSelected(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.JOB_SELECTED)));
			checkOutObject
					.setIsChecekOutSelected(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.IS_CHECKED_OUT_SELECTED)));
			checkOutObject
					.setClockInCheckOutSelectedText(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.CLOCKIN_CHECKOUT_SELECTED_TEXT)));
			checkOutObject
					.setVehicleRegistration(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.VEHICLE_REGISTRATION)));
			checkOutObject
					.setMilage(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.MILAGE)));
			checkOutObject
					.setRememberMySelection(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.REMEMBER_MY_SELECTION)));
			checkOutObject
					.setJobStartedTime(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.JOB_STARTED_TIME)));
			checkOutObject
					.setAssessmentSelected(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.ASSESSMENT_SELECTED)));

			checkOutObject
					.setIsAssessmentRemember(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.ASSESSMENT_REMEMBER_SELECTED)));
			checkOutObject
					.setIsStartedTravel(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.IS_TRAVEL_STARTED)));
			checkOutObject
					.setIsTravelEnd(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.IS_TRAVEL_END)));
			checkOutObject
					.setIsPollutionSelected(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.IS_POLLUTION_SELECTED)));
			checkOutObject
					.setVistecId(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.VISTEC_ID)));
			checkOutObject
					.setVistectImageId(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.VISTEC_IMAGE_ID)));
			checkOutObject
					.setIsAssessmentCompleted(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.CheckOutRemember.IS_ASSESSMENT_COMPLETED)));
		}
		cursor.close();
		return checkOutObject;
	}

	public static void saveQuestionSet(Context context, SurveyJson surveyJson) {
		deleteQuestionSet(context);
		ContentValues values = new ContentValues();
		values.put(JobViewerProviderContract.QuestionSetTable.BACK_STACK,
				surveyJson.getBackStack());
		values.put(JobViewerProviderContract.QuestionSetTable.QUESTION_SET,
				surveyJson.getQuestionJson());
		values.put(JobViewerProviderContract.QuestionSetTable.WORK_TYPE,
				surveyJson.getWorkType());
		context.getContentResolver().insert(
				JobViewerProviderContract.QuestionSetTable.CONTENT_URI, values);
	}

	public static void deleteQuestionSet(Context context) {
		context.getContentResolver().delete(
				JobViewerProviderContract.QuestionSetTable.CONTENT_URI, null,
				null);
	}

	public static SurveyJson getQuestionSet(Context context) {
		Cursor cursor = context.getContentResolver().query(
				JobViewerProviderContract.QuestionSetTable.CONTENT_URI, null,
				null, null, null);
		SurveyJson surveyJson = null;
		if (cursor != null && cursor.moveToFirst()) {
			surveyJson = new SurveyJson();
			surveyJson
					.setBackStack(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.QuestionSetTable.BACK_STACK)));
			surveyJson
					.setQuestionJson(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.QuestionSetTable.QUESTION_SET)));
			surveyJson
					.setWorkType(cursor.getString(cursor
							.getColumnIndex(JobViewerProviderContract.QuestionSetTable.WORK_TYPE)));

		}
		cursor.close();
		return surveyJson;
	}

}
