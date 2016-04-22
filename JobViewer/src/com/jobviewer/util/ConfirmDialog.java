package com.jobviewer.util;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.lanesgroup.jobviewer.R;

public class ConfirmDialog extends Dialog implements OnClickListener {

	private Button mTimeContinue, mTimeCancel;
	private ConfirmDialogCallback mCallback;
	ConfirmDialogCallbackForNoPhotos mCallbackForNoPhotos;
	private Context mContext;
	private TextView mHeader, mMessage;
	public static String eventType;

	public interface ConfirmDialogCallback {
		public void onConfirmStartTraining();

		public void onConfirmDismiss();
	}

	public interface ConfirmDialogCallbackForNoPhotos {
		public void onConfirmStartWithNoPhotos();

		public void onConfirmDismissWithNoPhotos();
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
		} else if(this.eventType.equalsIgnoreCase(Constants.WORK_NO_PHOTOS_CONFIRMATION)){
			mMessage.setText(context.getResources().getString(R.string.workWithNoPhotosConfirmation));
			mHeader.setText(context.getResources().getString(R.string.confirm));
		} else if(this.eventType.equalsIgnoreCase(ActivityConstants.LEAVE_WORK_CONFIMRATION)){
			mMessage.setText(context.getResources().getString(R.string.workEndConfirmationMsg));
			mHeader.setText(context.getResources().getString(R.string.workEndConfirmation));
		} else {
			mMessage.setText(Constants.END_TRAINING_MESSAGE);
		}
		
		mContext = context;
		mCallback = callback;
		mCallbackForNoPhotos = null;
		mTimeCancel = (Button) findViewById(R.id.dialog_cancel);
		mTimeCancel.setOnClickListener(this);
		mTimeContinue = (Button) findViewById(R.id.dialog_ok);
		mTimeContinue.setOnClickListener(this);
	}
	
	public ConfirmDialog(Context context, ConfirmDialogCallbackForNoPhotos callback,
			String eventType,String extra) {
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
		} else if(this.eventType.equalsIgnoreCase(Constants.WORK_NO_PHOTOS_CONFIRMATION)){
			mMessage.setText(context.getResources().getString(R.string.workWithNoPhotosConfirmation));
			mHeader.setText(context.getResources().getString(R.string.confirm));
		}  else if(this.eventType.equalsIgnoreCase(ActivityConstants.LEAVE_WORK_CONFIMRATION)){
			mMessage.setText(context.getResources().getString(R.string.workEndConfirmationMsg));
			mHeader.setText(context.getResources().getString(R.string.workEndConfirmation));
		} else {
			mMessage.setText(Constants.END_TRAINING_MESSAGE);
		}
		
		mContext = context;
		mCallbackForNoPhotos = callback;
		mCallback = null;
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
			if(eventType.equalsIgnoreCase(ActivityConstants.LEAVE_WORK_CONFIMRATION)){
				mCallback.onConfirmDismiss();
			}
		} else if (view == mTimeContinue) {
			this.dismiss();			
			if(mCallback != null)
				mCallback.onConfirmStartTraining();
			else 
				mCallbackForNoPhotos.onConfirmStartWithNoPhotos();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		this.dismiss();
		return true;
	}
}
