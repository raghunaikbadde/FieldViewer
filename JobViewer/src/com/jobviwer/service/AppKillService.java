package com.jobviwer.service;

import com.jobviewer.provider.JobViewerDBHandler;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class AppKillService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("service created");
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		// TODO Auto-generated method stub
		System.out.println("onTaskRemoved");
		JobViewerDBHandler.deleteQuestionSet(getApplicationContext());
		super.onTaskRemoved(rootIntent);

	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		System.out.println("Service started");
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("Service is running");
			}
		}, 5000);
	}

}
