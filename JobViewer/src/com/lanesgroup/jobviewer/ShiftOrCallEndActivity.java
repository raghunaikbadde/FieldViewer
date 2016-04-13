package com.lanesgroup.jobviewer;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.Utils;

public class ShiftOrCallEndActivity extends BaseActivity implements
		OnClickListener {

	Button mCloseButton, mGoOnCallButton;
	TextView mHeading;
	LinearLayout mHoursCalculationLayout,mShiftCompleteThankYouLayout;
	TextView mNumberOfBreaks,mShiftHours,mWorkHours,mNumberOfWork;
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
		mNumberOfBreaks = (TextView)findViewById(R.id.break_taken_numbers);
		mShiftHours = (TextView)findViewById(R.id.shift_hour_time);
		mWorkHours = (TextView)findViewById(R.id.hours_worked_time);
		mNumberOfWork = (TextView)findViewById(R.id.work_completed_numbers);
		
		
		
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
		
		else{
			
			try {
				BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler.getBreakShiftTravelCall(ShiftOrCallEndActivity.this);
				
				try{
					int numberOfBreaks = breakShiftTravelCall.getNoOfBreaks();
					mNumberOfBreaks.setText(String.valueOf(numberOfBreaks));
				}catch(Exception e){
					mNumberOfBreaks.setText("0");
				}
				
				long shiftStartTime = Long.valueOf(breakShiftTravelCall.getShiftStartTime());
				long shiftEndTime = Long.valueOf(breakShiftTravelCall.getShiftEndTime());
				long numberOFMillisecondsShift = shiftEndTime - shiftStartTime;
				if(numberOFMillisecondsShift > 0){
					String shiftHours = Utils.getTimeInHHMMFromNumberOfMillis(numberOFMillisecondsShift);
					mShiftHours.setText(shiftHours);
				}
				
				long workStartTime = Long.valueOf(breakShiftTravelCall.getWorkStartTime());
				long workEndTime = Long.valueOf(breakShiftTravelCall.getWorkEndTime());
				long numberOfWorkHoursinMillis = workEndTime - workStartTime;
				if(numberOfWorkHoursinMillis > 0){
					String workHours = Utils.getTimeInHHMMFromNumberOfMillis(numberOfWorkHoursinMillis);
					mWorkHours.setText(workHours);
				}
				
				mNumberOfWork.setText("1");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
			checkOutRemember.setJobStartedTime("");
			checkOutRemember.setVistecId("");
			JobViewerDBHandler.saveCheckOutRemember(v.getContext(),
					checkOutRemember);
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
		closeApplication();
	}
}
