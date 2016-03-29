package com.lanesgroup.jobviewer;

import android.app.Activity;
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

	public static void exitApplication() {
		Intent j = new Intent(Intent.ACTION_MAIN);
		j.addCategory(Intent.CATEGORY_HOME);
		j.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(j);
		((Activity) context).finish();
		System.exit(0);
	}
}
