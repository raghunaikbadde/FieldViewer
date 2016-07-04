package com.jobviewer.confined.fragment;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.confined.ConfinedQuestionManager;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.BreakShiftTravelCall;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.JobViewerSharedPref;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.ConfirmWorkStopDialog;
import com.lanesgroup.jobviewer.R;
import com.raghu.ShoutOutBackLogRequest;
import com.raghu.WorkRequest;
import com.vehicle.communicator.HttpConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class ConfinedNoCustomerStopFragment extends Fragment implements OnClickListener {

    private View mRootView;
    private Button mStopButton;
    private Button mResumeButton;
    private JobViewerSharedPref mSharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mRootView = inflater.inflate(R.layout.risk_assessment_stop_screen, container,
                false);
        mSharedPref = new JobViewerSharedPref();
        removePhoneKeypad();
        mStopButton = (Button) mRootView.findViewById(R.id.button1);
        mResumeButton = (Button) mRootView.findViewById(R.id.button2);
        mStopButton.setOnClickListener(this);
        mResumeButton.setOnClickListener(this);
        return mRootView;
    }

    public void removePhoneKeypad() {
        InputMethodManager inputManager = (InputMethodManager) mRootView
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        IBinder binder = mRootView.getWindowToken();
        inputManager.hideSoftInputFromWindow(binder,
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mStopButton.getId()) {
            // Popup dialog to report to field manager
            sendWorkCompletedToServer();
        } else if (v.getId() == mResumeButton.getId()) {

            ConfirmWorkStopDialog mDialog = new ConfirmWorkStopDialog(getActivity(), new ConfirmWorkStopDialog.ConfirmWorkStopDialogCallback() {
                @Override
                public void onConfirmButtonPressed() {
                    ConfinedQuestionManager.getInstance()
                            .loadPreviousFragmentOnResume();
                }

                @Override
                public void onConfirmDismiss() {

                }
            });
            mDialog.show();

        }
    }

    private void startEndMethod() {
        Intent appPageActivityIntent = new Intent(getActivity(),
                ActivityPageActivity.class);
        appPageActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(appPageActivityIntent);
    }

    private void sendWorkCompletedToServer() {
        if (Utils.isInternetAvailable(getActivity())) {
            ContentValues data = new ContentValues();
            CheckOutObject checkOutRemember = JobViewerDBHandler
                    .getCheckOutRemember(getActivity());
            User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
            CheckOutObject checkOutRemember2 = JobViewerDBHandler
                    .getCheckOutRemember(getActivity());
            insertWorkEndTimeIntoHoursCalculator();
            data.put("started_at", checkOutRemember2.getJobStartedTime());
            if (checkOutRemember.getVistecId() != null) {
                data.put("reference_id", checkOutRemember.getVistecId());
            } else {
                data.put("reference_id", "");
            }
            data.put("engineer_id", Utils.work_engineer_id);
            data.put("status", Utils.work_status_stopped);
            data.put("completed_at", Utils.getCurrentDateAndTime());
            data.put("activity_type", "work");
            if (Utils.isNullOrEmpty(Utils.work_flooding_status)) {
                data.put("flooding_status", "");
            } else
                data.put("flooding_status", Utils.work_flooding_status);
            data.put("DA_call_out", Utils.work_DA_call_out);
            data.put("is_redline_captured", false);
            GPSTracker tracker = new GPSTracker(getActivity());
            data.put("location_latitude", tracker.getLatitude());
            data.put("location_longitude", tracker.getLongitude());
            data.put("created_by", userProfile.getEmail());
            Utils.startProgress(getActivity());
            try {
                mSharedPref.saveWorkId(getActivity(), checkOutRemember.getWorkId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
                            + CommsConstant.WORK_UPDATE_API + "/" + mSharedPref.getSharedPref(getActivity()).getString(JobViewerSharedPref.KEY_WORK_ID, ""), data,
                    getWorkCompletedHandler());
        } else {
            Utils.startProgress(getActivity());
            saveCreatedWorkInBackLogDb();
            saveInBackLogDB();
            removeConfinedSpaceAsessementStartedTime(getActivity());
            JobViewerDBHandler.deleteConfinedQuestionSet(getActivity());
            JobViewerDBHandler.deleteWorkWithNoPhotosQuestionSet(getActivity());
            Utils.StopProgress();
            startEndMethod();
        }
    }

    private void saveCreatedWorkInBackLogDb() {
        CheckOutObject checkOutRemember = JobViewerDBHandler
                .getCheckOutRemember(getActivity());
        User userProfile = JobViewerDBHandler
                .getUserProfile(getActivity());
        WorkRequest workRequest = new WorkRequest();
        workRequest.setStarted_at(checkOutRemember.getJobStartedTime());
        Utils.lastest_work_started_at = Utils.getCurrentDateAndTime();
        if (checkOutRemember.getVistecId() != null) {
            workRequest.setReference_id(checkOutRemember.getVistecId());
        } else {
            workRequest.setReference_id("");
        }
        workRequest.setEngineer_id(Utils.work_engineer_id);
        workRequest.setStatus(Utils.work_status_stopped);
        workRequest.setCompleted_at(Utils.getCurrentDateAndTime());
        workRequest.setActivity_type("work");
        workRequest.setFlooding_status(Utils.work_flooding_status);
        workRequest.setDA_call_out(Utils.work_DA_call_out);
        workRequest.setIs_redline_captured(false);
        GPSTracker tracker = new GPSTracker(getActivity());
        workRequest.setLocation_latitude("" + tracker.getLatitude());
        workRequest.setLocation_longitude("" + tracker.getLongitude());
        workRequest.setCreated_by(userProfile.getEmail());
        BackLogRequest backLogRequest = new BackLogRequest();
        backLogRequest.setRequestApi(CommsConstant.HOST
                + CommsConstant.WORK_UPDATE_API + "/" + mSharedPref.getSharedPref(getActivity()).getString(JobViewerSharedPref.KEY_WORK_ID, ""));
        backLogRequest.setRequestClassName("WorkRequest");
        backLogRequest.setRequestJson(GsonConverter.getInstance().encodeToJsonString(workRequest));
        backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
        JobViewerDBHandler.saveBackLog(getActivity(), backLogRequest);
    }

    private void insertWorkEndTimeIntoHoursCalculator() {
        BreakShiftTravelCall breakShiftTravelCall = JobViewerDBHandler
                .getBreakShiftTravelCall(getActivity());
        breakShiftTravelCall.setWorkEndTime(String.valueOf(System
                .currentTimeMillis()));
        JobViewerDBHandler.saveBreakShiftTravelCall(getActivity(),
                breakShiftTravelCall);
    }

    private Handler getWorkCompletedHandler() {

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HttpConnection.DID_SUCCEED:
                        executeSendDataToServer();
                        break;
                    case HttpConnection.DID_ERROR:
                        Utils.StopProgress();
                        String error = (String) msg.obj;
                        VehicleException exception = GsonConverter
                                .getInstance()
                                .decodeFromJsonString(error, VehicleException.class);
                        ExceptionHandler.showException(getActivity(), exception,
                                "Info");
                        break;
                    default:
                        break;
                }
            }

        };
        return handler;
    }

    private void executeSendDataToServer() {
        ContentValues values = new ContentValues();
        CheckOutObject checkOutRemember2 = JobViewerDBHandler
                .getCheckOutRemember(getActivity());
        User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
        if (checkOutRemember2 == null
                || Utils.isNullOrEmpty(checkOutRemember2.getWorkId())) {
            values.put("work_id", "");
        } else
            values.put("work_id", checkOutRemember2.getWorkId());
        values.put("survey_type", checkOutRemember2.getAssessmentSelected());
        values.put("related_type", "Work");
        if (checkOutRemember2 == null
                || Utils.isNullOrEmpty(checkOutRemember2.getVistecId())) {
            values.put("related_type_reference", "");
        } else
            values.put("related_type_reference",
                    checkOutRemember2.getVistecId());
        try {
            values.put("started_at", getConfinedSpaceAsessementStartedTime(getActivity()));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        values.put("completed_at", Utils.getCurrentDateAndTime());

        values.put("created_by", userProfile.getEmail());
        //GPSTracker gpsTracker = new GPSTracker(getActivity());
        String encodeToJsonString = GsonConverter.getInstance()
                .encodeToJsonString(
                        ConfinedQuestionManager.getInstance().getQuestionMaster());
        values.put("survey_json", encodeToJsonString);
        Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
                + CommsConstant.SURVEY_STORE_API, values, getSendSurveyHandler());

    }

    private Handler getSendSurveyHandler() {

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HttpConnection.DID_SUCCEED:
                        Utils.StopProgress();
                        JobViewerDBHandler.deleteConfinedQuestionSet(getActivity());
                        JobViewerDBHandler.deleteWorkWithNoPhotosQuestionSet(getActivity());

                        getActivity().finish();
                        removeConfinedSpaceAsessementStartedTime(getActivity());
                        startEndMethod();
                        break;
                    case HttpConnection.DID_ERROR:
                        Utils.StopProgress();
                        String error = (String) msg.obj;
                        VehicleException exception = GsonConverter
                                .getInstance()
                                .decodeFromJsonString(error, VehicleException.class);
                        ExceptionHandler.showException(getActivity(), exception,
                                "Info");
                        break;

                    default:
                        break;
                }
            }
        };
        return handler;

    }

    private void saveInBackLogDB() {
        ShoutOutBackLogRequest shoutOutBackLogRequest = new ShoutOutBackLogRequest();
        CheckOutObject checkOutRemember2 = JobViewerDBHandler
                .getCheckOutRemember(getActivity());
        User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
        shoutOutBackLogRequest.setWork_id(checkOutRemember2.getWorkId());
        shoutOutBackLogRequest.setSurvey_type(checkOutRemember2
                .getAssessmentSelected());
        shoutOutBackLogRequest.setRelated_type("Work");
        shoutOutBackLogRequest.setRelated_type_reference(checkOutRemember2
                .getVistecId());
        shoutOutBackLogRequest.setStarted_at(checkOutRemember2
                .getJobStartedTime());
        shoutOutBackLogRequest.setCompleted_at(Utils.getCurrentDateAndTime());
        String encodeToJsonString = GsonConverter.getInstance()
                .encodeToJsonString(
                        QuestionManager.getInstance().getQuestionMaster());
        shoutOutBackLogRequest.setSurvey_json(encodeToJsonString);
        shoutOutBackLogRequest.setCreated_by(userProfile.getEmail());
        shoutOutBackLogRequest.setStatus(Utils.work_status_stopped);
        GPSTracker gpsTracker = new GPSTracker(getActivity());
        shoutOutBackLogRequest.setLocation_latitude(""
                + gpsTracker.getLatitude());
        shoutOutBackLogRequest.setLocation_longitude(""
                + gpsTracker.getLongitude());
        BackLogRequest backLogRequest = new BackLogRequest();
        backLogRequest.setRequestApi(CommsConstant.HOST
                + CommsConstant.SURVEY_STORE_API);
        backLogRequest.setRequestClassName("ShoutOutBackLogRequest");
        backLogRequest.setRequestJson(GsonConverter.getInstance()
                .encodeToJsonString(shoutOutBackLogRequest));
        backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
        JobViewerDBHandler.saveBackLog(getActivity(), backLogRequest);

    }

    private String getConfinedSpaceAsessementStartedTime(Context mContext) throws JSONException {
        String str = JobViewerDBHandler.getJSONFlagObject(mContext);
        if (Utils.isNullOrEmpty(str)) {
            str = "{}";
        }
        JSONObject jsonObject = new JSONObject(str);
        if (jsonObject.has(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME)) {
            return jsonObject.getString(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME);
        }
        CheckOutObject checkOutRemember2 = JobViewerDBHandler
                .getCheckOutRemember(getActivity());
        return checkOutRemember2.getJobStartedTime();

    }

    private void removeConfinedSpaceAsessementStartedTime(Context mContext) {
        String str = JobViewerDBHandler.getJSONFlagObject(mContext);
        if (Utils.isNullOrEmpty(str)) {
            str = "{}";
        }
        try {
            JSONObject jsonObject = new JSONObject(str);
            if (jsonObject.has(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME)) {
                jsonObject.remove(Constants.CONFINED_ENTRY_ENTRY_STARTED_TIME);
                String jsonString = jsonObject.toString();
                JobViewerDBHandler.saveFlaginJSONObject(getActivity(), jsonString);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

