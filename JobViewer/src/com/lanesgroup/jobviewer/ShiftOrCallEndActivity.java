package com.lanesgroup.jobviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ShiftOrCallEndActivity extends BaseActivity implements OnClickListener{

	Button mCloseButton,mGoOnCallButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shift_complete_screen);
		
		initUI();
	}
	
	private void initUI(){
		mCloseButton = (Button) findViewById(R.id.button1);
		mGoOnCallButton = (Button) findViewById(R.id.button2);
		mCloseButton.setOnClickListener(this);
		mGoOnCallButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == mCloseButton){
			Intent intent = new Intent(ShiftOrCallEndActivity.this,WelcomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
		} else if (v == mGoOnCallButton){
			/*Intent intent = new Intent(ShiftOrCallEndActivity.this,ClockInConfirmationActivity.class);
			startActivity(intent);*/
			
		}
	}
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(ShiftOrCallEndActivity.this,WelcomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);
	}
}
