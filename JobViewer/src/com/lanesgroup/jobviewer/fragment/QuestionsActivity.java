package com.lanesgroup.jobviewer.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.SurveyJson;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.QuestionMaster;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.survey.object.util.SurveyUtil;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.fragment.CheckTypeFragment.onClicksEnterJobNumber;
import com.lanesgroup.jobviewer.fragment.RiskAssessmentFragment.onClicksRiskAssessment;

public class QuestionsActivity extends Activity implements
		onClicksRiskAssessment, onClicksEnterJobNumber {

	private static FragmentManager mFragmentManager;
	QuestionManager manager;
	QuestionMaster loadJsonFromAssets;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.questions_flow_screen);
		Utils.startService(this);
		mFragmentManager = getFragmentManager();
		manager = QuestionManager.getInstance();
		SurveyJson questionSet = JobViewerDBHandler.getQuestionSet(this);
		if (questionSet != null
				&& !Utils.isNullOrEmpty(questionSet.getQuestionJson())) {
			manager.reloadAssessment(questionSet);
		} else {
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(this);
			if (checkOutRemember.getAssessmentSelected().equalsIgnoreCase(
					ActivityConstants.EXCAVATION)) {
				loadJsonFromAssets = SurveyUtil.loadJsonFromAssets(this,
						"excavation_risk_assessment_survey1.json");
			} else {
				loadJsonFromAssets = SurveyUtil.loadJsonFromAssets(this,
						"non_excavation_risk_assessment_survey.json");
			}

			manager.setQuestionMaster(loadJsonFromAssets);
			Screen firstScreen = manager.getFirstScreen();
			int questionType = SurveyUtil.getQuestionType(firstScreen
					.get_type());
			loadFragment(SurveyUtil.getFragment(questionType));
		}
	}

	@Override
	public void onNextClick() {

	}

	@Override
	public void onCancelClick() {

	}

	private void loadFragment(Fragment fragment) {
		Fragment attachedFragment = mFragmentManager
				.findFragmentById(R.id.container);
		Fragment fragmentToBeAttached = mFragmentManager
				.findFragmentByTag(fragment.getTag());
		if (fragmentToBeAttached == null) {
			fragmentToBeAttached = fragment;
		}

		FragmentTransaction fragmentTx = mFragmentManager.beginTransaction();
		if (attachedFragment != null) {
			fragmentTx.detach(attachedFragment);
		}

		if (!fragmentToBeAttached.isAdded()) {
			fragmentTx.add(R.id.container, fragmentToBeAttached,
					fragment.getTag());
		}
		if (fragmentToBeAttached.isDetached()) {
			fragmentTx.attach(fragmentToBeAttached);
		}
		if (!(fragmentToBeAttached instanceof CheckTypeFragment)) {
			fragmentTx.addToBackStack(fragment.getTag());
		}
		fragmentTx.commit();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			QuestionManager.getInstance().loadPreviousFragment();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

	public static void loadNextFragment(Fragment fragment) {
		mFragmentManager.popBackStack();
		mFragmentManager.beginTransaction().add(R.id.container, fragment)
				.commit();
	}

	/*
	 * private OnBackStackChangedListener mOnBackStackChangeListener = new
	 * OnBackStackChangedListener() {
	 * 
	 * @Override public void onBackStackChanged() { if
	 * (mFragmentManager.getBackStackEntryCount() == 0) { Fragment fragment =
	 * mFragmentManager .findFragmentById(R.id.container); if (fragment != null
	 * && fragment instanceof RiskAssessmentFragment) { } } } };
	 */
}