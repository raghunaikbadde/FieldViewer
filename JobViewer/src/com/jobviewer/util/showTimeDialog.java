package com.jobviewer.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

public class showTimeDialog extends Dialog implements OnClickListener {

	private TextView mTime;
	private CheckBox mCheckOverride;
	private Button mTimeContinue, mTimeCancel;
	private boolean mIsChecked;
	private String mCurrentTime;
	private DialogCallback mCallback;
	private Context mContext;
	String eventType;

	public interface DialogCallback {
		public void onContinue();

		public void onDismiss();
	}

	public showTimeDialog(Context context, DialogCallback callback,
			String eventType) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.eventType = eventType;
		this.setCancelable(false);
		setContentView(R.layout.dialog_box4);
		mContext = context;
		mCallback = callback;
		mTime = (TextView) findViewById(R.id.time);
		mCurrentTime = new SimpleDateFormat("HH:mm:ss dd MMM yyyy")
				.format(Calendar.getInstance().getTime());
		if ("start".equalsIgnoreCase(eventType)) {
			Utils.timeSheetRequest.setStarted_at(mCurrentTime);
		} else if ("travel".equalsIgnoreCase(eventType)) {
			Utils.startTravelTimeRequest.setStarted_at(mCurrentTime);
		} else {
			Utils.endTimeRequest.setStarted_at(mCurrentTime);
		}
		User userProfile = JobViewerDBHandler.getUserProfile(mContext);
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getContext());
		if (!Utils.isNullOrEmpty(userProfile.getUserid())) {
			if ("start".equalsIgnoreCase(eventType)) {
				Utils.timeSheetRequest.setUser_id(userProfile.getEmail());
				if (!Utils.isNullOrEmpty(checkOutRemember.getVistecId())) {
					Utils.timeSheetRequest.setReference_id(checkOutRemember
							.getVistecId());
				}
				Utils.timeSheetRequest.setRecord_for(userProfile.getEmail());
			} else if ("travel".equalsIgnoreCase(eventType)) {
				Utils.startTravelTimeRequest.setUser_id(userProfile.getEmail());
				if (!Utils.isNullOrEmpty(checkOutRemember.getVistecId())) {
					Utils.startTravelTimeRequest
							.setReference_id(checkOutRemember.getVistecId());
				}
				Utils.startTravelTimeRequest.setRecord_for(userProfile
						.getEmail());
			} else {
				Utils.endTimeRequest.setUser_id(userProfile.getEmail());
				if (!Utils.isNullOrEmpty(checkOutRemember.getVistecId())) {
					Utils.endTimeRequest.setReference_id(checkOutRemember
							.getVistecId());
				}
				Utils.endTimeRequest.setRecord_for(userProfile.getEmail());
			}

		}
		mTime.setText(mCurrentTime);
		mTimeCancel = (Button) findViewById(R.id.dialog_cancel);
		mTimeCancel.setOnClickListener(this);
		mTimeContinue = (Button) findViewById(R.id.dialog_ok);
		mTimeContinue.setOnClickListener(this);
		mCheckOverride = (CheckBox) findViewById(R.id.checkBox1);
		mCheckOverride
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						mIsChecked = isChecked;
						if (isChecked)
							mTimeContinue.setText("Override");
						else
							mTimeContinue.setText("Confirm");
					}
				});
	}

	@Override
	public void onClick(View view) {
		if (view == mTimeCancel) {
			this.dismiss();
		} else if (view == mTimeContinue) {
			if (mIsChecked) {
				this.dismiss();
				if ("start".equalsIgnoreCase(eventType)) {
					Utils.timeSheetRequest.setIs_overriden("true");
				} else if ("travel".equalsIgnoreCase(eventType)) {
					Utils.startTravelTimeRequest.setIs_overriden("true");
				} else {
					Utils.endTimeRequest.setIs_overriden("true");
				}
				Intent intent = new Intent(mContext, ChangeTimeDialog.class);
				intent.putExtra("eventType", eventType);
				((Activity) mContext).startActivityForResult(intent,
						Constants.RESULT_CODE_CHANGE_TIME);
			} else {
				this.dismiss();
				mCallback.onContinue();
			}
		}
	}
}
