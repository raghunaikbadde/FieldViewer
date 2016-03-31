package com.lanesgroup.jobviewer.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lanesgroup.jobviewer.R;

public class RiskAssessmentFragment extends Fragment implements OnClickListener {
	
	private ProgressBar mProgress;
	private TextView mProgressStep;
	private CheckBox mRememberSelection, mExcavation, mNonExcavation;
	private Button mSave, mNext;
	private View mRootView;
	
	public interface onClicksRiskAssessment{
		public void onNextClick();
		public void onCancelClick();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mRootView = inflater.inflate(R.layout.risk_assessment_screen, container,
				false); 
		initUI();
		return mRootView;
	}
	private void initUI() {
		mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
		mProgressStep = (TextView) mRootView.findViewById(R.id.progress_step_text);
		mRememberSelection = (CheckBox) mRootView.findViewById(R.id.confirm_checkbox);
		mExcavation = (CheckBox) mRootView.findViewById(R.id.checkBox1);
		mNonExcavation = (CheckBox) mRootView.findViewById(R.id.checkBox2);
		mSave = (Button) mRootView.findViewById(R.id.button1);
		mSave.setOnClickListener(this);
		mNext = (Button) mRootView.findViewById(R.id.button2);
		mNext.setOnClickListener(this);
	}
	@Override
	public void onClick(View view) {
		if(view == mSave){
			
		}else if(view == mNext){
			getFragmentManager().popBackStack();
			getFragmentManager().beginTransaction()
			.add(R.id.container, new CheckTypeFragment()).commit();
			
		}
		
	}
}	