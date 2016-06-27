package com.jobviewer.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.StartTrainingObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ConfirmDialog.ConfirmDialogCallback;
import com.jobviewer.util.ConfirmDialog.ConfirmDialogCallbackForNoPhotos;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.NewWorkActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.TravelToWorkSiteActivity;
import com.lanesgroup.jobviewer.TravelToWorkSiteOrArriveAtSiteActivity;
import com.vehicle.communicator.HttpConnection;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

public class SelectActivityDialog extends Activity implements
        ConfirmDialogCallback, ConfirmDialogCallbackForNoPhotos {

    private final String WORK = "Work";
    private final String WORK_NO_PHOTOS = "WorkNoPhotos";
    private final String TRAINING = "Training";
    private final ArrayList<HashMap<String, Object>> m_data = new ArrayList<HashMap<String, Object>>();
    private Button dialog_ok;
    private LinearLayout dialogBoxLayout;
    /*
     * private CheckBox mWork, mWorkNoPhotos, mTraining; private String
     * selected; private OnCheckedChangeListener checkChangedListner; private
     * Button start, cancel;
     */
    private Context mContext;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.select_dialog);
        mContext = this;
        dialog_ok = (Button) findViewById(R.id.dialog_ok);
        dialogBoxLayout = (LinearLayout) findViewById(R.id.select_dialog_layout);
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        map1.put("maintext", R.drawable.work_camera_icon);
        map1.put("subtext", "Work");
        m_data.add(map1);

        HashMap<String, Object> map2 = new HashMap<String, Object>();
        map2.put("maintext", R.drawable.work_nophotos_user_icon);
        map2.put("subtext", "Work (no photos or data)");// no small text of this
        // item!
        m_data.add(map2);

        HashMap<String, Object> map3 = new HashMap<String, Object>();
        map3.put("maintext", R.drawable.training_book_icon);
        map3.put("subtext", "Training / Toolbox Talk");
        m_data.add(map3);

        for (HashMap<String, Object> m : m_data)
            // make data of this view should not be null (hide )
            m.put("checked", false);
        // end init data

        final ListView lv = (ListView) findViewById(R.id.listview);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final SimpleAdapter adapter = new SimpleAdapter(this, m_data,
                R.layout.dialog_list_layout, new String[]{"maintext",
                "subtext", "checked"}, new int[]{R.id.oncall_image,
                R.id.oncall_text, R.id.checkBox2});

        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                RadioButton rb = (RadioButton) view
                        .findViewById(R.id.checkBox2);

                if (!rb.isChecked()) // OFF->ON
                {
                    for (HashMap<String, Object> m : m_data)
                        // clean previous selected
                        m.put("checked", false);

                    m_data.get(position).put("checked", true);
                    adapter.notifyDataSetChanged();
                    updateOkButtonDrawable(true);
                } else {
                    for (HashMap<String, Object> m : m_data)
                        // clean previous selected
                        m.put("checked", false);

//                    m_data.get(position).put("checked", rb.isChecked());
                    adapter.notifyDataSetChanged();
                    updateOkButtonDrawable(false);
                }
            }
        });
        updateOkButtonDrawable(false);

        // show result
        findViewById(R.id.dialog_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected = -1;
                Intent intent = new Intent();
                for (int i = 0; i < m_data.size(); i++) // clean
                // previous
                // selected
                {
                    HashMap<String, Object> m = m_data.get(i);
                    Boolean x = (Boolean) m.get("checked");
                    if (x == true) {
                        selected = i;
                        break; // break, since it's a single choice list
                    }
                }
                if (selected == -1)
                    return;
                else if (selected == 0) {
                    CheckOutObject checkOutRemember = JobViewerDBHandler
                            .getCheckOutRemember(v.getContext());
                    if (checkOutRemember != null
                            && ActivityConstants.TRUE
                            .equalsIgnoreCase(checkOutRemember
                                    .getIsTravelEnd())) {
                    	if(shouldNewWorkTravelArrivedSite(mContext)){
                    		intent.setClass(SelectActivityDialog.this,
                                    TravelToWorkSiteOrArriveAtSiteActivity.class);
                    	}else{
                    		intent.setClass(SelectActivityDialog.this,
                                NewWorkActivity.class);
                    	}
                    } else {
                        intent.setClass(SelectActivityDialog.this,
                                TravelToWorkSiteActivity.class);
                    }
//                    result = WORK;
                    startActivity(intent);
                    finish();

                } else if (selected == 1) {

                    new ConfirmDialog(mContext, SelectActivityDialog.this,
                            Constants.WORK_NO_PHOTOS_CONFIRMATION, "").show();
//                    result = WORK_NO_PHOTOS;

                    return;
                } else if (selected == 2) {

                    dialogBoxLayout.setVisibility(View.INVISIBLE);
                    new ConfirmDialog(mContext, SelectActivityDialog.this,
                            Constants.START_TRAINING).show();
//                    result = TRAINING;

                    return;
                }
                /*
                 * intent.putExtra("Selected", result); setResult(RESULT_OK,
				 * intent);
				 */
                finish();
            }
        });

        findViewById(R.id.dialog_cancel).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }

    @Override
    public void onConfirmStartTraining() {

        ContentValues data = new ContentValues();
        Utils.timeSheetRequest = new TimeSheetRequest();

        Utils.timeSheetRequest.setStarted_at(Utils.getCurrentDateAndTime());

        data.put("started_at", Utils.getCurrentDateAndTime());

        User userProfile = JobViewerDBHandler.getUserProfile(this);
        if (userProfile != null) {
            Utils.timeSheetRequest.setRecord_for(userProfile.getEmail());
            data.put("record_for", userProfile.getEmail());
        }

        Utils.timeSheetRequest.setIs_inactive("");
        data.put("is_inactive", "");

        Utils.timeSheetRequest.setIs_overriden("");
        data.put("is_overriden", "");

        Utils.timeSheetRequest.setOverride_reason("");
        data.put("override_reason", "");

        Utils.timeSheetRequest.setOverride_comment("");
        data.put("override_comment", "");

        Utils.timeSheetRequest.setOverride_timestamp("");
        data.put("override_timestamp", "");

        CheckOutObject checkOutObject = JobViewerDBHandler
                .getCheckOutRemember(this);
        if (checkOutObject.getVistecId() != null) {
            Utils.timeSheetRequest
                    .setReference_id(checkOutObject.getVistecId());
            data.put("reference_id", checkOutObject.getVistecId());
        } else {
            Utils.timeSheetRequest.setReference_id("");
            data.put("reference_id", "");
        }
        if (userProfile != null)
            data.put("user_id", userProfile.getEmail());
        else {
            data.put("user_id", "");
        }

        String time = Utils.getCurrentDateAndTime();

        if (Utils.isInternetAvailable(this)) {
            // finish();
            Utils.startProgress(this);
            Utils.SendHTTPRequest(this, CommsConstant.HOST
                            + CommsConstant.START_TRAINING_API, data,
                    getStartTrainingHandler(time));
        } else {
            Utils.saveTimeSheetInBackLogTable(SelectActivityDialog.this,
                    Utils.timeSheetRequest, CommsConstant.START_TRAINING_API,
                    Utils.REQUEST_TYPE_WORK);
            saveTrainingTimeSheet(Utils.timeSheetRequest);
            finish();
            Intent intent = new Intent(mContext, ActivityPageActivity.class);
            startActivity(intent);
        }
        setResult(RESULT_OK);

    }

    private void saveTrainingTimeSheet(TimeSheetRequest timeSheetRequest) {
        StartTrainingObject startTraining = new StartTrainingObject();
        startTraining.setIsTrainingStarted("true");
        startTraining.setStartTime(timeSheetRequest.getStarted_at());
        JobViewerDBHandler.saveStartTraining(this, startTraining);

    }

    private Handler getStartTrainingHandler(final String time) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HttpConnection.DID_SUCCEED:
                        Utils.StopProgress();
                        saveTrainingTimeSheet(Utils.timeSheetRequest);
                        Toast.makeText(
                                BaseActivity.context,
                                BaseActivity.context.getResources().getString(
                                        R.string.startedTrainingMsg),
                                Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(mContext,
                                ActivityPageActivity.class);
                        startActivity(intent);
                        break;
                    case HttpConnection.DID_ERROR:
                        Utils.StopProgress();
                        String error = (String) msg.obj;
                        VehicleException exception = GsonConverter
                                .getInstance()
                                .decodeFromJsonString(error, VehicleException.class);
                        ExceptionHandler.showException(mContext, exception, "Info");
                        Utils.saveTimeSheetInBackLogTable(
                                SelectActivityDialog.this, Utils.timeSheetRequest,
                                CommsConstant.START_TRAINING_API,
                                Utils.REQUEST_TYPE_WORK);
                        saveTrainingTimeSheet(Utils.timeSheetRequest);
                        break;
                    default:
                        break;
                }
            }
        };
        return handler;
    }

    @Override
    public void onConfirmDismiss() {
        dialogBoxLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConfirmStartWithNoPhotos() {
        int selected = -1;

        Intent workWithNoPhotosintent = new Intent();
        for (int i = 0; i < m_data.size(); i++) // clean
        // previous
        // selected
        {
            HashMap<String, Object> m = m_data.get(i);
            Boolean x = (Boolean) m.get("checked");
            if (x == true) {
                selected = i;
                break; // break, since it's a single choice list
            }
        }

        if (selected == -1)
            return;
        else if (selected == 0 || selected == 1) {
            CheckOutObject checkOutRemember = JobViewerDBHandler
                    .getCheckOutRemember(this);
            if (checkOutRemember != null
                    && ActivityConstants.TRUE.equalsIgnoreCase(checkOutRemember
                    .getIsTravelEnd())) {
                workWithNoPhotosintent.setClass(SelectActivityDialog.this,
                        NewWorkActivity.class);
            } else {
                workWithNoPhotosintent.setClass(SelectActivityDialog.this,
                        TravelToWorkSiteActivity.class);
            }
//            result = WORK_NO_PHOTOS;
            workWithNoPhotosintent.putExtra(Constants.WORK_NO_PHOTOS,
                    Constants.WORK_NO_PHOTOS);
            startActivity(workWithNoPhotosintent);
        }
        finish();
    }

    @Override
    public void onConfirmDismissWithNoPhotos() {

    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void updateOkButtonDrawable(boolean isEnable) {
        if (isEnable) {
            dialog_ok.setBackground(mContext.getResources().getDrawable(R.drawable.dialog_red_button));
            dialog_ok.setEnabled(true);
        } else {
            dialog_ok.setBackground(mContext.getResources().getDrawable(R.drawable.dialog_dark_grey_button));
            dialog_ok.setEnabled(false);
        }
    }
    
    private boolean shouldNewWorkTravelArrivedSite(Context mContext) {
        String str = JobViewerDBHandler.getJSONFlagObject(mContext);
        if (Utils.isNullOrEmpty(str)) {
            str = "{}";
        }
        try {
            JSONObject jsonObject = new JSONObject(str);
            if (jsonObject.has(Constants.FLAG_NEW_WORK_TRAVEL_ARRIVED_SITE)) {
            	return jsonObject.getBoolean(Constants.FLAG_NEW_WORK_TRAVEL_ARRIVED_SITE);
            }

            
            String jsonString = jsonObject.toString();
            JobViewerDBHandler.saveFlaginJSONObject(getApplicationContext(),
                    jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
