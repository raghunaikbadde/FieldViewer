package com.jobviewer.app;

import android.app.Application;

public class JobViewerApplication extends Application {
	private static ApplicationSubject applicationSubject;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public ApplicationSubject getApplicationSubject() {
		if (applicationSubject == null)
			applicationSubject = new ApplicationSubject();
		return applicationSubject;
	}
}
