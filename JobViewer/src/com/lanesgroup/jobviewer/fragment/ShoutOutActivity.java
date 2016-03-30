package com.lanesgroup.jobviewer.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;

import com.jobviewer.survey.object.QuestionMaster;
import com.jobviewer.survey.object.util.SurveyUtil;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.R;

public class ShoutOutActivity extends Activity {

	private static FragmentManager mFragmentManager;
	private static QuestionMaster questionMaster;
	private static String option;
	private static String startedAt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.questions_flow_screen);
		Utils.startService(this);

		option = getIntent().getExtras().get(ActivityConstants.SHOUT_OPTION)
				.toString();
		startedAt = Utils.getCurrentDateAndTime();

		mFragmentManager = getFragmentManager();

		if (option.equalsIgnoreCase(ActivityConstants.HAZARD)) {
			setQuestionMaster(SurveyUtil.loadJsonFromAssets(this,
					"shout_about_safety_hazard.json"));
		} else if (option.equalsIgnoreCase(ActivityConstants.IDEA)) {
			setQuestionMaster(SurveyUtil.loadJsonFromAssets(this,
					"shout_about_safety_idea.json"));
		} else if (option.equalsIgnoreCase(ActivityConstants.SAFETY)) {
			setQuestionMaster(SurveyUtil.loadJsonFromAssets(this,
					"shout_about_safety_good_safety.json"));
		}

		loadFragment(new ShoutOutMediaTextTypeFragment());
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
			finish();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

	public static void loadNextFragment(Fragment fragment) {
		mFragmentManager.popBackStack();
		mFragmentManager.beginTransaction().add(R.id.container, fragment)
				.commit();
	}

	public static String getOptionSelected() {
		return option;
	}

	public static String getStartedAt() {
		return startedAt;
	}

	public static QuestionMaster getQuestionMaster() {
		return questionMaster;
	}

	public void setQuestionMaster(QuestionMaster questionMaster) {
		this.questionMaster = questionMaster;
	}
}