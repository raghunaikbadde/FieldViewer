package com.jobviewer.survey.object.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.widget.Toast;

import com.jobviewer.db.objects.SurveyJson;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.QuestionMaster;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.util.ActivityConstants;
import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.fragment.QuestionsActivity;

public class QuestionManager {

	private static QuestionMaster questionMaster;
	private static QuestionManager questionManager;
	private static List<String> backStack = new ArrayList<String>();
	private Screen currentScreen;
	private String nextScreenId;
	private String WorkType;
	boolean isBackPressed = false;

	public static QuestionManager getInstance() {
		if (questionManager == null) {
			questionManager = new QuestionManager();
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

	public void setQuestionMaster(QuestionMaster master) {
		questionMaster = master;
	}

	public QuestionMaster getQuestionMaster() {
		return questionMaster;
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
			QuestionsActivity.loadNextFragment(SurveyUtil
					.getFragment(questionType));
		} else if (screenId
				.equalsIgnoreCase(ActivityConstants.STOP_SCREEN_CUSTOMER)) {
			int questionType = SurveyUtil.getQuestionType(screenId);
			QuestionsActivity.loadNextFragment(SurveyUtil
					.getFragment(questionType));
		} else if (screenId
				.equalsIgnoreCase(ActivityConstants.ASSESSMENT_COMPLETE)) {
			int questionType = SurveyUtil.getQuestionType(screenId);
			QuestionsActivity.loadNextFragment(SurveyUtil
					.getFragment(questionType));
		} else {
			for (int i = 0; i < questionMaster.getScreens().getScreen().length; i++) {
				if (screenId.equalsIgnoreCase(questionMaster.getScreens()
						.getScreen()[i].get_number())) {
					currentScreen = questionMaster.getScreens().getScreen()[i];
					int questionType = SurveyUtil.getQuestionType(currentScreen
							.get_type());
					QuestionsActivity.loadNextFragment(SurveyUtil
							.getFragment(questionType));
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
		JobViewerDBHandler.saveQuestionSet(BaseActivity.context, json);
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
		if (backStack != null && backStack.size() > 1) {
			String screenId = backStack.get(backStack.size() - 1);
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
}
