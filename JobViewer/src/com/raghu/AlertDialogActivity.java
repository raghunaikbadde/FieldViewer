package com.raghu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;

import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.Constants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.R;

public class AlertDialogActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Bundle bundle = getIntent().getExtras();
		int numberOfHoursElapsedFromTweleveHours = 0;
		if(bundle!=null && bundle.containsKey(Constants.ALARM_OVERTIME_NUMBER_OF_HOURS)){
			numberOfHoursElapsedFromTweleveHours = bundle.getInt(Constants.ALARM_OVERTIME_NUMBER_OF_HOURS);
		}
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(this);
		long shiftStartTime = 0;
		try{
			
			shiftStartTime = Long.valueOf(breakShiftTravelCall.getShiftStartTime());
		}catch(Exception e){
			shiftStartTime = 0;
		}
		
		try{
			if(shiftStartTime == 0)
				shiftStartTime = Long.valueOf(breakShiftTravelCall.getCallStartTime());
		}catch(Exception e){
			e.printStackTrace();
		}
		long presetnMillis = System.currentTimeMillis();
		long seconds = (presetnMillis-shiftStartTime)/1000;
		long minutes = seconds/60;
		int hours = (int)(minutes/60);  
		if(numberOfHoursElapsedFromTweleveHours==0){
			hours = 12;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String header = getResources().getString(R.string.alertOverTimeHeader);
		String message =
				  getResources().getString(R.string.alertOverTimeMsgOne) +
				 " "+hours +" "+
				 getResources().getString(R.string.alertOverTimeMsgTwo);
		builder.setTitle(header)
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								Utils.overTimeAlerts = null;
								finish();
							}
						});
		if(Utils.overTimeAlerts == null){
			Utils.overTimeAlerts = builder.create();
		}
		if(checkWhetherToShowAlert()){
			if(!Utils.overTimeAlerts.isShowing()){
				Utils.overTimeAlerts.show();	
			} else {
				Utils.overTimeAlerts.dismiss();
				Utils.overTimeAlerts = builder.create();
				Utils.overTimeAlerts.show();
			}
		} else{
			finish();
		}
		
	}
	private boolean checkWhetherToShowAlert(){
		long presetnMillis = System.currentTimeMillis();
		long shiftStartTime = 0;
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(this);
		try{
			
			shiftStartTime = Long.valueOf(breakShiftTravelCall.getShiftStartTime());
		}catch(Exception e){
			shiftStartTime = 0;
		}
		
		try{
			if(shiftStartTime == 0)
				shiftStartTime = Long.valueOf(breakShiftTravelCall.getCallStartTime());
		}catch(Exception e){
			e.printStackTrace();
		}
		
		long seconds = (presetnMillis-shiftStartTime)/1000;
		long minutes = seconds/60;
//		int hours = (int)(minutes/60);
		int thresHold = (int)(Utils.OVETTIME_ALERT_TOGGLE/1000);
		return seconds >= thresHold;
	}
}