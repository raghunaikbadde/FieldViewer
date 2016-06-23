package com.jobviewer.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.lanesgroup.jobviewer.NewWorkActivity;

public class EditTextWatcher implements TextWatcher {
	private int maxLenght;
	private EditText editText;
	private Context context;
	private EditText nextFocusEditText;

	public EditTextWatcher(Context context, EditText editText, int length,
			EditText nextFocusEditText) {
		maxLenght = length;
		this.editText = editText;
		this.context = context;
		this.nextFocusEditText = nextFocusEditText;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTextChanged(Editable s) {

		String text = s.toString();
		if (text.length() < maxLenght) {
			NewWorkActivity.setError(editText);
		}
		if (nextFocusEditText != null && text.length() == maxLenght) {

			nextFocusEditText.requestFocus();
		}
		NewWorkActivity.enableNextButton();

	}
}
