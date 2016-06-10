package com.lanesgroup.jobviewer;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;

import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.TimeSheet;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSDialog;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;

public class LauncherActivity extends BaseActivity {
	Intent launcherIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(!Utils.isGPSEnabled(this)){
			showGPSDialog();return;
		}else{
			//bug no 12: fix
			//Implement the GPS check on app startup and to store the co-ordinates. If GPS is turned off/unavailable after the app starts up, 
			//the GPS co-ordinates obtained at the startup should be sent to the server for images or for work location as appropriate. 
			//This is similar to the fix provided in Vehicle Check application
			GPSTracker gpsTracker = new GPSTracker(this);
			gpsTracker.getLatitude();
			gpsTracker.getLongitude();
		}
		
		if(isBreakStartedShown()){
			return;
		}
		
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(this);
		if (checkOutRemember != null
				&& !Utils.isNullOrEmpty(checkOutRemember
						.getIsAssessmentCompleted())) {
			Utils.checkOutObject = checkOutRemember;
			launcherIntent = new Intent(this, PollutionActivity.class);
		} else if (checkOutRemember != null
				&& !Utils.isNullOrEmpty(checkOutRemember.getVistecId())) {
			Utils.checkOutObject = checkOutRemember;
			launcherIntent = new Intent(this, ActivityPageActivity.class);
		} else if (checkOutRemember != null
				&& ActivityConstants.TRUE.equalsIgnoreCase(checkOutRemember
						.getIsStartedTravel())) {
			launcherIntent = new Intent(this, EndTravelActivity.class);
			launcherIntent
					.putExtra(Constants.STARTED, Constants.TRAVEL_STARTED);		
		} else if (checkOutRemember != null
				&& ActivityConstants.TRUE.equalsIgnoreCase(checkOutRemember
						.getIsTravelEnd())) {
			Utils.checkOutObject = checkOutRemember;
			launcherIntent = new Intent(this, ActivityPageActivity.class);
		} else if (checkOutRemember != null
				&& !Utils.isNullOrEmpty(checkOutRemember.getJobSelected())) {
			Utils.checkOutObject = checkOutRemember;
			launcherIntent = new Intent(this, ActivityPageActivity.class);
		} else {
			launcherIntent = new Intent(this, WelcomeActivity.class);
		}
		launcherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(launcherIntent);
	}

	private boolean isBreakStartedShown() {
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(this);
		try{
			if(breakShiftTravelCall.isBreakStarted().equalsIgnoreCase(Constants.YES_CONSTANT)){
				launcherIntent = new Intent(this, EndBreakActivity.class);
				launcherIntent.putExtra(Constants.TIME, breakShiftTravelCall.getBreakStartedTime());
				launcherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(launcherIntent);
				return true;
			} else {
				return false;
			}
		}catch(Exception e){
			return false;
			
		}
	}
	
	private void showGPSDialog(){
		new GPSDialog(this).show();;
		
	}
}
