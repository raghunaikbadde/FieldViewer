package com.lanesgroup.jobviewer.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.jobviewer.survey.object.QuestionMaster;
import com.jobviewer.survey.object.util.QuestionManager;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.WorkSuccessActivity;

public class StopFragment extends Fragment implements OnClickListener{

	private View mRootView;
	private Button mStopButton;
	private Button mResumeButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.stop_work_screen, container,
				false);
		mStopButton = (Button)mRootView.findViewById(R.id.button1);
		mResumeButton = (Button)mRootView.findViewById(R.id.button2);
		mStopButton.setOnClickListener(this);
		mResumeButton.setOnClickListener(this);
		return mRootView;
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == mStopButton.getId()){
			//Popup dialog to report to field manager
			startEndMethod(); 
		} else if(v.getId() == mResumeButton.getId()){
			//QuestionManager.getInstance().loadPreviousFragmentFromStopFragment();
			
		}
	}

	private void startEndMethod() {
		Intent appPageActivityIntent = new Intent(getActivity(),ActivityPageActivity.class);
		appPageActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(appPageActivityIntent);
	}
}
