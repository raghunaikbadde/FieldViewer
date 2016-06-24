package com.jobviewer.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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

public class showTimeDialog extends Dialog implements OnClickListener {

	private TextView mTitle, mDesc;
	private TextView mTime;
	private CheckBox mCheckOverride;
	private Button mTimeContinue, mTimeCancel;
	private boolean mIsChecked;
	private String mCurrentTime;
	private DialogCallback mCallback;
	private Context mContext;
	private String eventType;

	public interface DialogCallback {
		void onContinue();

		void onDismiss();
	}

	public showTimeDialog(Context context, DialogCallback callback,
			String eventType) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		this.eventType = eventType;
		this.setCancelable(false);
		setContentView(R.layout.dialog_box4);
		mContext = context;
		mCallback = callback;
		mTime = (TextView) findViewById(R.id.time);
		mTitle = (TextView) findViewById(R.id.dialog_info);
		mDesc = (TextView) findViewById(R.id.your_break_text);

		mCurrentTime = new SimpleDateFormat("HH:mm:ss dd MMM yyyy")
				.format(Calendar.getInstance().getTime());
		if ("start".equalsIgnoreCase(eventType)) {
			Utils.timeSheetRequest.setStarted_at(mCurrentTime);
		} else if ("travel".equalsIgnoreCase(eventType)) {
			mTitle.setText(mContext.getResources().getString(
					R.string.start_travel));
			mDesc.setText(mContext.getResources().getString(
					R.string.your_travel_start_time));
			Utils.startTravelTimeRequest.setStarted_at(mCurrentTime);
		} else if ("End Travel".equalsIgnoreCase(eventType)) {
			mTitle.setText(mContext.getResources().getString(
					R.string.end_travel_str));
			mDesc.setText(mContext.getResources().getString(
					R.string.your_travel_end_time));
			Utils.endTravelTimeRequest.setStarted_at(mCurrentTime);
		} else if ("ClockIn".equalsIgnoreCase(eventType)) {
			mTitle.setText(mContext.getResources().getString(
					R.string.shiftStartStr));
			mDesc.setText(mContext.getResources().getString(
					R.string.shiftStartStrDesc));
			Utils.startShiftTimeRequest.setStarted_at(mCurrentTime);
		} else {
			mTitle.setText(mContext.getResources().getString(
					R.string.end_break_str));
			mDesc.setText(mContext.getResources().getString(
					R.string.your_break_end_time));
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
			} else if ("End Travel".equalsIgnoreCase(eventType)) {
				Utils.endTravelTimeRequest.setUser_id(userProfile.getEmail());
				if (!Utils.isNullOrEmpty(checkOutRemember.getVistecId())) {
					Utils.endTravelTimeRequest.setReference_id(checkOutRemember
							.getVistecId());
				}
				Utils.endTravelTimeRequest
						.setRecord_for(userProfile.getEmail());
			} else if ("ClockIn".equalsIgnoreCase(eventType)) {
				Utils.startShiftTimeRequest.setUser_id(userProfile.getEmail());
				if (!Utils.isNullOrEmpty(checkOutRemember.getVistecId())) {
					Utils.startShiftTimeRequest
							.setReference_id(checkOutRemember.getVistecId());
				}
				Utils.startShiftTimeRequest.setRecord_for(userProfile
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
				} else if ("End Travel".equalsIgnoreCase(eventType)) {
					Utils.endTravelTimeRequest.setIs_overriden("true");
				} else if ("ClockIn".equalsIgnoreCase(eventType)) {
					Utils.startShiftTimeRequest.setIs_overriden("true");
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		this.dismiss();
		return true;
	}
}
