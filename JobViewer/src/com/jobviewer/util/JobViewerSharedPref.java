package com.jobviewer.util;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class JobViewerSharedPref {
	public static final String PREF_NAME = "JobViewerPref";
	public static final String LONGITUDE = "longitude";
	public static final String LATITUDE = "latitude";

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
