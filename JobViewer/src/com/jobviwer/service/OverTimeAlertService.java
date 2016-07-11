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
        if (Utils.isNullOrEmpty(jsonFlagObject)) {
            jsonFlagObject = "{}";
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonFlagObject);
            if (jsonObject.has(Constants.ALARM_OVERTIME_COUNT)) {
                repeating_alram_count = jsonObject
                        .getInt(Constants.ALARM_OVERTIME_COUNT);
                jsonObject.remove(Constants.ALARM_OVERTIME_COUNT);
                repeating_alram_count = repeating_alram_count + 1;
                jsonObject.put(Constants.ALARM_OVERTIME_COUNT,
                        repeating_alram_count);
                JobViewerDBHandler.saveFlaginJSONObject(context,
                        jsonObject.toString());

            } else {
                jsonObject.put(Constants.ALARM_OVERTIME_COUNT,
                        repeating_alram_count);
                JobViewerDBHandler.saveFlaginJSONObject(context,
                        jsonObject.toString());
            }
            Intent i = new Intent(context, AlertDialogActivity.class);
            i.putExtra(Constants.ALARM_OVERTIME_NUMBER_OF_HOURS,
                    repeating_alram_count);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } catch (JSONException JSOE) {
            JSOE.printStackTrace();
        }

    }


}
