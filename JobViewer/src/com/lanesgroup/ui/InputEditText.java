package com.lanesgroup.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.lanesgroup.jobviewer.R;

/**
 * Input EditText which allows define custom drawable for error state
 */
public class InputEditText extends EditText {

	private static final int[] STATE_ERROR = { R.attr.state_error };

	private boolean mIsError = false;

	public InputEditText(Context context) {
		this(context, null, 0);
		init();
	}

	public InputEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public InputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public InputEditText(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// empty
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				setError(null);
			}

			@Override
			public void afterTextChanged(Editable s) {
				// empty
			}
		});
	}

	@Override
	public void setError(CharSequence error) {
		mIsError = error != null;
		super.setError(error);
		refreshDrawableState();
	}

	@Override
	public void setError(CharSequence error, Drawable icon) {
		mIsError = error != null;
		super.setError(error, icon);
		refreshDrawableState();
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (mIsError) {
			mergeDrawableStates(drawableState, STATE_ERROR);
		}
		return drawableState;
	}
}
