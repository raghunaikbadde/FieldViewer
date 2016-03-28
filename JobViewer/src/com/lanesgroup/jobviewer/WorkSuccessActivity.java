package com.lanesgroup.jobviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WorkSuccessActivity extends BaseActivity implements OnClickListener{

	private Button mDoneButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.success_screen);
		initUI();
	}
	
	private void initUI(){
		mDoneButton = (Button) findViewById(R.id.done_button);
		mDoneButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == mDoneButton.getId()){
			Intent appPageActivityIntent = new Intent(WorkSuccessActivity.this,ActivityPageActivity.class);
			appPageActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
					|Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(appPageActivityIntent); 
		}
	}
}
