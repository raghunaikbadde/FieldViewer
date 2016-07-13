package com.lanesgroup.jobviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.Utils;
import com.jobviwer.service.RiskAssementOverTimeService;
import com.lanesgroup.jobviewer.R;

public class UpdateRiskOverTimeAlertDialog extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Bundle bundle = getIntent().getExtras();
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(this);
		long riskAssesmentEndtTime = 0;
		try{
			riskAssesmentEndtTime = Long.valueOf(breakShiftTravelCall.getRiskAssessmentEndTime());
		}catch(Exception e){
			riskAssesmentEndtTime = 0;
		}
		
		long presetnMillis = System.currentTimeMillis();
		long seconds = (presetnMillis-riskAssesmentEndtTime)/1000;
		long minutes = (long)seconds/60;
		int hours = (int)(minutes/60);  
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String header = getResources().getString(R.string.alertUpdateRiskAssementHeader);
		String message =
				  getResources().getString(R.string.alertUpdateRiskAssementMsg1) +
				 " "+hours +" "+
				 getResources().getString(R.string.alertUpdateRiskAssementMsg2);
		builder.setTitle(header)
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								Utils.updateRiskAssementOverTimeAlerts = null;
								finish();
							}
						})
				.setNegativeButton("Update",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								
								dialog.cancel();
								Utils.updateRiskAssementOverTimeAlerts = null;
								
								finish();
								
								if(!RiskAssementOverTimeService.isAppOnForgeground){
									Log.d(Utils.LOG_TAG,"applciation runnig in background");
									Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.lanesgroup.jobviewer");
									startActivity(launchIntent);
								}else{
									Log.d(Utils.LOG_TAG,"applciation runnig in foreground");
								}
							}
						});;
		if(Utils.updateRiskAssementOverTimeAlerts == null){
			Utils.updateRiskAssementOverTimeAlerts = builder.create();
		}
		cancelTwelveHourOverTimeAlerts();
		if(!Utils.updateRiskAssementOverTimeAlerts.isShowing()){
			Utils.updateRiskAssementOverTimeAlerts.show();	
		} else{
			Utils.updateRiskAssementOverTimeAlerts.dismiss();
			Utils.updateRiskAssementOverTimeAlerts = builder.create();
			Utils.updateRiskAssementOverTimeAlerts.show();
		}
		
	}
	private void cancelTwelveHourOverTimeAlerts() {
		try{
			if(Utils.overTimeAlerts != null ){
				if(Utils.overTimeAlerts.isShowing()){
					Utils.overTimeAlerts.dismiss();
				}
			}
			ClockInConfirmationActivity.cancelAlarm();
		}catch(Exception e){
			
		}
	}

}
