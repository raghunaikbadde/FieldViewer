package com.jobviewer.confined;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.widget.Toast;

import com.jobviewer.confined.fragment.ConfinedTimerWithMediaFragment;
import com.jobviewer.db.objects.SurveyJson;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.QuestionMaster;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.survey.object.util.SurveyUtil;
import com.jobviewer.util.ActivityConstants;
import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.MultipleInputActivity;
import com.lanesgroup.jobviewer.R;

public class ConfinedQuestionManager {

    private static QuestionMaster questionMaster;
    private static ConfinedQuestionManager questionManager;
    private static List<String> backStack = new ArrayList<String>();
    private Screen currentScreen;
    private String nextScreenId;
    private String WorkType;
    private Screen multipleTypeScreen;
    private boolean isBackPressed = false;

    public static ConfinedQuestionManager getInstance() {
        if (questionManager == null) {
            questionManager = new ConfinedQuestionManager();
        }
        return questionManager;
    }

    public Screen getScreenById(String id) {
        Screen nextScreen = null;
        Screen[] screenArray = questionMaster.getScreens().getScreen();
        for (Screen screen : screenArray) {
            if (id.equalsIgnoreCase(screen.get_number())) {
                nextScreen = screen;
                break;
            }
        }
        return nextScreen;
    }

    public void addToBackStack(String id) {
        boolean isIdAdded = checkIfIdAlreadyAdded(id);
        if (!isIdAdded) {
            backStack.add(id);
        }
    }

    private boolean checkIfIdAlreadyAdded(String id) {
        boolean isAdded = false;
        int location = 0;
        for (int i = 0; i < backStack.size(); i++) {
            if (id.equalsIgnoreCase(backStack.get(i))) {
                location = i;
                isAdded = true;
                break;
            }
        }

        if (isAdded) {
            for (int i = backStack.size() - 1; i > location; i--) {
                backStack.remove(i);

            }
        }
        return isAdded;
    }

    public void updateScreenOnQuestionMaster(Screen resultScreen) {
        for (int i = 0; i < questionMaster.getScreens().getScreen().length; i++) {
            if (questionMaster.getScreens().getScreen()[i].get_number()
                    .equalsIgnoreCase(resultScreen.get_number())) {
                questionMaster.getScreens().getScreen()[i] = resultScreen;
                break;
            }
        }

    }

    public Screen getFirstScreen() {
        setCurrentScreen(questionMaster.getScreens().getScreen()[0]);
        addToBackStack(questionMaster.getScreens().getScreen()[0].get_number());
        return questionMaster.getScreens().getScreen()[0];
    }

    public boolean isFirstScreen(Screen screen) {
        Screen firstScreen = questionMaster.getScreens().getScreen()[0];
        return screen.equals(firstScreen);
    }

    public QuestionMaster getQuestionMaster() {
        return questionMaster;
    }

    public void setQuestionMaster(QuestionMaster master) {
        questionMaster = master;
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }

    public void setCurrentScreen(Screen currentScreen) {
        this.currentScreen = currentScreen;
    }

    public String getNextScreenId() {
        return nextScreenId;
    }

    public void setNextScreenId(String nextScreenId) {
        this.nextScreenId = nextScreenId;
    }

	public void loadNextFragment(String screenId) {
        if (screenId.equalsIgnoreCase(ActivityConstants.STOP_SCREEN)) {
            int questionType = SurveyUtil.getQuestionType(screenId);
            ConfinedAssessmentQuestionsActivity.loadNextFragment(SurveyUtil
                    .getConfinedFragment(questionType));
        } else if (screenId
                .equalsIgnoreCase(ActivityConstants.STOP_SCREEN_CUSTOMER)) {
            int questionType = SurveyUtil.getQuestionType(screenId);
            ConfinedAssessmentQuestionsActivity.loadNextFragment(SurveyUtil
                    .getConfinedFragment(questionType));
        } else if (screenId
                .equalsIgnoreCase(ActivityConstants.ASSESSMENT_COMPLETE)) {
            int questionType = SurveyUtil.getQuestionType(screenId);
            ConfinedAssessmentQuestionsActivity.loadNextFragment(SurveyUtil
                    .getConfinedFragment(questionType));
        } else {
            for (int i = 0; i < questionMaster.getScreens().getScreen().length; i++) {
                if (screenId.equalsIgnoreCase(questionMaster.getScreens()
                        .getScreen()[i].get_number())) {
                    currentScreen = questionMaster.getScreens().getScreen()[i];
                    int questionType = SurveyUtil.getQuestionType(currentScreen
                            .get_type());
                    ConfinedAssessmentQuestionsActivity
                            .loadNextFragment(SurveyUtil
                                    .getConfinedFragment(questionType));
                    if (!isBackPressed) {
                        addToBackStack(screenId);
                    } else {
                        isBackPressed = false;
                    }
                    break;
                }
            }
        }

    }

    public void saveAssessment(String workType) {
        SurveyJson json = new SurveyJson();
        String encodeToJsonString = GsonConverter.getInstance()
                .encodeToJsonString(questionMaster);
        json.setQuestionJson(encodeToJsonString);
        json.setWorkType(workType);
        json.setBackStack(getBackStackAsString());
        JobViewerDBHandler.saveConfinedQuestionSet(BaseActivity.context, json);
    }

    public void reloadAssessment(SurveyJson surveyJson) {
        questionMaster = GsonConverter.getInstance().decodeFromJsonString(
                surveyJson.getQuestionJson(), QuestionMaster.class);
        setWorkType(surveyJson.getWorkType());
        setBackStack(surveyJson.getBackStack());
        isBackPressed = true;
        loadNextFragment(backStack.get(backStack.size() - 1));

    }

    public void setBackStack(String stack) {
        backStack = new ArrayList<String>(Arrays.asList(stack.split(",")));
    }

    public String getBackStackAsString() {
        return android.text.TextUtils.join(",", backStack);
    }

    public String getWorkType() {
        return WorkType;
    }

    public void setWorkType(String workType) {
        WorkType = workType;
    }

    public void loadPreviousFragment() {
        if (backStack != null && backStack.size() > 1) {
            String screenId = backStack.get(backStack.size() - 2);
            backStack.remove(backStack.size() - 1);
            isBackPressed = true;
            loadNextFragment(screenId);
        } else {
            Toast.makeText(
                    BaseActivity.context,
                    BaseActivity.context.getResources().getString(
                            R.string.noPreviousQuestionMessage),
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void loadPreviousFragmentOnResume() {
        if (backStack != null && backStack.size() >= 1) {
            String screenId = backStack.get(backStack.size() - 1);
            isBackPressed = true;
            loadNextFragment(screenId);
        } else {
            Screen screen = getFirstScreen();
            loadNextFragment(screen.get_number());
        }

    }

    public void showOntimerCompleteScreen(int on_timer_complete) {
        Screen screen = questionMaster.getScreens().getScreen()[on_timer_complete - 1];
        Intent intent = new Intent(BaseActivity.context,
                MultipleInputActivity.class);
        setMultipleTypeScreen(screen);
        BaseActivity.context.startActivity(intent);
    }

    public void UpdateMultipleScreen(Screen screen) {
        for (int i = 0; i < questionMaster.getScreens().getScreen().length; i++) {
            if (screen.get_number().equalsIgnoreCase(
                    questionMaster.getScreens().getScreen()[i].get_number())) {
                questionMaster.getScreens().getScreen()[i] = screen;
                break;
            }
        }
        ConfinedTimerWithMediaFragment.restartTimer();
    }

    public Screen getMultipleTypeScreen() {
        return multipleTypeScreen;
    }

    public void setMultipleTypeScreen(Screen multipleTypeScreen) {
        this.multipleTypeScreen = multipleTypeScreen;
    }
}
