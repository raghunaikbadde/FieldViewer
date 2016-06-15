package com.jobviwer.service;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.Constants;
import com.jobviewer.util.Utils;
import com.raghu.AlertDialogActivity;

public class OverTimeAlertService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String jsonFlagObject = JobViewerDBHandler.getJSONFlagObject(context);
		int repeating_alram_count = 0;
		if(Utils.isNullOrEmpty(jsonFlagObject)){
			jsonFlagObject = "{}";
		}
			try{
				JSONObject jsonObject = new JSONObject(jsonFlagObject);
				if(jsonObject.has(Constants.ALARM_OVERTIME_COUNT)){
					repeating_alram_count = jsonObject.getInt(Constants.ALARM_OVERTIME_COUNT);
					jsonObject.remove(Constants.ALARM_OVERTIME_COUNT);
					repeating_alram_count = repeating_alram_count+1;
					jsonObject.put(Constants.ALARM_OVERTIME_COUNT, repeating_alram_count);
					JobViewerDBHandler.saveFlaginJSONObject(context, jsonObject.toString());					
					
				}  else {
					jsonObject.put(Constants.ALARM_OVERTIME_COUNT, repeating_alram_count);
					JobViewerDBHandler.saveFlaginJSONObject(context, jsonObject.toString());
				}
				Intent i = new Intent(context, AlertDialogActivity.class);
				i.putExtra(Constants.ALARM_OVERTIME_NUMBER_OF_HOURS, repeating_alram_count);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);					
			}catch(JSONException JSOE){
				
			}
		
		/*
		 * Dialog dialog = new Dialog(context); int hours = 12;
		 * //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		 * dialog.getWindow
		 * ().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		 * dialog.setCancelable(false);
		 * dialog.setTitle(context.getResources().getString
		 * (R.string.alertOverTimeHeader)); String message =
		 * context.getResources().getString(R.string.alertOverTimeMsgOne) +
		 * " "+hours +" "+
		 * context.getResources().getString(R.string.alertOverTimeMsgTwo);
		 * //dialog.setContentView(R.layout.confirm_dialog); LayoutInflater
		 * inflater = (LayoutInflater)
		 * context.getSystemService(context.LAYOUT_INFLATER_SERVICE); View
		 * layout = inflater.inflate(R.layout.confirm_dialog, null);
		 * dialog.setContentView(layout); TextView mHeader = (TextView)
		 * layout.findViewById(R.id.dialog_header);
		 * mHeader.setText(context.getResources
		 * ().getString(R.string.alertOverTimeHeader)); TextView mMessage =
		 * (TextView) layout.findViewById(R.id.cofirmation_msg_text);
		 * mMessage.setText(message); dialog.show();
		 */
	}

	/*
	 * @Override public IBinder onBind(Intent intent) { return null; }
	 * 
	 * @Override public int onStartCommand(Intent intent, int flags, int
	 * startId) { Dialog dialog = new Dialog(this); int hours = 12;
	 * //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	 * dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	 * dialog.setCancelable(false);
	 * dialog.setTitle(getResources().getString(R.string.alertOverTimeHeader));
	 * String message = getResources().getString(R.string.alertOverTimeMsgOne) +
	 * " "+hours +" "+ getResources().getString(R.string.alertOverTimeMsgTwo);
	 * //dialog.setContentView(R.layout.confirm_dialog); LayoutInflater inflater
	 * = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE); View layout
	 * = inflater.inflate(R.layout.confirm_dialog, null);
	 * dialog.setContentView(layout); TextView mHeader = (TextView)
	 * layout.findViewById(R.id.dialog_header);
	 * mHeader.setText(getResources().getString(R.string.alertOverTimeHeader));
	 * TextView mMessage = (TextView)
	 * layout.findViewById(R.id.cofirmation_msg_text);
	 * mMessage.setText(message); dialog.show(); return
	 * super.onStartCommand(intent, flags, startId); }
	 */

}
