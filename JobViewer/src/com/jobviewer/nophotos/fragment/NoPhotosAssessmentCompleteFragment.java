package com.jobviewer.nophotos.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.nophotos.WorkWithNoPhotosQuestionManager;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.R;

public class NoPhotosAssessmentCompleteFragment extends Fragment implements
		OnClickListener {
	private View mRootView;
	private Button doneButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mRootView = inflater.inflate(R.layout.assessment_complete_fragment,
				container, false);
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		TextView screenTitle = (TextView) mRootView
				.findViewById(R.id.screenTitle);
		
		TextView questionTitle = (TextView) mRootView
				.findViewById(R.id.questionTitle);
		questionTitle.setText(getResources().getString(R.string.persoal_risk_assement_complete_str));
		TextView question = (TextView) mRootView
				.findViewById(R.id.question);
		questionTitle.setText(getResources().getString(R.string.persoal_riskassement_continue_str));
		
		TextView tapDone = (TextView) mRootView
				.findViewById(R.id.tapDone);
		tapDone.setVisibility(View.VISIBLE);
		
		if (ActivityConstants.EXCAVATION.equalsIgnoreCase(checkOutRemember
				.getAssessmentSelected())) {
			screenTitle.setText(getActivity().getResources().getString(
					R.string.persoal_risk_assement_str));
		} else {
			screenTitle.setText(getActivity().getResources().getString(
					R.string.persoal_risk_assement_str));
		}
		doneButton = (Button) mRootView.findViewById(R.id.doneButton);
		doneButton.setOnClickListener(this);
		return mRootView;
	}

	@Override
	public void onClick(View v) {
		WorkWithNoPhotosQuestionManager.getInstance().saveAssessment("work");
		Intent homeIntent = new Intent(getActivity(),
					ActivityPageActivity.class);
		homeIntent.putExtra(Constants.WORK_NO_PHOTOS_HOME, true);
		startActivity(homeIntent);
	}

}
