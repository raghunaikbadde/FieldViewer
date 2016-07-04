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
import com.lanesgroup.jobviewer.ClockInConfirmationActivity;
import com.lanesgroup.jobviewer.R;

public class UpdateRiskOverTimeAlertDialog  extends Activity {

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
							}
						});;
		if(Utils.updateRiskAssementOverTimeAlerts == null){
			Utils.updateRiskAssementOverTimeAlerts = builder.create();
		}
		if(checkWhetherToShowAlert()){
			cancelTwelveHourOverTimeAlerts();
			if(!Utils.updateRiskAssementOverTimeAlerts.isShowing()){
				Utils.updateRiskAssementOverTimeAlerts.show();	
			} else{
				Utils.updateRiskAssementOverTimeAlerts.dismiss();
				Utils.updateRiskAssementOverTimeAlerts = builder.create();
				Utils.updateRiskAssementOverTimeAlerts.show();
			}
		} else{
			finish();
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
	private boolean checkWhetherToShowAlert(){
		long presetnMillis = System.currentTimeMillis();
		long riskAssessmentEndTime = 0;
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(this);
		try{
			
			riskAssessmentEndTime = Long.valueOf(breakShiftTravelCall.getRiskAssessmentEndTime());
		}catch(Exception e){
			riskAssessmentEndTime = 0;
		}
		
		
		long seconds = (presetnMillis-riskAssessmentEndTime)/1000;
		long minutes = seconds/60;
//		int hours = (int)(minutes/60);
		int thresHold = (int)(Utils.RISK_ASSMENET_OVETTIME_ALERT_TOGGLE/1000);
		return seconds >= thresHold;
	}
}
