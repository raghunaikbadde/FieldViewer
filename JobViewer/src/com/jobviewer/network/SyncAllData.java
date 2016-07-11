package com.jobviewer.network;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.JobViewerSharedPref;
import com.jobviewer.util.Utils;
import com.jobviwer.request.object.Data;
import com.jobviwer.request.object.SyncRequest;
import com.jobviwer.response.object.StartUpResponse;
import com.vehicle.communicator.HttpConnection;

public class SyncAllData {
    private List<BackLogRequest> allBackLog;
    private Context context;
    private boolean isStartUpResponseArrived = true;
    private JobViewerSharedPref mSharedPref;

    public void sendAllData(Context context) {
        this.context = context;
        allBackLog = JobViewerDBHandler.getAllBackLog(context);
        mSharedPref = new JobViewerSharedPref();
        createAndSendRequestToServer();
    }

    private void createAndSendRequestToServer() {
        SyncRequest request = new SyncRequest();
        Data[] dataArray;

        if (allBackLog != null && allBackLog.size() > 0) {
            dataArray = new Data[allBackLog.size()];
            for (int i = 0; i < allBackLog.size(); i++) {
                String apiName = allBackLog.get(i).getRequestApi();

                int length = apiName.split("/").length;
                Data data = new Data();
                String entityType = apiName.split("/")[length - 2];
                data.setEntity(entityType);
                String action = apiName.split("/")[length - 1];
                if (action.equalsIgnoreCase("null")) {
                    entityType = apiName.split("/")[length - 3];
                    action = apiName.split("/")[length - 2];
                    data.setEntity(entityType);
                }
                data.setAction(action);
                String requestJson = allBackLog.get(i).getRequestJson();
                data.setPayload(requestJson);
                if (apiName.contains(CommsConstant.STARTUP_API)) {
                    data.setAction("");
                    data.setEntity("startup");
                }
                if (apiName.contains(CommsConstant.POLLUTION_REPORT_UPLOAD)) {
                    data.setAction("store");
                    data.setEntity("pollution");
                }
                if (apiName.contains(CommsConstant.WORK_PHOTO_UPLOAD)) {
                    data.setAction("upload");
                    data.setEntity("works");
                }
                /*
                 * if(apiName.contains(CommsConstant.STARTUP_API)){ Data[] data1
				 * = new Data[1]; data1[0] = data; data.setAction("");
				 * data.setEntity("startup");
				 * 
				 * request.setData(data1); sendStartUpRequestStoreData(request);
				 * 
				 * continue; }else
				 * if(apiName.contains(CommsConstant.WORK_CREATE_API)){ Data[]
				 * data1 = new Data[1]; data1[0] = data;
				 * 
				 * request.setData(data1); sendWorkCreateData(request);
				 * 
				 * continue; }else {
				 */
                dataArray[i] = data;
                // }
            }
            // Data[] dataa = removeNullElementsInDataArray(dataArray);
            request.setData(dataArray);
            sendDataToServer(request);
        }
    }

    private Data[] removeNullElementsInDataArray(Data[] dataArray) {
        int length = 0;
        for (Data mData : dataArray) {
            if (mData == null) {
                length++;
            }
        }
        int j = 0;
        Data[] dataArrayOfWithOutNull = new Data[dataArray.length - length];
        for (Data mData : dataArray) {
            if (mData != null) {
                dataArrayOfWithOutNull[j] = mData;
                j++;
            }
        }
        return dataArrayOfWithOutNull;
    }

    private void sendStartUpRequestStoreData(SyncRequest request) {

        String encodeToJsonString = GsonConverter.getInstance()
                .encodeToJsonString(request.getData());
        try {
            JSONObject jsonObject = new JSONObject(encodeToJsonString);
            // jsonObject.put("data", jsonObject.toString());
            encodeToJsonString = GsonConverter.getInstance()
                    .encodeToJsonString(jsonObject);
        } catch (JSONException jsoe) {
            jsoe.printStackTrace();
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
        try {
            JSONObject jsonObject = new JSONObject(encodeToJsonString);
            // jsonObject.put("data", jsonObject.toString());
            encodeToJsonString = GsonConverter.getInstance()
                    .encodeToJsonString(jsonObject);
        } catch (JSONException jsoe) {
            jsoe.printStackTrace();
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
                        int id = 0;
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            id = jsonObject.getInt("id");
                            // result =
                            // ((JSONObject)jsonObject.get("data")).toString();
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    /*
                     * JVResponse decodeFromJsonString = GsonConverter
					 * .getInstance().decodeFromJsonString(result,
					 * JVResponse.class);
					 */
                        CheckOutObject checkOutRemember = JobViewerDBHandler
                                .getCheckOutRemember(context);
                        // checkOutRemember.setIsStartedTravel("true");
                        checkOutRemember.setWorkId(String.valueOf(id));
                        JobViewerDBHandler.saveCheckOutRemember(context,
                                checkOutRemember);
                        mSharedPref.saveWorkId(context, id + "");
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
                        try {
                            JSONObject jsonObject = new JSONObject(result);

                            JSONObject dataJsonObject = jsonObject
                                    .getJSONObject("data");
                            // result = dataJsonObject.toString();
                            JSONObject daa = new JSONObject();
                            daa.put("data", dataJsonObject);
                            result = daa.toString();
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                        StartUpResponse decodeFromJsonString = GsonConverter
                                .getInstance().decodeFromJsonString(result,
                                        StartUpResponse.class);
                        JobViewerDBHandler.saveUserDetail(context,
                                decodeFromJsonString.getData().getUser());
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
        try {
            JSONArray jsonObject = new JSONArray(encodeToJsonString);
            // jsonObject.put("data", jsonObject.toString());
            encodeToJsonString = GsonConverter.getInstance()
                    .encodeToJsonString(jsonObject);
        } catch (JSONException jsoe) {
            jsoe.printStackTrace();
        }
        ContentValues values = new ContentValues();
        values.put("data", encodeToJsonString);

        Utils.SendHTTPRequest(context, CommsConstant.HOST
                + CommsConstant.SYNC_API, values, getSyncHandler());

    }

    private void sendDataToServer(JSONArray request) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("data", request.toString());
        } catch (JSONException jse) {
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

                        int id = 0;
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            id = jsonObject.getInt("id");
                            // result =
                            // ((JSONObject)jsonObject.get("data")).toString();
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    /*
                     * JVResponse decodeFromJsonString = GsonConverter
					 * .getInstance().decodeFromJsonString(result,
					 * JVResponse.class);
					 */
                        try {
                            CheckOutObject checkOutRemember = JobViewerDBHandler
                                    .getCheckOutRemember(context);
                            // checkOutRemember.setIsStartedTravel("true");
                            checkOutRemember.setWorkId(String.valueOf(id));
                            JobViewerDBHandler.saveCheckOutRemember(context,
                                    checkOutRemember);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mSharedPref.saveWorkId(context, id + "");

                        try {
                            JSONObject jsonObject = new JSONObject(result);

                            JSONObject dataJsonObject = jsonObject
                                    .getJSONObject("data");
                            // result = dataJsonObject.toString();
                            JSONObject daa = new JSONObject();
                            daa.put("data", dataJsonObject);
                            result = daa.toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            StartUpResponse decodeFromJsonString = GsonConverter
                                    .getInstance().decodeFromJsonString(result,
                                            StartUpResponse.class);
                            JobViewerDBHandler.saveUserDetail(context,
                                    decodeFromJsonString.getData().getUser());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        JobViewerDBHandler.deleteBackLog(context);

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
