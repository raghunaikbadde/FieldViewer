package com.jobviewer.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.lanesgroup.jobviewer.R;

public class ConfirmDialog extends Dialog implements OnClickListener {

	private TextView mTimeContinue, mTimeCancel;
	private ConfirmDialogCallback mCallback;
	private ConfirmDialogCallbackForNoPhotos mCallbackForNoPhotos;
	private Context mContext;
	private TextView mHeader, mMessage;
	public static String eventType;

	public interface ConfirmDialogCallback {
		void onConfirmStartTraining();

		void onConfirmDismiss();
	}

	public interface ConfirmDialogCallbackForNoPhotos {
		void onConfirmStartWithNoPhotos();

		void onConfirmDismissWithNoPhotos();
	}

	public ConfirmDialog(Context context, ConfirmDialogCallback callback,
			String eventType) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));

		ConfirmDialog.eventType = eventType;
		this.setCancelable(false);
		setContentView(R.layout.confirm_dialog);
		mHeader = (TextView) findViewById(R.id.dialog_header);
		mHeader.setText(Constants.END_TRAINING_HEADER);
		mMessage = (TextView) findViewById(R.id.cofirmation_msg_text);
		String vistecId = "";
		CheckOutObject checkOutObject = JobViewerDBHandler
				.getCheckOutRemember(context);
		if (checkOutObject != null
				&& Utils.isNullOrEmpty(checkOutObject.getVistecId())) {
			vistecId = checkOutObject.getVistecId();
		}
		Log.d(Utils.LOG_TAG, "vistecId " + vistecId);
		if (Utils.isNullOrEmpty(vistecId)) {
			Log.d(Utils.LOG_TAG, "confirm dialog utils vistecId " + vistecId);
			vistecId = Utils.checkOutObject.getVistecId();
		}
		Log.d(Utils.LOG_TAG, "vistecId " + vistecId);
		if (ConfirmDialog.eventType.equalsIgnoreCase(Constants.START_TRAINING)) {
			mMessage.setText(Constants.START_TRAINING_MESSAGE);
		} else if (ConfirmDialog.eventType
				.equalsIgnoreCase(Constants.POLLUTION_CONFIRMATION)) {
			mMessage.setText(context.getResources().getString(
					R.string.pollution_cofirmation_msg));
			mHeader.setText(context.getResources().getString(
					R.string.pollution_confirmation));
		} else if (ConfirmDialog.eventType
				.equalsIgnoreCase(Constants.WORK_NO_PHOTOS_CONFIRMATION)) {
			mMessage.setText(context.getResources().getString(
					R.string.workWithNoPhotosConfirmation));
			mHeader.setText(context.getResources().getString(R.string.confirm));
		} else if (ConfirmDialog.eventType
				.equalsIgnoreCase(ActivityConstants.LEAVE_WORK_CONFIMRATION)) {
			mMessage.setText(context.getResources().getString(
					R.string.workEndConfirmationMsg)
					+ " " + vistecId +"?");
			mHeader.setText(context.getResources().getString(
					R.string.workEndConfirmation));
		} else if (ConfirmDialog.eventType
				.equalsIgnoreCase(Constants.TAP_DA_PHONE_CALL)) {
			mMessage.setText(context.getResources().getString(
					R.string.DACallConfimrationMsg));
			mHeader.setText(context.getResources().getString(
					R.string.DACallConfirmation));
		} else {
			mMessage.setText(Constants.END_TRAINING_MESSAGE);
		}

		mContext = context;
		mCallback = callback;
		mCallbackForNoPhotos = null;
		mTimeCancel = (TextView) findViewById(R.id.dialog_cancel);
		mTimeCancel.setOnClickListener(this);
		mTimeContinue = (TextView) findViewById(R.id.dialog_ok);
		mTimeContinue.setOnClickListener(this);
	}

	public ConfirmDialog(Context context,
			ConfirmDialogCallbackForNoPhotos callback, String eventType,
			String extra) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));

		ConfirmDialog.eventType = eventType;
		this.setCancelable(false);
		setContentView(R.layout.confirm_dialog);
		mHeader = (TextView) findViewById(R.id.dialog_header);
		mHeader.setText(Constants.END_TRAINING_HEADER);
		mMessage = (TextView) findViewById(R.id.cofirmation_msg_text);

		String vistecId = "";
		CheckOutObject checkOutObject = JobViewerDBHandler
				.getCheckOutRemember(context);
		if (checkOutObject != null
				&& Utils.isNullOrEmpty(checkOutObject.getVistecId())) {
			vistecId = checkOutObject.getVistecId();
		}
		Log.d(Utils.LOG_TAG, "vistecId " + vistecId);
		if (Utils.isNullOrEmpty(vistecId)) {
			Log.d(Utils.LOG_TAG, "confirm dialog utils vistecId " + vistecId);
			vistecId = Utils.checkOutObject.getVistecId();
		}
		Log.d(Utils.LOG_TAG, "vistecId " + vistecId);
		if (ConfirmDialog.eventType.equalsIgnoreCase(Constants.START_TRAINING)) {
			mMessage.setText(Constants.START_TRAINING_MESSAGE);
		} else if (ConfirmDialog.eventType
				.equalsIgnoreCase(Constants.POLLUTION_CONFIRMATION)) {
			mMessage.setText(context.getResources().getString(
					R.string.pollution_cofirmation_msg));
			mHeader.setText(context.getResources().getString(
					R.string.pollution_confirmation));
		} else if (ConfirmDialog.eventType
				.equalsIgnoreCase(Constants.WORK_NO_PHOTOS_CONFIRMATION)) {
			mMessage.setText(context.getResources().getString(
					R.string.workWithNoPhotosConfirmation));
			mHeader.setText(context.getResources().getString(R.string.confirm));
		} else if (ConfirmDialog.eventType
				.equalsIgnoreCase(ActivityConstants.LEAVE_WORK_CONFIMRATION)) {
			mMessage.setText(context.getResources().getString(
					R.string.workEndConfirmationMsg)
					+ " " + vistecId +"?");
			mHeader.setText(context.getResources().getString(
					R.string.workEndConfirmation));
		} else if (ConfirmDialog.eventType
				.equalsIgnoreCase(Constants.TAP_DA_PHONE_CALL)) {
			mMessage.setText(context.getResources().getString(
					R.string.DACallConfimrationMsg));
			mHeader.setText(context.getResources().getString(
					R.string.DACallConfirmation));
		} else {
			mMessage.setText(Constants.END_TRAINING_MESSAGE);
		}

		mContext = context;
		mCallbackForNoPhotos = callback;
		mCallback = null;
		mTimeCancel = (TextView) findViewById(R.id.dialog_cancel);
		mTimeCancel.setOnClickListener(this);
		mTimeContinue = (TextView) findViewById(R.id.dialog_ok);
		mTimeContinue.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view == mTimeCancel) {
			this.dismiss();
			if (eventType.equalsIgnoreCase(Constants.POLLUTION_CONFIRMATION)
					|| eventType.equalsIgnoreCase(Constants.TAP_DA_PHONE_CALL)) {
				mCallback.onConfirmDismiss();
			}
			if (eventType
					.equalsIgnoreCase(ActivityConstants.LEAVE_WORK_CONFIMRATION)||
					eventType
					.equalsIgnoreCase(Constants.START_TRAINING)) {
				mCallback.onConfirmDismiss();
			}
		} else if (view == mTimeContinue) {
			this.dismiss();
			if (mCallback != null)
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
