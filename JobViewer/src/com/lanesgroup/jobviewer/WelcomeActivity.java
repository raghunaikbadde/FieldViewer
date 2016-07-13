package com.lanesgroup.jobviewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ShoutAboutSafetyObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.request.object.TimeSheetRequest;
import com.lanesgroup.jobviewer.fragment.ShoutOutActivity;

public class WelcomeActivity extends BaseActivity {
	private String selected;
	private Context context;
	private ImageView shout_about_image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_screen);
		if (getIntent().getBooleanExtra("Exit me", false)) {
			finish();
		} else {
			Utils.checkOutObject = new CheckOutObject();
			context = this;
			if (Utils.isInternetAvailable(context)) {
				GPSTracker tracker = new GPSTracker(context);
				tracker.getLocation();
			}
			shout_about_image = (ImageView) findViewById(R.id.shout_about_image);
			shout_about_image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					launchShoutAboutSafetyScreen(v);
				}
			});

			Button clockIn = (Button) findViewById(R.id.clock_in);

			clockIn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(WelcomeActivity.this,
							SelectClockInActivityDialog.class);
					startActivityForResult(intent,
							Constants.RESULT_CODE_WELCOME);
				}
			});

			Utils.startNotification(this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1003 && resultCode == RESULT_OK) {
			selected = data.getExtras().getString("Selected");

			if (Utils.checkOutObject == null) {
				Utils.checkOutObject = new CheckOutObject();
			}
			Intent intent;
			if (selected.equalsIgnoreCase("Shift")) {
				Utils.startShiftTimeRequest = new TimeSheetRequest();
				JobViewerDBHandler.saveTimeSheet(WelcomeActivity.this,
						Utils.startShiftTimeRequest,
						CommsConstant.START_SHIFT_API);
				intent = new Intent(WelcomeActivity.this, ClockInActivity.class);
				Utils.checkOutObject
						.setJobSelected(ActivityConstants.JOB_SELECTED_SHIFT);
				JobViewerDBHandler.saveCheckOutRemember(WelcomeActivity.this,
						Utils.checkOutObject);
				intent.putExtra(Utils.SHIFT_START, Utils.SHIFT_START);
			} else {
				intent = new Intent(WelcomeActivity.this,
						ClockInConfirmationActivity.class);
				Utils.callStartTimeRequest = new TimeSheetRequest();
				intent.putExtra(Utils.CALLING_ACTIVITY, WelcomeActivity.this
						.getClass().getSimpleName());
				Utils.checkOutObject
						.setJobSelected(ActivityConstants.JOB_SELECTED_ON_CALL);
				JobViewerDBHandler.saveCheckOutRemember(WelcomeActivity.this,
						Utils.checkOutObject);
				intent.putExtra(Utils.CALL_START, Utils.CALL_START);
			}
			startActivity(intent);

		}
	}

	

	private void launchShoutAboutSafetyScreen(View v) {
		ShoutAboutSafetyObject shoutAboutSafety = JobViewerDBHandler
				.getShoutAboutSafety(v.getContext());
		Intent intent = new Intent();
		if (shoutAboutSafety != null
				&& !Utils.isNullOrEmpty(shoutAboutSafety.getQuestionSet())) {
			intent.setClass(v.getContext(), ShoutOutActivity.class);
			intent.putExtra(ActivityConstants.SHOUT_OPTION,
					shoutAboutSafety.getOptionSelected());
			intent.putExtra(ActivityConstants.IS_SHOUT_SAVED,
					ActivityConstants.TRUE);
			startActivity(intent);
		} else {
			intent.setClass(v.getContext(), ShoutOptionsActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onBackPressed() {
		closeApplication();
	}
}
