package com.lanesgroup.jobviewer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.EditTextFocusListener;
import com.jobviewer.util.EditTextWatcher;

public class NewWorkActivity extends BaseActivity implements OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressStep;
	private CheckBox pollutionCheckBox;
	private EditText mDistrict1;
	private static EditText mDistrict2;
	private static EditText mTaskNumber1;
	private static EditText mTaskNumber2;
	private Button mCancel;
	private static Button mNext;
	static Context context;
	static int progress = 100 / 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_work_screen);
		context = this;
		initUI();
	}

	private void initUI() {
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mProgress.setProgress(progress);
		mProgressStep = (TextView) findViewById(R.id.progress_step_text);
		pollutionCheckBox = (CheckBox) findViewById(R.id.pollutionCheckBox);
		mDistrict1 = (EditText) findViewById(R.id.distric1_edittext);
		mDistrict2 = (EditText) findViewById(R.id.distric2_edittext);
		mTaskNumber1 = (EditText) findViewById(R.id.distric3_edittext);
		mTaskNumber2 = (EditText) findViewById(R.id.distric4_edittext);
		mCancel = (Button) findViewById(R.id.button1);
		mNext = (Button) findViewById(R.id.button2);
		mNext.setOnClickListener(this);
		mDistrict2.requestFocus();
		mDistrict2.setOnFocusChangeListener(new EditTextFocusListener(this,
				mDistrict2, 2));
		mTaskNumber1.setOnFocusChangeListener(new EditTextFocusListener(this,
				mTaskNumber1, 2));
		mTaskNumber2.setOnFocusChangeListener(new EditTextFocusListener(this,
				mTaskNumber2, 4));

		mDistrict2.addTextChangedListener(new EditTextWatcher(this, mDistrict2,
				2, mTaskNumber1));
		mTaskNumber1.addTextChangedListener(new EditTextWatcher(this,
				mTaskNumber1, 2, mTaskNumber2));
		mTaskNumber2.addTextChangedListener(new EditTextWatcher(this,
				mTaskNumber2, 4, null));
		pollutionCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							mProgressStep
									.setText(buttonView
											.getContext()
											.getResources()
											.getString(
													R.string.progress_step_pollution));
							progress=100/6;
							mProgress.setProgress(progress);
							
						} else {
							mProgressStep.setText(buttonView.getContext()
									.getResources()
									.getString(R.string.progress_step));
							progress=100/5;
							mProgress.setProgress(progress);
						}

					}
				});
	}

	public static void enableNextButton() {
		int mDistrict2Text = mDistrict2.getText().toString().length();
		int mTaskNumber1Text = mTaskNumber1.getText().toString().length();
		int mTaskNumber2Text = mTaskNumber2.getText().toString().length();
		if (mDistrict2Text == 2 && mTaskNumber1Text == 2
				&& mTaskNumber2Text == 4) {
			mNext.setEnabled(true);
			mNext.setBackgroundResource(R.drawable.red_background);
		} else {
			mNext.setEnabled(false);
			mNext.setBackgroundResource(R.drawable.dark_grey_background);
		}
	}

	@Override
	public void onClick(View view) {
		if (view == mCancel) {

		} else if (view == mNext) {
			boolean isValidUserInput = isValidUserInput();
			if (isValidUserInput) {
				CheckOutObject checkOutRemember = JobViewerDBHandler
						.getCheckOutRemember(view.getContext());
				checkOutRemember.setVistecId(mDistrict2.getText().toString()
						+ mTaskNumber1.getText().toString()
						+ mTaskNumber2.getText().toString());
				if (pollutionCheckBox.isChecked()) {
					checkOutRemember.setIsPollutionSelected("true");
				} else {
					checkOutRemember.setIsPollutionSelected("");
				}
				JobViewerDBHandler.saveCheckOutRemember(view.getContext(),
						checkOutRemember);
				Intent intent = new Intent(NewWorkActivity.this,
						CaptureVistecActivity.class);
				startActivity(intent);
			}
		}
	}

	private boolean isValidUserInput() {
		if (mDistrict2.getText().toString().length() < 2) {
			setError(mDistrict2);
			return false;
		} else if (mTaskNumber1.getText().toString().length() < 2) {
			setError(mTaskNumber1);
			return false;
		} else if (mTaskNumber2.getText().toString().length() < 4) {
			setError(mTaskNumber2);
			return false;
		}
		return true;
	}

	public static void setError(EditText editText) {
		final Drawable error_indicator;
		error_indicator = ResourcesCompat.getDrawable(context.getResources(),
				R.drawable.small_error_icon, null);
		int left = 0;
		int top = 0;
		int right = error_indicator.getIntrinsicHeight();
		int bottom = error_indicator.getIntrinsicWidth();
		error_indicator.setBounds(new Rect(left, top, right, bottom));
		switch (editText.getId()) {
		case R.id.distric2_edittext:
			mDistrict2.setError("Enter 2 digits", error_indicator);
			break;
		case R.id.distric3_edittext:
			mTaskNumber1.setError("Enter 2 digits", error_indicator);
			break;
		case R.id.distric4_edittext:
			mTaskNumber2.setError("Enter 4 letters or digits", error_indicator);
			break;

		default:
			break;
		}
	}

}
