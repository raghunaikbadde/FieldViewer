package com.jobviewer.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.R;

public class ConfirmDialog extends Dialog implements OnClickListener {

	private Button mTimeContinue, mTimeCancel;
	private ConfirmDialogCallback mCallback;
	private Context mContext;
	private TextView mHeader, mMessage;
	String eventType;

	public interface ConfirmDialogCallback {
		public void onConfirmStartTraining();

		public void onConfirmDismiss();
	}

	public ConfirmDialog(Context context, ConfirmDialogCallback callback,
			String eventType) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.eventType = eventType;
		this.setCancelable(false);
		setContentView(R.layout.confirm_dialog);
		mHeader = (TextView) findViewById(R.id.dialog_header);
		mHeader.setText(Constants.END_TRAINING_HEADER);
		mMessage = (TextView) findViewById(R.id.cofirmation_msg_text);
		if(this.eventType.equalsIgnoreCase(Constants.START_TRAINING)){
			mMessage.setText(Constants.START_TRAINING_MESSAGE);
		} else if(this.eventType.equalsIgnoreCase(Constants.POLLUTION_CONFIRMATION)){
			mMessage.setText(context.getResources().getString(R.string.pollution_cofirmation_msg));
			mHeader.setText(context.getResources().getString(R.string.pollution_confirmation));
		} else {
			mMessage.setText(Constants.END_TRAINING_MESSAGE);
		}
		
		mContext = context;
		mCallback = callback;
		mTimeCancel = (Button) findViewById(R.id.dialog_cancel);
		mTimeCancel.setOnClickListener(this);
		mTimeContinue = (Button) findViewById(R.id.dialog_ok);
		mTimeContinue.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view == mTimeCancel) {
			this.dismiss();
			if(eventType.equalsIgnoreCase(Constants.POLLUTION_CONFIRMATION)){
				mCallback.onConfirmDismiss();
			}
		} else if (view == mTimeContinue) {
			this.dismiss();
			mCallback.onConfirmStartTraining();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		this.dismiss();
		return true;
	}
}
