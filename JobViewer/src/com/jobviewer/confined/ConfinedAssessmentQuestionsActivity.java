package com.jobviewer.confined;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.jobviewer.db.objects.SurveyJson;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.QuestionMaster;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.SurveyUtil;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.fragment.CheckTypeFragment;
import com.lanesgroup.jobviewer.fragment.CheckTypeFragment.onClicksEnterJobNumber;
import com.lanesgroup.jobviewer.fragment.RiskAssessmentFragment.onClicksRiskAssessment;

public class ConfinedAssessmentQuestionsActivity extends Activity implements
		onClicksRiskAssessment, onClicksEnterJobNumber {

	private static FragmentManager mFragmentManager;
	ConfinedQuestionManager manager;
	QuestionMaster loadJsonFromAssets;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.questions_flow_screen);
		Utils.startService(this);
		mFragmentManager = getFragmentManager();
		manager = ConfinedQuestionManager.getInstance();
		SurveyJson questionSet = JobViewerDBHandler
				.getConfinedQuestionSet(this);
		updateFragments(questionSet);
	}

	private void updateFragments(SurveyJson questionSet) {
		if (questionSet != null
				&& !Utils.isNullOrEmpty(questionSet.getQuestionJson())) {
			manager.reloadAssessment(questionSet);
		} else {
			loadJsonFromAssets = SurveyUtil.loadJsonFromAssets(this,
					"confined_space_entry.json");

			manager.setQuestionMaster(loadJsonFromAssets);
			Screen firstScreen = manager.getFirstScreen();
			int questionType = SurveyUtil.getQuestionType(firstScreen
					.get_type());
			loadFragment(SurveyUtil.getConfinedFragment(questionType));
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
			ConfinedQuestionManager.getInstance().loadPreviousFragment();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

	public static void loadNextFragment(Fragment fragment) {
		mFragmentManager.popBackStack();
		mFragmentManager.beginTransaction().add(R.id.container, fragment)
				.commit();
	}
}