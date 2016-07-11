package com.jobviewer.util;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lanesgroup.jobviewer.R;

public class ConfirmStopDialog extends Dialog implements OnClickListener {

    private Button mWorkStop, mWorkStopDismiss;
    private ConfirmStopWork mCallback;
    private Context mContext;
    private final TextWatcher reasonTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                enableDisableConfirmButton(false);
            } else {
                enableDisableConfirmButton(true);
            }

        }
    };
    private TextView mHeader, mMessage;
    private String eventType;
    private EditText mReasonEditText;

    public ConfirmStopDialog(Context context, ConfirmStopWork callback,
                             String eventType) {
        super(context);
        mContext=context;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        this.eventType = eventType;
        this.mCallback = callback;
        this.setCancelable(false);
        setContentView(R.layout.dialog_box3);

        mReasonEditText = (EditText) findViewById(R.id.manager_name_edittext);

        mWorkStop = (Button) findViewById(R.id.dialog_ok);
        mWorkStopDismiss = (Button) findViewById(R.id.dialog_cancel);
        mWorkStop.setOnClickListener(this);
        mWorkStopDismiss.setOnClickListener(this);
        mReasonEditText.addTextChangedListener(reasonTextWatcher);
        enableDisableConfirmButton(false);

    }

    @Override
    public void onClick(View v) {
        if (v == mWorkStop) {
            mCallback.onConfirmStopWork(mReasonEditText.getText().toString());
        } else if (v == mWorkStopDismiss) {
            mCallback.onDismissStopWork();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void enableDisableConfirmButton(boolean isEnable) {
        if (isEnable) {
            mWorkStop.setBackground(mContext.getResources().getDrawable(R.drawable.dialog_red_button));
            mWorkStop.setEnabled(true);
        } else {
            mWorkStop.setBackground(mContext.getResources().getDrawable(R.drawable.dialog_dark_grey_button));
            mWorkStop.setEnabled(false);
        }
    }

    public interface ConfirmStopWork {
        void onConfirmStopWork(String reason);

        void onDismissStopWork();
    }
}
