package com.jobviewer.network;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Utils;
import com.jobviwer.request.object.Data;
import com.jobviwer.request.object.Payload;
import com.jobviwer.request.object.SyncRequest;
import com.vehicle.communicator.HttpConnection;

public class SyncAllData {
	List<BackLogRequest> allBackLog;
	Context context;

	public void sendAllData(Context context) {
		this.context = context;
		allBackLog = JobViewerDBHandler.getAllBackLog(context);
		createAndSendRequestToServer();
	}

	private void createAndSendRequestToServer() {
		SyncRequest request = new SyncRequest();
		Data[] dataArray = null;
		if (allBackLog != null && allBackLog.size() > 0) {
			dataArray = new Data[allBackLog.size()];
			for (int i = 0; i < allBackLog.size(); i++) {
				Data data = new Data();
				data.setEntity(allBackLog.get(i).getRequestType());
				data.setEntity("store");
				Payload payload = new Payload();
				payload.setJsonString(allBackLog.get(i).getRequestJson());
				data.setPayload(payload);
				dataArray[i] = data;
			}
			request.setData(dataArray);
			sendDataToServer(request);
		}
	}

	private void sendDataToServer(SyncRequest request) {
		String encodeToJsonString = GsonConverter.getInstance()
				.encodeToJsonString(request);
		ContentValues values = new ContentValues();
		values.put("data", encodeToJsonString);

		Utils.SendHTTPRequest(context, CommsConstant.HOST
				+ CommsConstant.SYNC_API, values, getSyncHandler());

	}

	private Handler getSyncHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					String result = (String) msg.obj;
					Log.i("Android", result);
					break;
				case HttpConnection.DID_ERROR:
					String result1 = (String) msg.obj;
					Log.i("Android", result1);
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

}
