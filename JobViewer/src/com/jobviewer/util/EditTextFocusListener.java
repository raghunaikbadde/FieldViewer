package com.jobviewer.util;

import android.content.Context;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.lanesgroup.jobviewer.NewWorkActivity;

public class EditTextFocusListener implements OnFocusChangeListener {
	private int maxLenght;
	private EditText editText;
	private Context context;

	public EditTextFocusListener(Context context, EditText editText, int length) {
		maxLenght = length;
		this.editText = editText;
		this.context = context;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {

			String text = ((EditText) v).getText().toString();
			if (text.length() < maxLenght) {
				NewWorkActivity.setError(editText);
			}
		}
		NewWorkActivity.enableNextButton();
	}
}
