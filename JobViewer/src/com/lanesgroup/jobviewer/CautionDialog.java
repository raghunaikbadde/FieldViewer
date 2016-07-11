package com.lanesgroup.jobviewer;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.JobViewerSharedPref;
import com.jobviewer.util.Utils;

/**
 * Created by system-local on 29-06-2016.
 */
public class CautionDialog extends Activity {
    private Context mContext;
    private Button btn_next;
    private String abstractionValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);
        setContentView(R.layout.caution_dialog);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.KEY_ABSTRACTION_VALUE)) {
            abstractionValue = intent.getStringExtra(Constants.KEY_ABSTRACTION_VALUE);
        }
        Log.d("TAG", "abstractionValue" + abstractionValue);
        mContext = this;
        initView();
    }

    private void initView() {
        btn_next = (Button) findViewById(R.id.btn_next);
        setClickListener();
    }

    private void setClickListener() {
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                CheckOutObject checkOutRemember = JobViewerDBHandler
                        .getCheckOutRemember(v.getContext());
                if (checkOutRemember != null
                        && ActivityConstants.TRUE
                        .equalsIgnoreCase(checkOutRemember
                                .getIsTravelEnd())) {
                    if (shouldNewWorkTravelArrivedSite(mContext)) {
                        intent.setClass(mContext,
                                TravelToWorkSiteOrArriveAtSiteActivity.class);
                    } else {
                        intent.setClass(mContext,
                                NewWorkActivity.class);
                    }
                } else {
                    intent.setClass(mContext,
                            TravelToWorkSiteActivity.class);
                }
                JobViewerSharedPref jobViewerSharedPref = new JobViewerSharedPref();
                jobViewerSharedPref.saveAbstractionValue(mContext, abstractionValue);
                jobViewerSharedPref.saveFromWork(mContext, true);
                intent.putExtra(Constants.KEY_ABSTRACTION_VALUE, abstractionValue);
                intent.putExtra(Constants.KEY_FROM_WORK, true);

//                    result = WORK;
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean shouldNewWorkTravelArrivedSite(Context mContext) {
        String str = JobViewerDBHandler.getJSONFlagObject(mContext);
        if (Utils.isNullOrEmpty(str)) {
            str = "{}";
        }
        try {
            JSONObject jsonObject = new JSONObject(str);
            if (jsonObject.has(Constants.FLAG_NEW_WORK_TRAVEL_ARRIVED_SITE)) {
                return jsonObject.getBoolean(Constants.FLAG_NEW_WORK_TRAVEL_ARRIVED_SITE);
            }


            String jsonString = jsonObject.toString();
            JobViewerDBHandler.saveFlaginJSONObject(getApplicationContext(),
                    jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}