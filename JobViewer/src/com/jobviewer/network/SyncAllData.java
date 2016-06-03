package com.jobviewer.network;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;
import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.JsonIOException;
import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Utils;
import com.jobviwer.request.object.Data;
import com.jobviwer.request.object.Payload;
import com.jobviwer.request.object.SyncRequest;
import com.jobviwer.response.object.JVResponse;
import com.jobviwer.response.object.StartUpResponse;
import com.vehicle.communicator.HttpConnection;

public class SyncAllData {
	List<BackLogRequest> allBackLog;
	Context context;
	boolean isStartUpResponseArrived = true;
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
				String apiName = allBackLog.get(i).getRequestApi();
		
				int length = apiName.split("/").length;
				Data data = new Data();
				String entityType = apiName.split("/")[length-2];
				data.setEntity(entityType);
				String action = apiName.split("/")[length-1];
				if(action.equalsIgnoreCase("null")){
					entityType = apiName.split("/")[length-3];
					action = apiName.split("/")[length-2];
					data.setEntity(entityType);
				}
				data.setAction(action);
				String requestJson = allBackLog.get(i).getRequestJson();
				data.setPayload(requestJson);
				dataArray[i] = data;
				if(apiName.contains(CommsConstant.STARTUP_API)){
					Data[] data1 = new Data[1];
					data1[0] = data;
					data.setAction("");
					data.setEntity("startup");
					
					request.setData(data1);
					sendStartUpRequestStoreData(request);
					
					continue;
				}/*else if(apiName.contains(CommsConstant.WORK_CREATE_API)){
					Data[] data1 = new Data[1];
					data1[0] = data;
					
					request.setData(data1);
					sendWorkCreateData(request);
					
					continue;
				}*/else {
						
				}			
			}
			request.setData(dataArray);
			sendDataToServer(request);
		}
	}


	private void sendStartUpRequestStoreData(SyncRequest request) {
		
		String encodeToJsonString = GsonConverter.getInstance()
				.encodeToJsonString(request.getData());
		try{
			JSONObject jsonObject = new JSONObject(encodeToJsonString);
			//jsonObject.put("data", jsonObject.toString());
			encodeToJsonString = GsonConverter.getInstance()
					.encodeToJsonString(jsonObject);
		}catch(JSONException jsoe){
			
		}
		
		ContentValues values = new ContentValues();
		values.put("data", encodeToJsonString);
		isStartUpResponseArrived = false;
		Utils.SendHTTPRequest(context, CommsConstant.HOST
				+ CommsConstant.SYNC_API, values, getStartUpSyncHandler());
	}
	
	private void sendWorkCreateData(SyncRequest request) {
		
		String encodeToJsonString = GsonConverter.getInstance()
				.encodeToJsonString(request.getData());
		try{
			JSONObject jsonObject = new JSONObject(encodeToJsonString);
			//jsonObject.put("data", jsonObject.toString());
			encodeToJsonString = GsonConverter.getInstance()
					.encodeToJsonString(jsonObject);
		}catch(JSONException jsoe){
			
		}
		
		ContentValues values = new ContentValues();
		values.put("data", encodeToJsonString);
		isStartUpResponseArrived = false;
		Utils.SendHTTPRequest(context, CommsConstant.HOST
				+ CommsConstant.SYNC_API, values, getWorkCreateHandler());
	}

	private Handler getWorkCreateHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {			
				isStartUpResponseArrived = true;
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					String result = (String) msg.obj;
					JVResponse decodeFromJsonString = GsonConverter
							.getInstance().decodeFromJsonString(result,
									JVResponse.class);
					CheckOutObject checkOutRemember = JobViewerDBHandler
							.getCheckOutRemember(context);
					checkOutRemember.setIsStartedTravel("true");
					checkOutRemember.setWorkId(decodeFromJsonString.getId());
					JobViewerDBHandler.saveCheckOutRemember(context,
							checkOutRemember);
					Utils.work_id = decodeFromJsonString.getId();
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
	private Handler getStartUpSyncHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {			
				isStartUpResponseArrived = true;
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					String result = (String) msg.obj;
					/*StartUpResponse decodeFromJsonString = GsonConverter
							.getInstance().decodeFromJsonString(result,
									StartUpResponse.class);
					JobViewerDBHandler.saveUserDetail(context,
							decodeFromJsonString.getData().getUser());*/
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

	private void sendDataToServer(SyncRequest request) {
		String encodeToJsonString = GsonConverter.getInstance()
				.encodeToJsonString(request.getData());
		try{
			JSONObject jsonObject = new JSONObject(encodeToJsonString);
			//jsonObject.put("data", jsonObject.toString());
			encodeToJsonString = GsonConverter.getInstance()
					.encodeToJsonString(jsonObject);
		}catch(JSONException jsoe){
			
		}
		ContentValues values = new ContentValues();
		values.put("data", encodeToJsonString);

		Utils.SendHTTPRequest(context, CommsConstant.HOST
				+ CommsConstant.SYNC_API, values, getSyncHandler());

	}
	
	private void sendDataToServer(JSONArray request) {
		JSONObject jsonObject = null;
		try{
			jsonObject = new JSONObject();
			jsonObject.put("data", request.toString());
		}catch(JSONException jse){
			jsonObject = new JSONObject();
		}
		ContentValues data = new ContentValues();
		data.put("data", jsonObject.toString());
		
		Utils.SendHTTPRequest(context, CommsConstant.HOST
				+ CommsConstant.SYNC_API, data, getSyncHandler());

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
