package com.lanesgroup.jobviewer;

import com.lanesgroup.jobviewer.fragment.ShoutOutActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;


public class ShoutOptionsActivity extends BaseActivity implements OnCheckedChangeListener, OnClickListener {

	private Button mSave, mNext;
	private RadioButton mHazard, mIdea, mSafety;
	private Context mContext;
	private String mOption;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shout_out_option_activity);
        initUI();
        
    }
    
    private void initUI() {
    	mContext = this;
    	mSave = (Button) findViewById(R.id.button1);
    	mSave.setOnClickListener(this);
    	mNext = (Button) findViewById(R.id.button2);
    	
    	mHazard = (RadioButton) findViewById(R.id.radio_hazard);
    	mHazard.setOnCheckedChangeListener(this);
    	mIdea = (RadioButton) findViewById(R.id.radio_idea);
    	mIdea.setOnCheckedChangeListener(this);
    	mSafety = (RadioButton) findViewById(R.id.radio_safety);
    	mSafety.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mOption = (String) buttonView.getTag();
		mNext.setOnClickListener(this);
		mNext.setBackgroundResource(R.drawable.red_background);
	}
	

	@Override
	public void onClick(View view) {
		Intent intent = new Intent();
		if(view == mSave){
			
		}else if (view == mNext){
			intent.putExtra("ShoutOption", mOption);
			intent.setClass(ShoutOptionsActivity.this, ShoutOutActivity.class);
			startActivity(intent);
		}
		
	}
}
