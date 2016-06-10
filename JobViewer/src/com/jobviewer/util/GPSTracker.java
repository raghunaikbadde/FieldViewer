package com.jobviewer.util;

import java.util.HashMap;

import org.json.JSONObject;

import com.jobviewer.provider.JobViewerDBHandler;
import com.lanesgroup.jobviewer.EndTravelActivity;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener {
	private final Context mContext;

	// flag for GPS status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	boolean canGetLocation = false;

	Location location; // location
	double latitude; // latitude
	double longitude; // longitude

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}

	public Location getLocation() {
		try {
			locationManager = (LocationManager) mContext
					.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS Enabled", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
								JobViewerSharedPref jobViewerSharedPref = new JobViewerSharedPref();
								jobViewerSharedPref.save(this,
										String.valueOf(latitude),
										String.valueOf(longitude));
							} else {
								JobViewerSharedPref jobViewerSharedPref = new JobViewerSharedPref();
								HashMap<String, String> values = jobViewerSharedPref
										.getValues(mContext);
								if (values != null
										&& !Utils
												.isNullOrEmpty(values
														.get(JobViewerSharedPref.LATITUDE))
										&& !Utils
												.isNullOrEmpty(values
														.get(JobViewerSharedPref.LONGITUDE))) {
									latitude = Double.parseDouble(values
											.get(JobViewerSharedPref.LATITUDE));
									longitude = Double
											.parseDouble(values
													.get(JobViewerSharedPref.LONGITUDE));
								}

							}
						}
					}
				}
			}

		} catch (Exception e) {
		}
		return location;
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app.
	 * */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}
		// return latitude
		//bug no 12: fix
		//Implement the GPS check on app startup and to store the co-ordinates. If GPS is turned off/unavailable after the app starts up, 
		//the GPS co-ordinates obtained at the startup should be sent to the server for images or for work location as appropriate. 
		//This is similar to the fix provided in Vehicle Check application
		if(latitude == 0.0){
			String lat = getMostRecentLat();
			if(!Utils.isNullOrEmpty(lat)){
				try {
					latitude = Double.valueOf(lat);
				} catch(Exception e) {
					
				}
			}
		} else {
			saveLattitude(String.valueOf(latitude));
		}
		return latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}
		//bug no 12: fix
		//Implement the GPS check on app startup and to store the co-ordinates. If GPS is turned off/unavailable after the app starts up, 
		//the GPS co-ordinates obtained at the startup should be sent to the server for images or for work location as appropriate. 
		//This is similar to the fix provided in Vehicle Check application
		if(longitude == 0.0){
			String lon = getMostRecentLon();
			if(!Utils.isNullOrEmpty(lon)){
				try {
					longitude = Double.valueOf(lon);
				} catch(Exception e) {
					
				}
			}
		} else {
			saveLongitude(String.valueOf(longitude));
		}

		// return longitude
		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * 
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog On pressing Settings button will
	 * lauch Settings Options
	 * */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting DialogHelp Title
		alertDialog.setTitle("GPS is settings");

		// Setting DialogHelp Message
		alertDialog
				.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						mContext.startActivity(intent);
					}
				});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		float bestAccuracy = -1f;
		if (location.getAccuracy() != 0.0f
				&& (location.getAccuracy() < bestAccuracy)
				|| bestAccuracy == -1f) {
			locationManager.removeUpdates(this);
		}
		bestAccuracy = location.getAccuracy();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public float getAccurecy() {
		return location.getAccuracy();
	}
	//bug no 12: fix
	//Implement the GPS check on app startup and to store the co-ordinates. If GPS is turned off/unavailable after the app starts up, 
	//the GPS co-ordinates obtained at the startup should be sent to the server for images or for work location as appropriate. 
	//This is similar to the fix provided in Vehicle Check application
	private void saveLattitude(String lat){
		String str = JobViewerDBHandler.getJSONFlagObject(mContext);
		if(Utils.isNullOrEmpty(str)){
			str = "{}";
		}
		try{
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.has(Constants.MOST_RECENT_SAVED_LAT)){
				jsonObject.remove(Constants.MOST_RECENT_SAVED_LAT);
			}
			
			jsonObject.put(Constants.MOST_RECENT_SAVED_LAT, lat);
			String jsonString = jsonObject.toString();
			JobViewerDBHandler.saveFlaginJSONObject(getApplicationContext(), jsonString);
		}catch(Exception e){
			
		}
	}
	
	private void saveLongitude(String lon){
		String str = JobViewerDBHandler.getJSONFlagObject(mContext);
		if(Utils.isNullOrEmpty(str)){
			str = "{}";
		}
		try{
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.has(Constants.MOST_RECENT_SAVED_LON)){
				jsonObject.remove(Constants.MOST_RECENT_SAVED_LON);
			}
			
			jsonObject.put(Constants.MOST_RECENT_SAVED_LON, lon);
			String jsonString = jsonObject.toString();
			JobViewerDBHandler.saveFlaginJSONObject(getApplicationContext(), jsonString);
		}catch(Exception e){
			
		}
	}
	
	private String getMostRecentLat(){
		String jsonStr = JobViewerDBHandler.getJSONFlagObject(mContext);
		String mostRecentLat =  "0.0";
		if(Utils.isNullOrEmpty(jsonStr)){
			jsonStr = "{}";
		}
		try{
			JSONObject flagJSON = new JSONObject(jsonStr);
			if(flagJSON.has(Constants.MOST_RECENT_SAVED_LAT)){
				mostRecentLat = flagJSON.getString(Constants.MOST_RECENT_SAVED_LAT);
			}
		}catch(Exception e){
			
		}
		return mostRecentLat;
	}
	
	private String getMostRecentLon(){
		String jsonStr = JobViewerDBHandler.getJSONFlagObject(mContext);
		String mostRecentLon =  "0.0";
		if(Utils.isNullOrEmpty(jsonStr)){
			jsonStr = "{}";
		}
		try{
			JSONObject flagJSON = new JSONObject(jsonStr);
			if(flagJSON.has(Constants.MOST_RECENT_SAVED_LON)){
				mostRecentLon = flagJSON.getString(Constants.MOST_RECENT_SAVED_LON);
			}
		}catch(Exception e){
			
		}
		return mostRecentLon;
	}
	
}
