package com.jobviewer.exception;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lanesgroup.jobviewer.R;

/**
 * Created by e050027 on 10/28/2015.
 */
public class CustomErrorDialog implements View.OnClickListener {

	private AlertDialog.Builder alertDialog;
	private AlertDialog dialog;
	private IDialogListener myListener;
	private Button negativeButton, positiveButton;

	public CustomErrorDialog(Context context, String title, String message,
			String buttonText, IDialogListener listener) {
		myListener = listener;
		alertDialog = new AlertDialog.Builder(context,
				R.style.AppCompatDialogStyle);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.custom_error_dialog, null);
		TextView txtTitle = (TextView) view.findViewById(R.id.dialog_title);
		TextView txtMessage = (TextView) view.findViewById(R.id.dialog_message);

		txtTitle.setText(title);
		txtMessage.setText(message);
		Button btnClose = (Button) view.findViewById(R.id.btn_close);
		btnClose.setText(buttonText);
		alertDialog.setView(view);
		alertDialog.setCancelable(false);
		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (myListener == null) {
					dialog.dismiss();
				} else
					myListener.onPositiveButtonClick(dialog);
			}
		});
	}

	public void show() {
		if (dialog != null) {
			dialog.dismiss();
		}

		dialog = alertDialog.show();
	}

	@Override
	public void onClick(View v) {
		if (v == negativeButton) {
			myListener.onNegativeButtonClick(dialog);
		} else {
			myListener.onPositiveButtonClick(dialog);
		}
	}
}
