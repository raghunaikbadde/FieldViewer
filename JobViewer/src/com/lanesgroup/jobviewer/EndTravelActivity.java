package com.lanesgroup.jobviewer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.OverrideReasoneDialog;
import com.jobviewer.util.Utils;
import com.jobviewer.util.showTimeDialog;
import com.jobviewer.util.showTimeDialog.DialogCallback;
import com.jobviwer.request.object.TimeSheetRequest;
import com.vehicle.communicator.HttpConnection;

public class EndTravelActivity extends BaseActivity implements
        View.OnClickListener, DialogCallback {
    private Button mStartTravel;
    private TextView mTravelTime, mTitleText, mOverrideText;
    private String TRAVEL_STARTED = "Travel started: ";
    // private String ON_BREAK = "On Break";
    private String ENROUTE = "Enroute to work";
    // private String BREAK_TIME = "Break started: ";
    // private String END_BREAK = "End Break";
    private String END_TRAVEL = "End travel";
    // private boolean isBreakStarted;
    private Context mContext;
    private String eventType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_travel_screen);
        Utils.endTimeRequest = new TimeSheetRequest();
        mContext = this;
        mTitleText = (TextView) findViewById(R.id.enroute_to_work_text);
        mTravelTime = (TextView) findViewById(R.id.travel_start_time_text);
        mOverrideText = (TextView) findViewById(R.id.travel_override_time_text);

        mStartTravel = (Button) findViewById(R.id.end_travel);
        eventType = (String) getIntent().getExtras().get(Constants.STARTED);
        mTitleText.setText(ENROUTE);


        String actualTime = Utils.startTravelTimeRequest.getStarted_at();
        mTravelTime.setText(actualTime + " (System)");
        mStartTravel.setText(END_TRAVEL);

        mStartTravel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Utils.endTravelTimeRequest = new TimeSheetRequest();
                eventType = "End Travel";
                new showTimeDialog(mContext, EndTravelActivity.this,
                        "End Travel").show();
            }
        });

        if (isAlreadyOveridden()) {
            String jsonStr = JobViewerDBHandler
                    .getJSONFlagObject(EndTravelActivity.this);
            try {
                JSONObject flagJSON = new JSONObject(jsonStr);
                String actualTravelStartTime = flagJSON
                        .getString(Constants.ACTUAL_TRAVEL_START_TIME_FLAG_JSON);
                String overrrideTime = flagJSON
                        .getString(Constants.OVERRIDE_TRAVEL_START_TIME_FLAG_JSON);
                mTravelTime.setText(actualTravelStartTime + " (System)");
                mOverrideText.setVisibility(View.VISIBLE);
                mOverrideText.setText(overrrideTime + " (User)");
                mTravelTime.setTextColor(this.getResources().getColor(R.color.light_grey));
            } catch (JSONException jse) {
                jse.printStackTrace();
            }
        } else {
            if (getIntent().getExtras().get(Constants.OVERRIDE_TIME) != null) {
                String overtime = getIntent().getExtras()
                        .get(Constants.OVERRIDE_TIME).toString();
                String actualTravelStartTime = getIntent().getExtras()
                        .get(Constants.TIME).toString();
                mOverrideText.setVisibility(View.VISIBLE);
                mTravelTime.setText(actualTravelStartTime + " (System)");
                mOverrideText.setText(overtime + " (User)");
                mTravelTime.setTextColor(this.getResources().getColor(R.color.grey));
                saveOverrideAndActualTravelStartTimeInFlagDB(
                        actualTravelStartTime, overtime, ActivityConstants.TRUE);
            } else {
                saveOverrideAndActualTravelStartTimeInFlagDB(getIntent()
                                .getExtras().getString(Constants.TIME), getIntent()
                                .getExtras().getString(Constants.TIME),
                        ActivityConstants.FALSE);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RESULT_CODE_CHANGE_TIME
                && resultCode == RESULT_OK) {
            if (ActivityConstants.TRUE
                    .equalsIgnoreCase(Utils.endTravelTimeRequest
                            .getIs_overriden())) {
                Intent intent = new Intent(this, OverrideReasoneDialog.class);
                intent.putExtra("eventType", eventType);
                startActivityForResult(intent,
                        Constants.RESULT_CODE_OVERRIDE_COMMENT);
            }
        } else if (requestCode == Constants.RESULT_CODE_OVERRIDE_COMMENT
                && resultCode == RESULT_OK) {

            if (Utils.isInternetAvailable(mContext)) {
                executeEndTravelService();
            } else {
                Utils.saveTimeSheetInBackLogTable(EndTravelActivity.this,
                        Utils.endTravelTimeRequest,
                        CommsConstant.END_TRAVEL_API, Utils.REQUEST_TYPE_WORK);
                // savePaidEndTravelinBackLogDb();
                JobViewerDBHandler.saveTimeSheet(this,
                        Utils.endTravelTimeRequest, CommsConstant.HOST
                                + CommsConstant.END_TRAVEL_API);
                CheckOutObject checkOutRemember = JobViewerDBHandler
                        .getCheckOutRemember(mContext);
                checkOutRemember.setIsStartedTravel("");
                checkOutRemember.setIsTravelEnd("true");
                JobViewerDBHandler.saveCheckOutRemember(mContext,
                        checkOutRemember);

                Intent intent = new Intent(this, NewWorkActivity.class);
                CheckAndCcontinueNoWork(intent);
                startActivity(intent);
            }
        }
    }

    private void CheckAndCcontinueNoWork(Intent intent) {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(Constants.WORK_NO_PHOTOS)) {
            intent.putExtra(Constants.WORK_NO_PHOTOS, Constants.WORK_NO_PHOTOS);
        }
    }

    @Override
    public void onContinue() {
        if (Utils.isInternetAvailable(mContext)) {
            JobViewerDBHandler.saveTimeSheet(mContext,
                    Utils.endTravelTimeRequest, CommsConstant.END_TRAVEL_API);
            executeEndTravelService();
        } else {
            JobViewerDBHandler.saveTimeSheet(this, Utils.endTimeRequest,
                    CommsConstant.HOST + CommsConstant.END_TRAVEL_API);
            CheckOutObject checkOutRemember = JobViewerDBHandler
                    .getCheckOutRemember(mContext);
            checkOutRemember.setIsStartedTravel("");
            checkOutRemember.setIsTravelEnd("true");
            JobViewerDBHandler.saveCheckOutRemember(mContext, checkOutRemember);
            Utils.saveTimeSheetInBackLogTable(EndTravelActivity.this,
                    Utils.endTravelTimeRequest, CommsConstant.END_TRAVEL_API,
                    Utils.REQUEST_TYPE_WORK);
            Intent intent = new Intent(this, NewWorkActivity.class);
            CheckAndCcontinueNoWork(intent);
            startActivity(intent);
        }

    }

    private Handler getEndTravelHandler() {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HttpConnection.DID_SUCCEED:
                        Utils.StopProgress();
                        // String result = (String) msg.obj;
                        CheckOutObject checkOutRemember = JobViewerDBHandler
                                .getCheckOutRemember(mContext);
                        checkOutRemember.setIsStartedTravel("");
                        checkOutRemember.setIsTravelEnd("true");
                        JobViewerDBHandler.saveCheckOutRemember(mContext,
                                checkOutRemember);

                        String jsonStr = JobViewerDBHandler
                                .getJSONFlagObject(mContext);
                        try {
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            jsonObject
                                    .remove(Constants.ACTUAL_TRAVEL_START_TIME_FLAG_JSON);
                            jsonObject
                                    .remove(Constants.IS_TRAVEL_OVERRIDEN_FLAG_JSON);
                            jsonObject
                                    .remove(Constants.OVERRIDE_TRAVEL_START_TIME_FLAG_JSON);
                            JobViewerDBHandler.saveFlaginJSONObject(mContext,
                                    jsonObject.toString());
                        } catch (JSONException e) {
                            // TODO: handle exception
                        }

                        Intent intent = new Intent(mContext, NewWorkActivity.class);
                        CheckAndCcontinueNoWork(intent);
                        startActivity(intent);
                        break;
                    case HttpConnection.DID_ERROR:
                        Utils.StopProgress();
                        String error = (String) msg.obj;
                        VehicleException exception = GsonConverter
                                .getInstance()
                                .decodeFromJsonString(error, VehicleException.class);
                        ExceptionHandler.showException(mContext, exception, "Info");
                        Utils.saveTimeSheetInBackLogTable(EndTravelActivity.this,
                                Utils.endTravelTimeRequest,
                                CommsConstant.END_TRAVEL_API,
                                Utils.REQUEST_TYPE_WORK);
                        break;
                    default:
                        break;
                }
            }
        };
        return handler;
    }

    private void executeEndTravelService() {
        Utils.startProgress(mContext);
        ContentValues data = new ContentValues();
        data.put("started_at", Utils.endTravelTimeRequest.getStarted_at());
        data.put("record_for", Utils.endTravelTimeRequest.getRecord_for());
        data.put("is_inactive", Utils.endTravelTimeRequest.getIs_inactive());
        data.put("is_overriden", Utils.endTravelTimeRequest.getIs_overriden());
        data.put("override_reason",
                Utils.endTravelTimeRequest.getOverride_reason());
        data.put("override_comment",
                Utils.endTravelTimeRequest.getOverride_comment());
        data.put("override_timestamp",
                Utils.endTravelTimeRequest.getOverride_timestamp());
        data.put("reference_id", Utils.endTravelTimeRequest.getReference_id());
        data.put("user_id", Utils.endTravelTimeRequest.getUser_id());

        Utils.SendHTTPRequest(this, CommsConstant.HOST
                + CommsConstant.END_TRAVEL_API, data, getEndTravelHandler());

    }

    @Override
    public void onBackPressed() {
        closeApplication();
    }

    @Override
    public void onDismiss() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

    private void saveOverrideAndActualTravelStartTimeInFlagDB(
            String actualTime, String overridenTime, String isOVeridden) {
        String flagJSON = JobViewerDBHandler.getJSONFlagObject(mContext);
        try {
            JSONObject jsonObject = new JSONObject(flagJSON);
            jsonObject.put(Constants.ACTUAL_TRAVEL_START_TIME_FLAG_JSON,
                    actualTime);
            jsonObject.put(Constants.OVERRIDE_TRAVEL_START_TIME_FLAG_JSON,
                    overridenTime);
            jsonObject
                    .put(Constants.IS_TRAVEL_OVERRIDEN_FLAG_JSON, isOVeridden);
            JobViewerDBHandler.saveFlaginJSONObject(mContext,
                    jsonObject.toString());
        } catch (JSONException jse) {
            jse.printStackTrace();
        }

    }

    private boolean isAlreadyOveridden() {
        String flagJSON = JobViewerDBHandler.getJSONFlagObject(mContext);
        String isOveridden = "";
        try {
            JSONObject jsonObject = new JSONObject(flagJSON);
            isOveridden = jsonObject
                    .getString(Constants.IS_TRAVEL_OVERRIDEN_FLAG_JSON);

        } catch (JSONException jse) {
            jse.printStackTrace();
        }
        return isOveridden.equalsIgnoreCase(ActivityConstants.TRUE);
    }
}
