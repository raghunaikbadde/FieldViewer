package com.lanesgroup.jobviewer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.fitness.request.StartBleScanRequest;
import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.Utils;

public class WorkSuccessActivity extends BaseActivity implements
		OnClickListener {

	private Button mDoneButton;
	private TextView mVistecText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.success_screen);
		initUI();
	}

	private void initUI() {
		mDoneButton = (Button) findViewById(R.id.done_button);
		mVistecText = (TextView) findViewById(R.id.vistec_number_text);
		mDoneButton.setOnClickListener(this);
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(this);
		mVistecText.setText(checkOutRemember.getVistecId());
		
		//Logic for counting the number of works completed in shift or call and storing in DB/
		//It is useful for showing at the shift end screen
		BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler
				.getBreakShiftTravelCall(this);
		String noOfWorksCompleted = breakShiftTravelCall
				.getNoOfWorksCompleted();
		int counterForNoOfWorks = 0;
		try {
			counterForNoOfWorks = Integer.valueOf(noOfWorksCompleted);
		} catch (NumberFormatException nfe) {
			counterForNoOfWorks = 0;
			Log.d(Utils.LOG_TAG,
					"Number format Exception while converting the numberOfWorks in db to integer. msg:"
							+ nfe.toString());
		} catch(Exception e){
			Log.d(Utils.LOG_TAG,
					"generic Exception while converting the  the numberOfWorks in db to integer. msg:"
							+ e.toString());	
		}
		counterForNoOfWorks = counterForNoOfWorks+1;
		noOfWorksCompleted = String.valueOf(counterForNoOfWorks);
		breakShiftTravelCall.setNoOfWorksCompleted(noOfWorksCompleted);
		JobViewerDBHandler.saveBreakShiftTravelCall(this, breakShiftTravelCall);
		//End of logic for the calcualting the number of works completed
		
		JobViewerDBHandler
				.deleteAllAddCardSavedImages(WorkSuccessActivity.this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == mDoneButton.getId()) {
			Intent appPageActivityIntent = new Intent(WorkSuccessActivity.this,
					ActivityPageActivity.class);
			ClockInConfirmationActivity.initiateAlarm();
			ClockInConfirmationActivity.setAlarmForOverTime();
			JobViewerDBHandler.deleteQuestionSet(WorkSuccessActivity.this);
			finish();
			startActivity(appPageActivityIntent);
		}
	}

	@Override
	public void onBackPressed() {
		Intent appPageActivityIntent = new Intent(WorkSuccessActivity.this,
				ActivityPageActivity.class);
		ClockInConfirmationActivity.initiateAlarm();
		ClockInConfirmationActivity.setAlarmForOverTime();
		JobViewerDBHandler.deleteQuestionSet(WorkSuccessActivity.this);
		finish();
		startActivity(appPageActivityIntent);
	}
}
