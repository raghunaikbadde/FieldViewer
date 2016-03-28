package com.lanesgroup.jobviewer;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
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

public class UpdateRiskAssessmentActivity extends BaseActivity implements
		OnClickListener {

	private Button mCancel, mSubmit;
	private boolean mCancelButtonClicked = false;
	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_risk_assessment_screen);
		initUI();
	}

	private void initUI() {
		mCancel = (Button) findViewById(R.id.button1);
		mSubmit = (Button) findViewById(R.id.button2);
		listView = (ListView) findViewById(R.id.listView);
		ArrayList<HashMap<String, String>> questionsAndAnswers = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("question1", "answer1");
		questionsAndAnswers.add(hashMap);
		HashMap<String, String> hashMap2 = new HashMap<String, String>();
		hashMap2.put("question2", "answer2");
		questionsAndAnswers.add(hashMap2);
		QuestionSetAdapter questionSetAdapter = new QuestionSetAdapter(this,
				questionsAndAnswers);
		listView.setAdapter(questionSetAdapter);
		mCancel.setOnClickListener(this);
		mSubmit.setOnClickListener(this);
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
		} else if (v == mSubmit) {

		}
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
			return 20;
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
			viewHolder.radioButton.setText("question fafafasf shfa jhffasfhah safha khyafafkj ksfdasfh" + position);
			viewHolder.answerText.setText("answer" + position);
			viewHolder.radioButton.setTag("question fafafasf shfa jhffasfhah safha khyafafkj ksfdasfh" + position);
	
			viewHolder.radioButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					selectedRadioText = v.getTag().toString();
					notifyDataSetChanged();
					Log.d("JV","selectedRadioText "+selectedRadioText);
				}
			});
			
			if (viewHolder.radioButton.getText().toString().equalsIgnoreCase(selectedRadioText)) {
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

			public ViewHolder(View convertView) {
				radioButton = (RadioButton) convertView
						.findViewById(R.id.radioButton1);
				answerText = (TextView) convertView
						.findViewById(R.id.answeredText);
			}
		}
	}
}
