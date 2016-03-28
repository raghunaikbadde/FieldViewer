package com.jobviewer.network;

import com.jobviewer.util.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean isConnected = NetworkUtil.getConnectivityStatusString(context);

		if (isConnected) {
			Utils.senImagesToserver(context);
		}

	}

}
