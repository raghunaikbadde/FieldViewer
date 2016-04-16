package com.jobviewer.survey.object.util;

import java.io.IOException;
import java.io.InputStream;

import android.app.Fragment;
import android.content.Context;
import android.util.Log;

import com.jobviewer.confined.fragment.ConfinedAssessmentCompleteFragment;
import com.jobviewer.confined.fragment.ConfinedCheckTypeFragment;
import com.jobviewer.confined.fragment.ConfinedEngineerFragment;
import com.jobviewer.confined.fragment.ConfinedInformationTypeFragment;
import com.jobviewer.confined.fragment.ConfinedMediaTextTypeFragment;
import com.jobviewer.confined.fragment.ConfinedMediaTypeFragment;
import com.jobviewer.confined.fragment.ConfinedStopFragment;
import com.jobviewer.confined.fragment.ConfinedTimerFragment;
import com.jobviewer.confined.fragment.ConfinedTimerWithMediaFragment;
import com.jobviewer.confined.fragment.ConfinedYesNoTypeFragment;
import com.jobviewer.nophotos.fragment.NoPhotosAssessmentCompleteFragment;
import com.jobviewer.nophotos.fragment.NoPhotosStopFragment;
import com.jobviewer.nophotos.fragment.NoPhotosYesNoTypeFragment;
import com.jobviewer.survey.object.QuestionMaster;
import com.lanesgroup.jobviewer.fragment.AssessmentCompleteFragment;
import com.lanesgroup.jobviewer.fragment.CheckTypeFragment;
import com.lanesgroup.jobviewer.fragment.InformationTypeFragment;
import com.lanesgroup.jobviewer.fragment.MediaTextTypeFragment;
import com.lanesgroup.jobviewer.fragment.MediaTypeFragment;
import com.lanesgroup.jobviewer.fragment.StopFragment;
import com.lanesgroup.jobviewer.fragment.YesNoTypeFragment;

public class SurveyUtil {

	public static QuestionMaster loadJsonFromAssets(Context context,
			String fileName) {
		QuestionMaster questionMaster = null;
		try {
			InputStream is = context.getAssets().open(fileName);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			String json = new String(buffer, "UTF-8");
			GsonConverter converter = new GsonConverter();
			questionMaster = converter.decodeFromJsonString(json,
					QuestionMaster.class);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return questionMaster;
	}

	public static int getQuestionType(String type) {
		Log.i("Android", "Question Type :" + type);
		if (type.equalsIgnoreCase("check")) {
			return 1;
		} else if (type.equalsIgnoreCase("media")) {
			return 2;
		} else if (type.equalsIgnoreCase("yesno")) {
			return 3;
		} else if (type.equalsIgnoreCase("media_with_input")) {
			return 4;
		} else if (type.equalsIgnoreCase("information")) {
			return 5;
		} else if (type.equalsIgnoreCase("__STOPSCREEN__")) {
			return 6;
		} else if (type.equalsIgnoreCase("__STOPSCREEN_CUSTOMER_NOT_PRESENT__")) {
			return 7;
		} else if (type.equalsIgnoreCase("COMPLETE")) {
			return 8;
		} else if (type.equalsIgnoreCase("timer")) {
			return 9;
		} else if (type.equalsIgnoreCase("inputs")) {
			return 10;
		} else if (type.equalsIgnoreCase("timer_with_media")) {
			return 11;
		} else {
			return 0;
		}
	}

	public static Fragment getFragment(int type) {
		switch (type) {
		case 1:
			return new CheckTypeFragment();
		case 2:
			return new MediaTypeFragment();
		case 3:
			return new YesNoTypeFragment();
		case 4:
			return new MediaTextTypeFragment();
		case 5:
			return new InformationTypeFragment();
		case 6:
			return new StopFragment();
		case 7:
			return new StopFragment();
		case 8:
			return new AssessmentCompleteFragment();
		default:
			return null;
		}
	}

	public static Fragment getConfinedFragment(int type) {
		switch (type) {
		case 1:
			return new ConfinedCheckTypeFragment();
		case 2:
			return new ConfinedMediaTypeFragment();
		case 3:
			return new ConfinedYesNoTypeFragment();
		case 4:
			return new ConfinedMediaTextTypeFragment();
		case 5:
			return new ConfinedInformationTypeFragment();
		case 6:
			return new ConfinedStopFragment();
		case 7:
			return new ConfinedStopFragment();
		case 8:
			return new ConfinedAssessmentCompleteFragment();
		case 9:
			return new ConfinedTimerFragment();
		case 10:
			return new ConfinedEngineerFragment();
		case 11:
			return new ConfinedTimerWithMediaFragment();
		default:
			return null;
		}
	}

	public static Fragment getNoPhotosFragment(int type) {
		switch (type) {
		case 3:
			return new NoPhotosYesNoTypeFragment();
		case 6:
			return new NoPhotosStopFragment();
		case 7:
			return new NoPhotosStopFragment();
		case 8:
			return new NoPhotosAssessmentCompleteFragment();
		default:
			return null;
		}
	}

}
