package com.lanesgroup.jobviewer.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.R;

public class ShoutOutCompleteFragment extends Fragment implements
		OnClickListener {
	private View mRootView;
	private Button doneButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mRootView = inflater.inflate(R.layout.shout_out_complete_fragment,
				container, false);
		doneButton = (Button) mRootView.findViewById(R.id.doneButton);
		doneButton.setOnClickListener(this);
		return mRootView;
	}

	@Override
	public void onClick(View v) {
		((BaseActivity) getActivity()).goBackToStartScreenFromShoutOut();
	}

}
