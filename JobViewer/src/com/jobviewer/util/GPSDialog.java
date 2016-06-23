package com.jobviewer.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.R;

public class GPSDialog extends Dialog implements
		android.view.View.OnClickListener {

	private TextView mHeaderTxt;
	private TextView mMessageTxt;
	private Button mExitButton;
	private Button mSettingsButton;
	private Context mContext;

	public GPSDialog(Context context) {
		super(context);
		this.mContext = context;
		initUI(context);
		updateDetailsOnUI(context);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
	}

	private void initUI(Context context) {
		this.setCancelable(false);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.confirm_dialog);

		mHeaderTxt = (TextView) findViewById(R.id.dialog_header);
		mMessageTxt = (TextView) findViewById(R.id.cofirmation_msg_text);
		mExitButton = (Button) findViewById(R.id.dialog_ok);
		mSettingsButton = (Button) findViewById(R.id.dialog_cancel);
	}

	private void updateDetailsOnUI(Context context) {
		mHeaderTxt.setText(context.getResources().getString(
				R.string.gpsSwitchedOff));
		mMessageTxt.setText(context.getResources().getString(
				R.string.gpsSwitchedOffMessage));
		mExitButton.setText(context.getResources().getString(
				R.string.exitButtonText));
		mSettingsButton.setText(context.getResources().getString(
				R.string.settignsButtonText));

		mExitButton.setOnClickListener(this);
		mSettingsButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == mExitButton) {
			((BaseActivity) mContext).closeApplication();
		} else if (v == mSettingsButton) {
			String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
			mContext.startActivity(new Intent(action));
			((BaseActivity) mContext).closeApplication();
		}

	}

}
