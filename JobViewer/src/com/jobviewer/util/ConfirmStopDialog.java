package com.jobviewer.util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jobviewer.util.ConfirmDialog.ConfirmDialogCallback;
import com.lanesgroup.jobviewer.R;

public class ConfirmStopDialog extends Dialog implements OnClickListener {

	private Button mWorkStop, mWorkStopDismiss;
	private ConfirmStopWork mCallback;
	private Context mContext;
	private TextView mHeader, mMessage;
	String eventType;
	private EditText mReasonEditText;
	public interface ConfirmStopWork {
		public void onConfirmStopWork(String reason);

		public void onDismissStopWork();
	}
	
	public ConfirmStopDialog(Context context, ConfirmStopWork callback,
			String eventType) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.eventType = eventType;
		this.mCallback = callback;
		this.setCancelable(false);
		setContentView(R.layout.dialog_box3);
		
		mReasonEditText = (EditText) findViewById(R.id.manager_name_edittext);
		
		mWorkStop = (Button) findViewById(R.id.dialog_ok);
		mWorkStopDismiss = (Button) findViewById(R.id.dialog_cancel);
		mWorkStop.setOnClickListener(this);
		mWorkStopDismiss.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if(v==mWorkStop){
			mCallback.onConfirmStopWork(mReasonEditText.getText().toString());
		} else if ( v == mWorkStopDismiss){
			mCallback.onDismissStopWork();
		}
	}
}
