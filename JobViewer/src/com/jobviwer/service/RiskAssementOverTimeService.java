package com.jobviwer.service;

import org.json.JSONObject;

import com.google.android.gms.nearby.bootstrap.request.StartScanRequest;
import com.google.android.gms.nearby.connection.Connections.StartAdvertisingResult;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.provider.JobViewerProviderContract.FlagJSON;
import com.jobviewer.util.Constants;
import com.jobviewer.util.Utils;
import com.raghu.AlertDialogActivity;
import com.raghu.UpdateRiskOverTimeAlertDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RiskAssementOverTimeService extends BroadcastReceiver{

	public static boolean isAppOnForgeground = false;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(Utils.LOG_TAG,"RiskAssementOverTimeService onReceive time = "+Utils.getCurrentDateAndTime());
		
		try{
			if(Utils.isMyApplicationRunningInForeGround(context)){
				isAppOnForgeground = true;
			}else{
				isAppOnForgeground = false;
			}
			String flagJSON = JobViewerDBHandler.getJSONFlagObject(context);
			JSONObject flagJsonObject = new JSONObject(flagJSON);
			
			String overrideFlag = "";
			
			if(flagJsonObject.has(Constants.OVERRIDE_TRAVEL_START_TIME_FLAG_JSON))
				overrideFlag = flagJsonObject.getString(Constants.OVERRIDE_TRAVEL_START_TIME_FLAG_JSON);
			
			if(Utils.isNullOrEmpty(overrideFlag)){
				overrideFlag = "0";				
			} else {
				int overrideFlagCount = Integer.valueOf(overrideFlag);
				overrideFlagCount++;
				overrideFlag = String.valueOf(overrideFlagCount);
				Intent i = new Intent(context, UpdateRiskOverTimeAlertDialog.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
			}
			flagJsonObject.put(Constants.OVERRIDE_TRAVEL_START_TIME_FLAG_JSON, overrideFlag);
			JobViewerDBHandler.saveFlaginJSONObject(context, flagJsonObject.toString());
		}catch(Exception e){
			Log.d(Utils.LOG_TAG,"RiskAssementOverTimeService error "+e.toString());
		}
		
		
		
	}

}
