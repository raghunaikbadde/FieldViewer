package com.lanesgroup.jobviewer;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShiftOrCallEndActivity extends BaseActivity implements
		OnClickListener {

	Button mCloseButton, mGoOnCallButton;
	TextView mHeading;
	LinearLayout mHoursCalculationLayout,mShiftCompleteThankYouLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shift_complete_screen);

		initUI();
		updateDetailsOnUI();
	}

	private void initUI() {
		mCloseButton = (Button) findViewById(R.id.button1);
		mGoOnCallButton = (Button) findViewById(R.id.button2);
		mHeading = (TextView) findViewById(R.id.shift_complete_text);
		
		mHoursCalculationLayout  = (LinearLayout) findViewById(R.id.shiftHoursSummary);
		mShiftCompleteThankYouLayout  = (LinearLayout) findViewById(R.id.shiftCompleteThankYouLayout);
		mCloseButton.setOnClickListener(this);
		mGoOnCallButton.setOnClickListener(this);
	}
	
	private void updateDetailsOnUI(){
		CheckOutObject checkOutRemember = JobViewerDBHandler.getCheckOutRemember(this);
		if(!checkOutRemember.getJobSelected().contains("shift")){
			mHoursCalculationLayout.setVisibility(View.GONE);
			mHeading.setText(getResources().getString(R.string.call_complete_str));
			mShiftCompleteThankYouLayout.setVisibility(View.GONE);
		}
		//else{
		//calculate hours and update on UI
		//}
	}
	

	@Override
	public void onClick(View v) {
		if (v == mCloseButton) {
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(v.getContext());
			checkOutRemember.setJobSelected("");
			checkOutRemember.setIsStartedTravel("");
			checkOutRemember.setIsTravelEnd("");
			checkOutRemember.setIsAssessmentCompleted("");
			JobViewerDBHandler.saveCheckOutRemember(v.getContext(),
					checkOutRemember);
			finish();
			onBackPressed();
			
		} else if (v == mGoOnCallButton) {
			/*
			 * Intent intent = new
			 * Intent(ShiftOrCallEndActivity.this,ClockInConfirmationActivity
			 * .class); startActivity(intent);
			 */

		}
	}

	@Override
	public void onBackPressed() {
		exitApplication(ShiftOrCallEndActivity.this);
	}
}
