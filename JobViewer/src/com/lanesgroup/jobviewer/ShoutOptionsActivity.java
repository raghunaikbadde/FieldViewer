package com.lanesgroup.jobviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.jobviewer.util.ActivityConstants;
import com.lanesgroup.jobviewer.fragment.ShoutOutActivity;

public class ShoutOptionsActivity extends BaseActivity implements
		OnClickListener, OnCheckedChangeListener {

	private Button mSave, mNext;
	RadioGroup radioGroup1;
	private String mOption;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shout_out_option_activity);
        initUI();
        
    }
    
	private void initUI() {
		mSave = (Button) findViewById(R.id.button1);
		mSave.setOnClickListener(this);
		mNext = (Button) findViewById(R.id.button2);
		radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
		radioGroup1.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View view) {
		Intent intent = new Intent();

		if (view == mSave) {

		} else if (view == mNext) {
			intent.putExtra(ActivityConstants.SHOUT_OPTION, mOption);
			intent.setClass(ShoutOptionsActivity.this, ShoutOutActivity.class);
			startActivity(intent);
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == R.id.radio_hazard) {
			mOption = ActivityConstants.HAZARD;
		} else if (checkedId == R.id.radio_idea) {
			mOption = ActivityConstants.IDEA;
		} else {
			mOption = ActivityConstants.SAFETY;
		}
		mNext.setOnClickListener(this);
		mNext.setBackgroundResource(R.drawable.red_background);
	}
}
