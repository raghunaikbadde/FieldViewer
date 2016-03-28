package com.lanesgroup.jobviewer;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class UpdateRiskAssessmentActivity extends BaseActivity implements OnClickListener{

	private Button mCancel,mSubmit;
	private boolean mCancelButtonClicked = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_risk_assessment_screen);
		initUI();
	}
	
	private void initUI(){
		mCancel = (Button)findViewById(R.id.button1);
		mSubmit = (Button)findViewById(R.id.button2);
		mCancel.setOnClickListener(this);
		mSubmit.setOnClickListener(this);
	}
	
	@Override
	public void onBackPressed() {
		if(mCancelButtonClicked){
			super.onBackPressed();
		}
	}
	
	@Override
	public void onClick(View v) {
		mCancelButtonClicked = false;
		if(v == mCancel){
			mCancelButtonClicked = true;
			onBackPressed();
		} else if(v == mSubmit){
			
		}
	}
	
}
