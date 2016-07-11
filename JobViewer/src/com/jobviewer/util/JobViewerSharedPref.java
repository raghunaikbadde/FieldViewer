package com.jobviewer.util;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class JobViewerSharedPref {
	public static final String PREF_NAME = "JobViewerPref";
	public static final String LONGITUDE = "longitude";
	public static final String LATITUDE = "latitude";
	public static final String WATER_ABSTRACTION_VALUE = "water_abstraction_value";
	public static final String KEY_FROM_WORK = "key_from_work";
	public static final String KEY_WORK_ID = "key_work_id";
	public static final String KEY_WORK_NO_PHOTOS_DATA = "key_work_no_photos_data";
	public static final String KEY_REGISTRATION_ARRAY = "key_registration_array ";

	public JobViewerSharedPref() {
		super();
	}

	public void save(Context context, String latitude, String longitude) {
		clearSharedPreference(context);
		SharedPreferences pref;
		Editor editor;

		pref = getSharedPref(context);
		editor = pref.edit();
		editor.putString(LATITUDE, latitude);
		editor.putString(LONGITUDE, longitude);
		editor.commit();
	}

	public void saveAbstractionValue(Context context, String abstraction) {
		clearSharedPreference(context);
		SharedPreferences pref;
		Editor editor;

		pref = getSharedPref(context);
		editor = pref.edit();
		editor.putString(WATER_ABSTRACTION_VALUE, abstraction);
		editor.commit();
	}
	public void saveRegistrationArrayString(Context context, String registration) {
		clearSharedPreference(context);
		SharedPreferences pref;
		Editor editor;
		
		pref = getSharedPref(context);
		editor = pref.edit();
		editor.putString(KEY_REGISTRATION_ARRAY, registration);
		editor.commit();
	}

	public void saveFromWork(Context context, boolean fromWork) {
		clearSharedPreference(context);
		SharedPreferences pref;
		Editor editor;
		pref = getSharedPref(context);
		editor = pref.edit();
		editor.putBoolean(KEY_FROM_WORK, fromWork);
		editor.commit();
	}

	public void saveWorkId(Context context, String workId) {
		clearSharedPreference(context);
		SharedPreferences pref;
		Editor editor;
		pref = getSharedPref(context);
		editor = pref.edit();
		editor.putString(KEY_WORK_ID, workId);
		editor.commit();
	}

	public void fromNoPhotsWork(Context context, boolean isTrue) {
		clearSharedPreference(context);
		SharedPreferences pref;
		Editor editor;
		pref = getSharedPref(context);
		editor = pref.edit();
		editor.putBoolean(KEY_WORK_NO_PHOTOS_DATA, isTrue);
		editor.commit();
	}

	public void removeValues(Context context) {
		SharedPreferences pref;
		Editor editor;

		pref = getSharedPref(context);
		editor = pref.edit();

		editor.remove(LONGITUDE);
		editor.remove(LATITUDE);
		editor.commit();
	}

	public void clearSharedPreference(Context context) {
		SharedPreferences pref;
		Editor editor;

		pref = getSharedPref(context);
		editor = pref.edit();

		editor.clear();
		editor.commit();
	}

	public HashMap<String, String> getValues(Context context) {
		HashMap<String, String> values = new HashMap<String, String>();
		SharedPreferences pref;
		pref = getSharedPref(context);
		values.put(LATITUDE, pref.getString(LATITUDE, null));
		values.put(LONGITUDE, pref.getString(LONGITUDE, null));
		return values;

	}

	public SharedPreferences getSharedPref(Context context) {
		return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}

}
