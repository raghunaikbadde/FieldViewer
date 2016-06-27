package com.lanesgroup.jobviewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.SurveyJson;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.Images;
import com.jobviewer.survey.object.QuestionMaster;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.fragment.QuestionsActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UpdateRiskAssessmentActivity extends BaseActivity implements
        OnClickListener {

    private Button mCancel, mNext;
    private boolean mCancelButtonClicked = false;
    private ListView listView;
    private String QuestionTAG = "question";
    private String AnswerTag = "answer";
    private String ScreenId = "ScreenId";
    private String selectedScreenId = "";
    private List<String> backStackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_risk_assessment_screen);
        initUI();
        updateData();
    }

    private void updateData() {
        SurveyJson questionSet;
        try {
            questionSet = JobViewerDBHandler.getQuestionSet(this);
        } catch (Exception e) {
            Log.d("JV", "Question set cannot be accessed - " + e.toString());
            e.printStackTrace();
            return;
        }
        if (questionSet == null) {
            Log.d("JV", "Question set is returned null");
            return;
        }
        backStackList = new ArrayList<String>(Arrays.asList(questionSet
                .getBackStack().split(",")));
        QuestionMaster questionMaster = GsonConverter.getInstance()
                .decodeFromJsonString(questionSet.getQuestionJson(),
                        QuestionMaster.class);
        QuestionManager.getInstance().setQuestionMaster(questionMaster);
        Screen[] screens = questionMaster.getScreens().getScreen();
        ArrayList<HashMap<String, String>> questionsAndAnswers = new ArrayList<HashMap<String, String>>();
        Log.d("JV", "number of screens" + screens.length);
        for (Screen screen : screens) {
            try {
                if (isScreenDisplayed(screen.get_number())) {
                    String type = screen.get_type();
                    String question = screen.getText();
                    String screenId = screen.get_number();
                    if (type.equalsIgnoreCase("yesno")) {
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put(QuestionTAG, question);
                        String answer = screen.getAnswer();
                        hashMap.put(AnswerTag, answer);
                        hashMap.put(ScreenId, screenId);
                        questionsAndAnswers.add(hashMap);
                    } else {
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put(QuestionTAG, question);
                        hashMap.put(AnswerTag, "N/A");
                        hashMap.put(ScreenId, screenId);
                        questionsAndAnswers.add(hashMap);
                    }
                    Log.d("JV", "type " + type + " question " + question);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        QuestionSetAdapter questionSetAdapter = new QuestionSetAdapter(this,
                questionsAndAnswers);
        listView.setAdapter(questionSetAdapter);
    }

    private boolean isScreenDisplayed(String get_number) {
        for (int i = 0; i < backStackList.size(); i++) {
            if (backStackList.get(i).equalsIgnoreCase(get_number)) {
                return true;
            }
        }
        return false;
    }

    private void initUI() {
        mCancel = (Button) findViewById(R.id.button1);
        mNext = (Button) findViewById(R.id.button2);
        listView = (ListView) findViewById(R.id.listView);
        mCancel.setOnClickListener(this);
        mNext.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        if (mCancelButtonClicked) {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        mCancelButtonClicked = false;
        if (v == mCancel) {
            mCancelButtonClicked = true;
            onBackPressed();
        } else if (v == mNext) {
            Intent intent = new Intent(v.getContext(), QuestionsActivity.class);
            intent.putExtra(Utils.UPDATE_RISK_ASSESSMENT_ACTIVITY,
                    selectedScreenId);
            deleteAnsweredQuestionsFromQuestionSet(selectedScreenId);
            startActivity(intent);
        }
    }

    public void enableNextButton(boolean isEnable) {
        if (isEnable) {
            mNext.setEnabled(true);
            mNext.setBackgroundResource(R.drawable.red_background);
        } else {
            mNext.setEnabled(false);
            mNext.setBackgroundResource(R.drawable.dark_grey_background);
        }

    }

    private void deleteAnsweredQuestionsFromQuestionSet(String screenId) {
        SurveyJson surveyJSON;
        try {
            surveyJSON = JobViewerDBHandler.getQuestionSet(this);
        } catch (Exception e) {
            Log.d("JV", "Question set cannot be accessed - " + e.toString());
            e.printStackTrace();
            return;
        }
        if (surveyJSON == null) {
            Log.d("JV", "Question set is returned null");
            return;
        }

        QuestionMaster questionMaster = GsonConverter.getInstance()
                .decodeFromJsonString(surveyJSON.getQuestionJson(),
                        QuestionMaster.class);
        QuestionManager.getInstance().setQuestionMaster(questionMaster);
        Screen[] screens = questionMaster.getScreens().getScreen();

        Log.d("JV", "number of screens" + screens.length);
        CheckOutObject checkOutRemember = JobViewerDBHandler
                .getCheckOutRemember(this);
        checkOutRemember.setJobStartedTime(Utils.getCurrentDateAndTime());
        JobViewerDBHandler.saveCheckOutRemember(context, checkOutRemember);

        boolean questionAttempted = false;
        for (Screen screen : screens) {

            if (screen.get_number().equalsIgnoreCase(screenId)) {
                questionAttempted = true;
            }

            if (questionAttempted) {
                screen.setAnswer("");
                if (!"yesno".equalsIgnoreCase(screen.get_type())) {
                    Images[] images = new Images[1];
                    Images image = new Images();
                    image.setTemp_id("");
                    images[0] = image;
                    screen.setImages(images);
                }

                QuestionManager.getInstance().updateScreenOnQuestionMaster(
                        screen);
            }
        }
        QuestionManager.getInstance().saveAssessment(surveyJSON.getWorkType());
    }

    private class QuestionSetAdapter extends BaseAdapter {
        Context mContext;
        ArrayList<HashMap<String, String>> questionsAndAnswers;
        String selectedRadioText = "";

        public QuestionSetAdapter(Context mContext,
                                  ArrayList<HashMap<String, String>> questionsAndAnswers) {
            this.mContext = mContext;
            this.questionsAndAnswers = questionsAndAnswers;
        }

        @Override
        public int getCount() {
            return questionsAndAnswers.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.update_work_assessment_questions_list, null);
                viewHolder = new ViewHolder(convertView);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            HashMap<String, String> hashMap = questionsAndAnswers.get(position);
            viewHolder.radioButton.setText(hashMap.get(QuestionTAG));
            viewHolder.answerText.setText(hashMap.get(AnswerTag));
            viewHolder.screenId = hashMap.get(ScreenId);

            viewHolder.radioButton.setTag(hashMap);

            viewHolder.radioButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) v
                            .getTag();
                    selectedRadioText = hashMap.get(QuestionTAG);
                    notifyDataSetChanged();
                    enableNextButton(true);
                    selectedScreenId = hashMap.get(ScreenId);
                    Log.d("JV", "selectedRadioText " + selectedRadioText);
                }
            });

            if (viewHolder.radioButton.getText().toString()
                    .equalsIgnoreCase(selectedRadioText)) {
                viewHolder.radioButton.setChecked(true);
            } else {
                viewHolder.radioButton.setChecked(false);
            }
            convertView.setTag(viewHolder);

            return convertView;
        }

        private class ViewHolder {
            public RadioButton radioButton;
            public TextView answerText;
            public String screenId = "";

            public ViewHolder(View convertView) {
                radioButton = (RadioButton) convertView
                        .findViewById(R.id.radioButton1);
                answerText = (TextView) convertView
                        .findViewById(R.id.answeredText);
            }
        }
    }
}
