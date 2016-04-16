package com.jobviewer.confined.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.jobviewer.confined.ConfinedQuestionManager;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.R;

public class ConfinedAssessmentCompleteFragment extends Fragment implements
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
		doneButton = (Button) mRootView.findViewById(R.id.doneButton);
		doneButton.setOnClickListener(this);
		return mRootView;
	}

	@Override
	public void onClick(View v) {
		ConfinedQuestionManager.getInstance().saveAssessment("confined");
		/*CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		checkOutRemember.setIsAssessmentCompleted("true");
		JobViewerDBHandler
				.saveCheckOutRemember(getActivity(), checkOutRemember);*/
		((BaseActivity) getActivity()).finish();

	}

}
