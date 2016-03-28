package com.lanesgroup.jobviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;

public class ClockInActivity extends BaseActivity implements OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressSteps, mUserEmail, user_group_text;
	private CheckBox mRememberSelection;
	private Button mBack, mNext;
	private RadioGroup radioGroup1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_out_vehicle_screen1);
		initUI();
		updateDetails();
	}

	private void updateDetails() {
		User userProfile = JobViewerDBHandler.getUserProfile(this);
		String email = userProfile.getEmail();
		String userName = email.substring(0, email.indexOf("@"));
		mUserEmail.setText(userName);
		user_group_text.setText("@"
				+ getResources().getString(R.string.tmp_user_group_str));
		radioGroup1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (group.getCheckedRadioButtonId() == R.id.radio_yes) {
					Utils.checkOutObject
							.setClockInCheckOutSelectedText(ActivityConstants.YES);
					mProgressSteps.setText(Utils.PROGRESS_1_TO_3);
				} else {
					Utils.checkOutObject
							.setClockInCheckOutSelectedText(ActivityConstants.NO);
					mProgressSteps.setText(Utils.PROGRESS_1_TO_2);
				}

				enableNextClick();

			}
		});
		mRememberSelection
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							Utils.checkOutObject.setIsChecekOutSelected("True");
						} else {
							Utils.checkOutObject.setIsChecekOutSelected("");
						}

					}
				});
	}

	private void initUI() {
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mProgress.setMax(3);
		mProgress.setProgress(1);
		mProgressSteps = (TextView) findViewById(R.id.progress_step_text);
		mProgressSteps.setText(Utils.PROGRESS_1_TO_3);
		mUserEmail = (TextView) findViewById(R.id.user_email_text);
		mRememberSelection = (CheckBox) findViewById(R.id.checkBox3);
		mBack = (Button) findViewById(R.id.back);
		mBack.setOnClickListener(this);
		mNext = (Button) findViewById(R.id.next);
		user_group_text = (TextView) findViewById(R.id.user_group_text);
		radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);

	}

	private void enableNextClick() {
		mNext.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
				R.drawable.red_background, null));
		mNext.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		Intent intent;
		if (view == mBack) {
			finish();
		} else if (view == mNext) {
			if (ActivityConstants.YES.equalsIgnoreCase(Utils.checkOutObject
					.getClockInCheckOutSelectedText())) {
				intent = new Intent(ClockInActivity.this,
						CheckoutVehicleActivity.class);
				startActivity(intent);
			} else {
				intent = new Intent(ClockInActivity.this,
						ClockInConfirmationActivity.class);
				intent.putExtra(Utils.CALLING_ACTIVITY, ClockInActivity.this
						.getClass().getSimpleName());
				startActivity(intent);
			}
		}
	}
}
