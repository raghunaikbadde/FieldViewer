package com.jobviewer.network;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SendImageService extends Service {
	private final LocalBinder mBinder = new LocalBinder();
	protected Handler handler;
	protected Toast mToast;

	public class LocalBinder extends Binder {
		public SendImageService getService() {
			return SendImageService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
		handler.post(new Runnable() {

			@Override
			public void run() {
				SyncAllData syncAllData=new SyncAllData();
				syncAllData.sendAllData(getApplicationContext());
				SendImagesOnBackground sendImagesOnBackground = new SendImagesOnBackground();
				sendImagesOnBackground
						.getAndSendImagesToServer(getApplicationContext());
				// Utils.sendImagesToserver(getApplicationContext());
				Log.i("Andriod", "Service Started");
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return android.app.Service.START_STICKY;
	}

}
