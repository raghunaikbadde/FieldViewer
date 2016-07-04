package com.lanesgroup.jobviewer;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by system-local on 01-07-2016.
 */
public class ConfirmWorkStopDialog extends Dialog implements View.OnClickListener {

    private TextView dialog_ok, dialog_cancel;
    private ConfirmWorkStopDialogCallback mCallback;
    private Context mContext;

    public ConfirmWorkStopDialog(Context context, ConfirmWorkStopDialogCallback callback) {
        super(context);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        this.setCancelable(false);
        setContentView(R.layout.confirm_work_stop);

        mContext = context;
        mCallback = callback;
        dialog_ok = (TextView) findViewById(R.id.dialog_ok);
        dialog_ok.setOnClickListener(this);
        dialog_cancel = (TextView) findViewById(R.id.dialog_cancel);
        dialog_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == dialog_cancel) {
            this.dismiss();

            mCallback.onConfirmDismiss();

        } else if (view == dialog_ok) {
            this.dismiss();
            if (mCallback != null)
                mCallback.onConfirmButtonPressed();

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        this.dismiss();
        return true;
    }

    public interface ConfirmWorkStopDialogCallback {
        void onConfirmButtonPressed();

        void onConfirmDismiss();
    }
}
