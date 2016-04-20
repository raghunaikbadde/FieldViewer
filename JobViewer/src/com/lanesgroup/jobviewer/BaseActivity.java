package com.lanesgroup.jobviewer;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.jobviewer.app.JobViewerApplication;
import com.jobviewer.app.Subject;
import com.jobviewer.app.Subject.Observer;

public class BaseActivity extends AppCompatActivity implements Observer {
	public static Context context;
	public static JobViewerApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		context = this;

		application = (JobViewerApplication) this.getApplication();
		application.getApplicationSubject().attach(this);
		getSupportActionBar().hide();
	}

	@Override
	public void update(Subject subject) {
		this.finish();
	}
	
	@Override
	public void finish() {
		application.getApplicationSubject().detach(this);
		super.finish();
	}

	public void closeApplication() {
		application.getApplicationSubject().exit();
	}
	
	public void goBackToStartScreenFromShoutOut(){
		application.getApplicationSubject().goToStartUpScreenFromShoutOut();
	}
}
