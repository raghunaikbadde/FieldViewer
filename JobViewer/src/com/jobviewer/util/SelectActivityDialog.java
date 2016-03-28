package com.jobviewer.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.lanesgroup.jobviewer.MainActivity;
import com.lanesgroup.jobviewer.NewWorkActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.TravelToWorkSiteActivity;
import com.lanesgroup.jobviewer.fragment.QuestionsActivity;

public class SelectActivityDialog extends Activity {

	private CheckBox mWork, mWorkNoPhotos, mTraining;
	private String selected;
	private OnCheckedChangeListener checkChangedListner;
	private Button start, cancel;

	private final String WORK = "Work";
	private final String WORK_NO_PHOTOS = "WorkNoPhotos";
	private final String TRAINING = "Training";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		setContentView(R.layout.dialog_box2);

		mWork = (CheckBox) findViewById(R.id.checkBox1);
		mWorkNoPhotos = (CheckBox) findViewById(R.id.checkBox2);
		mTraining = (CheckBox) findViewById(R.id.checkBox3);

		start = (Button) findViewById(R.id.dialog_ok);
		cancel = (Button) findViewById(R.id.dialog_cancel);

		checkChangedListner = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (buttonView == mWork && isChecked) {
					mTraining.setChecked(false);
					mWorkNoPhotos.setChecked(false);
					start.setClickable(true);
					enableStartButton();
					selected = WORK;
				} else if (buttonView == mWorkNoPhotos && isChecked) {
					mWork.setChecked(false);
					mTraining.setChecked(false);
					start.setClickable(true);
					enableStartButton();
					selected = WORK_NO_PHOTOS;
				} else if (buttonView == mTraining && isChecked) {
					mWork.setChecked(false);
					mWorkNoPhotos.setChecked(false);
					start.setClickable(true);
					enableStartButton();
					selected = TRAINING;
				} else {
					start.setClickable(false);
				}
			}
		};

		mWork.setOnCheckedChangeListener(checkChangedListner);
		mWorkNoPhotos.setOnCheckedChangeListener(checkChangedListner);
		mTraining.setOnCheckedChangeListener(checkChangedListner);

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void enableStartButton() {
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent;
				if (selected.equalsIgnoreCase(WORK)) {
					CheckOutObject checkOutRemember = JobViewerDBHandler
							.getCheckOutRemember(v.getContext());
					if (checkOutRemember != null
							&& ActivityConstants.TRUE
									.equalsIgnoreCase(checkOutRemember
											.getIsTravelEnd())) {
						intent = new Intent(SelectActivityDialog.this,
								NewWorkActivity.class);
					} else {
						intent = new Intent(SelectActivityDialog.this,
								TravelToWorkSiteActivity.class);
					}
					startActivity(intent);

				} else if (selected.equalsIgnoreCase(WORK_NO_PHOTOS)) {
					intent = new Intent(SelectActivityDialog.this,
							MainActivity.class);
					startActivity(intent);
				} else {
					// intent = new Intent(ActivityPageActivity.this,
					// ClockInConfirmationActivity.class);
					// intent.putExtra(Constants.CALLING_ACTIVITY,
					// ActivityPageActivity.this.getClass().getSimpleName());
				}
				finish();
			}
		});
	}
}
