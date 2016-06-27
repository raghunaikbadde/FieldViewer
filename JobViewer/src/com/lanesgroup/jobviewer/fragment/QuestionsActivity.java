package com.lanesgroup.jobviewer.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.SurveyJson;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.QuestionMaster;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.survey.object.util.SurveyUtil;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.fragment.CheckTypeFragment.onClicksEnterJobNumber;
import com.lanesgroup.jobviewer.fragment.RiskAssessmentFragment.onClicksRiskAssessment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionsActivity extends Activity implements
        onClicksRiskAssessment, onClicksEnterJobNumber {

    private static FragmentManager mFragmentManager;
    private QuestionManager manager;
    private QuestionMaster loadJsonFromAssets;

    public static void loadNextFragment(Fragment fragment) {
        mFragmentManager.popBackStack();
        mFragmentManager.beginTransaction().add(R.id.container, fragment)
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.questions_flow_screen);
        Utils.startService(this);
        mFragmentManager = getFragmentManager();
        manager = QuestionManager.getInstance();
        SurveyJson questionSet = JobViewerDBHandler.getQuestionSet(this);
        Bundle bundle = getIntent().getExtras();

        if (bundle != null
                && bundle.containsKey(Utils.UPDATE_RISK_ASSESSMENT_ACTIVITY)) {
            updateFragmentsFromRiskAssessment(questionSet,
                    bundle.getString(Utils.UPDATE_RISK_ASSESSMENT_ACTIVITY));
        } else {
            updateFragments(questionSet);
        }
    }

    private void updateFragments(SurveyJson questionSet) {
        if (questionSet != null
                && !Utils.isNullOrEmpty(questionSet.getQuestionJson())) {
            manager.reloadAssessment(questionSet);
        } else {
            CheckOutObject checkOutRemember = JobViewerDBHandler
                    .getCheckOutRemember(this);
            if (checkOutRemember.getAssessmentSelected().equalsIgnoreCase(
                    ActivityConstants.EXCAVATION)) {
                loadJsonFromAssets = SurveyUtil.loadJsonFromAssets(this,
                        "excavation.json");
            } else {
                loadJsonFromAssets = SurveyUtil.loadJsonFromAssets(this,
                        "non_excavation.json");
            }

            manager.setQuestionMaster(loadJsonFromAssets);
            Screen firstScreen = manager.getFirstScreen();
            int questionType = SurveyUtil.getQuestionType(firstScreen
                    .get_type());
            loadFragment(SurveyUtil.getFragment(questionType));
        }
    }

    private void updateFragmentsFromRiskAssessment(SurveyJson questionSet,
                                                   String screenId) {

        manager.setQuestionMaster(GsonConverter.getInstance()
                .decodeFromJsonString(questionSet.getQuestionJson(),
                        QuestionMaster.class));
        Screen screenToShow = manager.getScreenById(screenId);
        manager.setCurrentScreen(screenToShow);

        setBackStack(questionSet.getBackStack(), screenId);
        int questionType = SurveyUtil.getQuestionType(screenToShow.get_type());
        loadFragment(SurveyUtil.getFragment(questionType));

    }

    private void setBackStack(String backStack, String screenId) {
        ArrayList<String> arrayList = new ArrayList<String>(
                Arrays.asList(backStack.split(",")));
        List<String> updated = new ArrayList<String>();
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).equalsIgnoreCase(screenId)) {
                updated.add(arrayList.get(i));
                break;
            } else {
                updated.add(arrayList.get(i));
            }
        }

        manager.setBackStack(TextUtils.join(",", updated));

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
            Fragment f = getFragmentManager().findFragmentById(R.id.container);
            if (f instanceof StopFragment) {
                QuestionManager.getInstance().loadPreviousFragmentOnResume();
            } else
                QuestionManager.getInstance().loadPreviousFragment();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
}