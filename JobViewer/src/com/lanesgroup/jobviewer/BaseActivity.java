package com.lanesgroup.jobviewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

public class BaseActivity extends AppCompatActivity {
	public static Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		context = this;
		getSupportActionBar().hide();
	}
	public static void exitApplication(BaseActivity baseActivity) {
		Intent intent = new Intent(context, WelcomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Exit me", true);
		context.startActivity(intent);
		baseActivity.finish();
	}
}
