package com.lanesgroup.jobviewer;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.JobViewerSharedPref;
import com.jobviewer.util.Utils;

/**
 * Created by system-local on 29-06-2016.
 */
public class WaterAbstractionActivity extends BaseActivity {

	private Context mContext;
	private RadioButton radio_yes;
	private RadioButton radio_no;
	private Button mNext;
	private Button mCancel;
	private RadioGroup radioGroup1;
	private String selectedFlag = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.water_abstraction_layout);
		mContext = this;
		initView();
	}

	private void initView() {
		radio_yes = (RadioButton) findViewById(R.id.radio_yes);
		radio_no = (RadioButton) findViewById(R.id.radio_no);
		mNext = (Button) findViewById(R.id.button2);
		mCancel = (Button) findViewById(R.id.button1);
		radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
		setClickListener();
		enableDisableNextButton(false);
	}

	private void setClickListener() {
		radioGroup1
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == R.id.radio_yes) {
							selectedFlag = Constants.YES_CONSTANT;
						} else {
							selectedFlag = Constants.NO_CONSTANT;
						}
						enableDisableNextButton(true);
					}
				});

		mNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectedFlag.equals(Constants.YES_CONSTANT)) {
					Intent mCautionDialog = new Intent(mContext,
							CautionDialog.class);
					mCautionDialog.putExtra(Constants.KEY_ABSTRACTION_VALUE,
							selectedFlag);

					startActivity(mCautionDialog);
				} else {

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
	                jobViewerSharedPref.saveAbstractionValue(mContext, selectedFlag);
	                jobViewerSharedPref.saveFromWork(mContext, true);
	                intent.putExtra(Constants.KEY_ABSTRACTION_VALUE, selectedFlag);
	                intent.putExtra(Constants.KEY_FROM_WORK, true);

//	                    result = WORK;
	                startActivity(intent);
	                finish();
				}

			}
		});

		mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
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
	private void enableDisableNextButton(boolean isEnable) {
		if (isEnable) {
			mNext.setEnabled(true);
			mNext.setBackgroundResource(R.drawable.red_background);
		} else {
			mNext.setEnabled(false);
			mNext.setBackgroundResource(R.drawable.dark_grey_background);
		}
	}
}
