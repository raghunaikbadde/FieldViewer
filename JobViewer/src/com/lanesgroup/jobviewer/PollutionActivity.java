package com.lanesgroup.jobviewer;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jobviewer.adapter.MultiChoiceItem;
import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.raghu.PollutionReportRequest;
import com.vehicle.communicator.HttpConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PollutionActivity extends BaseActivity implements
        View.OnClickListener {
    static File file;
    private TextView title_text, number_text, progress_step_text;
    private ProgressBar progressBar;
    private CheckBox ptlCheckbox, ptwCheckbox;
    private LinearLayout ptlExpandLayout, ptwExpandLayout;
    private Spinner extentOfLandSpinner, landAffectedSpinner,
            extentOfWaterSpinner, waterBodyAffectedSpinner,
            indicativeCauseSpinner;
    private boolean savedButtonPressed = false;
    // MultiSelectSpinner waterPollutantsSpinner;
    // additionalEDSpinner;
    private Button nextButton, mTakePicUpStream, mTakePicDownStream,
            mSaveButton;
    private PollutionReportRequest pollutionReportRequest;
    private EditText mUpStreamEditText, mDownStreamEdiText;
    private RelativeLayout spinnerLayout, landAffectedLayout,
            spinnerLayoutExtentOfWater, spinnerLayoutWaterBody,
            spinnerLayoutAmmonia, spinnerLayoutFishKill,
            spinnerLayoutIndicative, spinnerLayoutadditionalED,
            landPollutantsSpinnerLayout, spinnerLayoutWaterPollutants;
    private TextView landPollution, landAffected, extentOfWater, waterBody,
            ammonia, fishKill, indicativeCause, additionalED,
            landPollutantsText, waterPollutantsTextView;
    private String stringOfLandPollutants;
    private String stringOfWaterPollutants;
    private ArrayList<String> stringOfAdditionalEPD;
    private ImageObject upStreamImageObject, downSteamIamgeObject;
    private String[] additionalEPDSelectedText = null;
    private String[] landPollutantSelectedText = null;
    private String[] waterPollutantSelectedText = null;
    private JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pollution_screen);
        initUI();
        updateData();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(Utils.CALLING_ACTIVITY)) {
            if (bundle.getString(Utils.CALLING_ACTIVITY).equalsIgnoreCase(
                    ActivityConstants.ACTIVITY_PAGE_ACTIVITY)) {
                updateUIAsperSavedPollutionReport();
            }
        }

    }

    private void updateData() {
        pollutionReportRequest = new PollutionReportRequest();
        CheckOutObject checkOutRemember = JobViewerDBHandler
                .getCheckOutRemember(this);
        if (ActivityConstants.EXCAVATION.equalsIgnoreCase(checkOutRemember
                .getAssessmentSelected())) {
            title_text.setText(getResources().getString(
                    R.string.excavation_risk_str));
        } else {
            title_text.setText(getResources().getString(
                    R.string.non_excavation_risk_str));
        }
        number_text.setText(checkOutRemember.getVistecId());

        progress_step_text.setText(getResources().getString(
                R.string.pollution_progress));
        progressBar.setProgress((100 / 6) * 4);
        ptlCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                validateUserInputs();
                if (isChecked) {
                    ptlExpandLayout.setVisibility(View.VISIBLE);
                    pollutionReportRequest.setLand_polluted("Yes");
                } else {
                    pollutionReportRequest.setLand_polluted("No");
                    ptlExpandLayout.setVisibility(View.GONE);
                }
            }
        });
        ptwCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                validateUserInputs();
                if (isChecked) {
                    ptwExpandLayout.setVisibility(View.VISIBLE);
                    pollutionReportRequest.setWater_polluted("Yes");
                } else {
                    pollutionReportRequest.setWater_polluted("No");
                    ptwExpandLayout.setVisibility(View.GONE);
                }
            }
        });

        final ArrayAdapter<String> additionEPDAdapter = new ArrayAdapter<String>(
                this, R.layout.simple_spinner_item);
        String[] additionalEPDArray = getResources().getStringArray(
                R.array.additionalEquipmentDeployed);
        for (String value : additionalEPDArray) {
            additionEPDAdapter.add(value);
        }
        upStreamImageObject = new ImageObject();
        downSteamIamgeObject = new ImageObject();
    }

    private void initUI() {
        title_text = (TextView) findViewById(R.id.title_text);
        number_text = (TextView) findViewById(R.id.number_text);
        progress_step_text = (TextView) findViewById(R.id.progress_step_text);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ptlCheckbox = (CheckBox) findViewById(R.id.ptlCheckbox);
        mUpStreamEditText = (EditText) findViewById(R.id.upstream_edittext);
        mDownStreamEdiText = (EditText) findViewById(R.id.downstream_edittext);
        mSaveButton = (Button) findViewById(R.id.button1);
        mSaveButton.setOnClickListener(this);
        ptwCheckbox = (CheckBox) findViewById(R.id.ptwCheckbox);
        ptlExpandLayout = (LinearLayout) findViewById(R.id.ptlExpandLayout);
        ptwExpandLayout = (LinearLayout) findViewById(R.id.ptwExpandLayout);
        nextButton = (Button) findViewById(R.id.nextButton);
        mTakePicUpStream = (Button) findViewById(R.id.takePicture_upstream);
        mTakePicDownStream = (Button) findViewById(R.id.takePicture_downstream);
        mTakePicUpStream.setOnClickListener(this);
        mTakePicDownStream.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        spinnerLayout = (RelativeLayout) findViewById(R.id.spinnerLayout);
        spinnerLayout.setOnClickListener(this);
        landPollution = (TextView) findViewById(R.id.landPollution);
        landAffectedLayout = (RelativeLayout) findViewById(R.id.spinnerLayout1);
        landAffectedLayout.setOnClickListener(this);
        landAffected = (TextView) findViewById(R.id.landAffected);

        spinnerLayoutExtentOfWater = (RelativeLayout) findViewById(R.id.spinnerLayoutExtentOfWater);
        spinnerLayoutExtentOfWater.setOnClickListener(this);
        extentOfWater = (TextView) findViewById(R.id.extentOfWater);

        spinnerLayoutWaterBody = (RelativeLayout) findViewById(R.id.spinnerLayoutWaterBody);
        spinnerLayoutWaterBody.setOnClickListener(this);
        waterBody = (TextView) findViewById(R.id.waterBody);

        spinnerLayoutAmmonia = (RelativeLayout) findViewById(R.id.spinnerLayoutAmmonia);
        spinnerLayoutAmmonia.setOnClickListener(this);
        ammonia = (TextView) findViewById(R.id.ammonia);

        spinnerLayoutFishKill = (RelativeLayout) findViewById(R.id.spinnerLayoutFishKill);
        spinnerLayoutFishKill.setOnClickListener(this);
        fishKill = (TextView) findViewById(R.id.fishKill);

        spinnerLayoutIndicative = (RelativeLayout) findViewById(R.id.spinnerLayoutIndicative);
        spinnerLayoutIndicative.setOnClickListener(this);

        spinnerLayoutadditionalED = (RelativeLayout) findViewById(R.id.spinnerLayoutadditionalED);
        spinnerLayoutadditionalED.setOnClickListener(this);

        landPollutantsSpinnerLayout = (RelativeLayout) findViewById(R.id.landPollutantsSpinnerLayout);
        landPollutantsSpinnerLayout.setOnClickListener(this);

        spinnerLayoutWaterPollutants = (RelativeLayout) findViewById(R.id.spinnerLayoutWaterPollutants);
        spinnerLayoutWaterPollutants.setOnClickListener(this);

        indicativeCause = (TextView) findViewById(R.id.indicativeCause);
        additionalED = (TextView) findViewById(R.id.additionalED);
        landPollutantsText = (TextView) findViewById(R.id.landPollutantsText);
        waterPollutantsTextView = (TextView) findViewById(R.id.waterPollutantsTextView);
        enableNextButton(false);
    }

    @Override
    public void onClick(View v) {
        savedButtonPressed = false;
        switch (v.getId()) {

            case R.id.button1:
                Intent homeIntent = new Intent(this, ActivityPageActivity.class);
                homeIntent.putExtra(Utils.SHOULD_SHOW_WORK_IN_PROGRESS, true);
                homeIntent.putExtra(Utils.CALLING_ACTIVITY,
                        ActivityConstants.POLLUTION_ACTIVITY);

                savedButtonPressed = true;
                sendPollutionReportToServer();
                startActivity(homeIntent);
                break;
            case R.id.nextButton:

                pollutionReportRequest.setLand_pollutants(stringOfLandPollutants);
                pollutionReportRequest.setWater_pollutants(stringOfWaterPollutants);
                if (ptwCheckbox.isChecked()) {
                    JobViewerDBHandler.saveImage(getApplicationContext(),
                            upStreamImageObject);
                    JobViewerDBHandler.saveImage(getApplicationContext(),
                            downSteamIamgeObject);
                }
                if (Utils.isInternetAvailable(PollutionActivity.this)) {
                    Utils.startProgress(PollutionActivity.this);
                    if (ptwCheckbox.isChecked()) {
                        sendUpStreamWorkImageToServer(upStreamImageObject);
                    } else {
                        sendPollutionReportToServer();
                    }

                } else {
                    sendPollutionReportToServer();
                    Intent addPhotosActivityIntent = new Intent(
                            PollutionActivity.this, AddPhotosActivity.class);
                    startActivity(addPhotosActivityIntent);
                }

                break;
            case R.id.spinnerLayout:
                String landPollutionHeader = "Extent of land pollution";
                Utils.dailogboxSelector(this, Utils.mLandPollutionList,
                        R.layout.work_complete_dialog, landPollution,
                        landPollutionHeader);
                break;
            case R.id.spinnerLayout1:
                String landAffectedHeader = "Land type";
                Utils.dailogboxSelector(this, Utils.mLandAffectedList,
                        R.layout.work_complete_dialog, landAffected,
                        landAffectedHeader);
                break;
            case R.id.spinnerLayoutExtentOfWater:
                String extentOfWaterHeader = "Extent of water pollution";
                Utils.dailogboxSelector(this, Utils.mExtentOfWaterList,
                        R.layout.work_complete_dialog, extentOfWater,
                        extentOfWaterHeader);
                break;
            case R.id.spinnerLayoutWaterBody:
                String waterBodyHeader = "Water body";
                Utils.dailogboxSelector(this, Utils.mWaterBodyList,
                        R.layout.work_complete_dialog, waterBody, waterBodyHeader);
                break;
            case R.id.spinnerLayoutIndicative:
                String indicativeHeader = "Indicative Cause";
                Utils.dailogboxSelector(this, Utils.mIndicativeCause,
                        R.layout.work_complete_dialog, indicativeCause,
                        indicativeHeader);
                break;
            case R.id.spinnerLayoutAmmonia:
                String ammoniaHeader = "Ammonia";
                Utils.dailogboxSelector(this, Utils.mAmmonia,
                        R.layout.work_complete_dialog, ammonia, ammoniaHeader);
                break;
            case R.id.spinnerLayoutFishKill:
                String fishKillHeader = "Fish Kill";
                Utils.dailogboxSelector(this, Utils.mFishKill,
                        R.layout.work_complete_dialog, fishKill, fishKillHeader);
                break;
            case R.id.takePicture_upstream:
                file = new File(Environment.getExternalStorageDirectory()
                        + File.separator + "image.jpg");
                Intent intent = new Intent(
                        com.jobviewer.util.Constants.IMAGE_CAPTURE_ACTION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(intent,
                        com.jobviewer.util.Constants.UPSTREAM_RESULT_CODE);
                break;
            case R.id.takePicture_downstream:
                file = new File(Environment.getExternalStorageDirectory()
                        + File.separator + "image.jpg");
                Intent imageIntent = new Intent(
                        com.jobviewer.util.Constants.IMAGE_CAPTURE_ACTION);
                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(imageIntent,
                        com.jobviewer.util.Constants.DOWNSTREAM_RESULT_CODE);
                break;
            case R.id.spinnerLayoutadditionalED:
                String title = "Select equipment";
                String[] additionalEPDArray = getResources().getStringArray(
                        R.array.additionalEquipmentDeployed);
                String text = additionalED.getText().toString();
                boolean isSelctedBefore = false;

                if (!getResources().getString(R.string.select_spinner_str)
                        .equalsIgnoreCase(text)) {
                    isSelctedBefore = true;
                    additionalEPDSelectedText = text.split(",");
                }

                List<MultiChoiceItem> multiChoiceItems = new ArrayList<MultiChoiceItem>();
                for (String currentValue : additionalEPDSelectedText) {
                    MultiChoiceItem item = new MultiChoiceItem();
                    item.setText(currentValue);
                    if (isSelctedBefore) {
                        for (String value : additionalEPDSelectedText) {
                            if (currentValue
                                    .equalsIgnoreCase(value)) {
                                item.setChecked(true);
                                break;
                            }
                        }
                    } else {
                        item.setChecked(false);
                    }

                    multiChoiceItems.add(item);
                }
                Utils.createMultiSelectDialog(this, multiChoiceItems, title,
                        additionalED);
                break;
            case R.id.landPollutantsSpinnerLayout:
                String landPollutantTitle = "Land Pollutants";
                String[] landPollutantArray = getResources().getStringArray(
                        R.array.landPollutantsArray);
                String landPollutanttext = landPollutantsText.getText().toString();
                boolean islandPollutantSelctedBefore = false;

                if (!getResources().getString(R.string.select_spinner_str)
                        .equalsIgnoreCase(landPollutanttext)) {
                    islandPollutantSelctedBefore = true;
                    landPollutantSelectedText = landPollutanttext.split(",");
                }

                List<MultiChoiceItem> multiChoiceItems1 = new ArrayList<MultiChoiceItem>();
                for (String currentValue : landPollutantArray) {
                    MultiChoiceItem item = new MultiChoiceItem();
                    item.setText(currentValue);
                    if (islandPollutantSelctedBefore) {
                        for (String value : landPollutantSelectedText) {
                            if (currentValue
                                    .equalsIgnoreCase(value)) {
                                item.setChecked(true);
                                break;
                            }
                        }
                    } else {
                        item.setChecked(false);
                    }

                    multiChoiceItems1.add(item);
                }
                Utils.createMultiSelectDialog(this, multiChoiceItems1,
                        landPollutantTitle, landPollutantsText);
                break;
            case R.id.spinnerLayoutWaterPollutants:
                String waterPollutantTitle = "Water Pollutants";
                String[] waterPollutantArray = getResources().getStringArray(
                        R.array.waterPollutantsArray);
                String waterPollutanttext = waterPollutantsTextView.getText()
                        .toString();
                boolean iswaterPollutantSelctedBefore = false;

                if (!getResources().getString(R.string.select_spinner_str)
                        .equalsIgnoreCase(waterPollutanttext)) {
                    iswaterPollutantSelctedBefore = true;
                    waterPollutantSelectedText = waterPollutanttext.split(",");
                }

                List<MultiChoiceItem> multiChoiceItems2 = new ArrayList<MultiChoiceItem>();
                for (String curValue : waterPollutantArray) {
                    MultiChoiceItem item = new MultiChoiceItem();
                    item.setText(curValue);
                    if (iswaterPollutantSelctedBefore) {
                        for (String value : waterPollutantSelectedText) {
                            if (curValue
                                    .equalsIgnoreCase(value)) {
                                item.setChecked(true);
                                break;
                            }
                        }
                    } else {
                        item.setChecked(false);
                    }

                    multiChoiceItems2.add(item);
                }
                Utils.createMultiSelectDialog(this, multiChoiceItems2,
                        waterPollutantTitle, waterPollutantsTextView);
                break;
            default:
                break;
        }
        validateUserInputs();
    }

    private void sendPollutionReportToServer() {
        ContentValues data = new ContentValues();

        if (ptlCheckbox.isChecked()) {
            data.put("land_polluted", pollutionReportRequest.getLand_polluted());

            pollutionReportRequest.setLand_area(landPollution.getText()
                    .toString());
            data.put("land_area", pollutionReportRequest.getLand_area());

            pollutionReportRequest.setLand_type(landAffected.getText()
                    .toString());
            data.put("land_type", pollutionReportRequest.getLand_type());

            if (pollutionReportRequest.getLand_pollutants() == null) {
                pollutionReportRequest.setLand_pollutants("");
            }

            String landpollutants = landPollutantsText.getText().toString();
            pollutionReportRequest.setLand_pollutants(landpollutants);
            /*
             * String idList = landpollutants.toString(); String
			 * landPollutantsString = idList.substring(1, idList.length() -
			 * 1).replace(", ", ",");
			 */
            data.put("land_pollutants", landpollutants);
        }

        if (ptwCheckbox.isChecked()) {

            data.put("water_polluted",
                    pollutionReportRequest.getWater_polluted());

            pollutionReportRequest.setWater_area(extentOfWater.getText()
                    .toString());
            data.put("water_area", pollutionReportRequest.getWater_area());

            pollutionReportRequest
                    .setWater_body(waterBody.getText().toString());
            data.put("water_body", pollutionReportRequest.getWater_body());

            String waterpts = waterPollutantsTextView.getText().toString();
            pollutionReportRequest.setWater_pollutants(waterpts);
            data.put("water_pollutants", waterpts);

            pollutionReportRequest.setDo_upstream(mUpStreamEditText.getText()
                    .toString());
            data.put("do_upstream", pollutionReportRequest.getDo_upstream());

            pollutionReportRequest.setDo_downstream(mDownStreamEdiText
                    .getText().toString());
            data.put("do_downstream", pollutionReportRequest.getDo_downstream());

            if (!Utils.isNullOrEmpty(upStreamImageObject.getImage_string())) {
                pollutionReportRequest.setDo_upstream_image(upStreamImageObject
                        .getImageId());
            } else {
                pollutionReportRequest.setDo_upstream_image("");
            }
            data.put("do_upstream_image",
                    pollutionReportRequest.getDo_upstream_image());

            if (!Utils.isNullOrEmpty(downSteamIamgeObject.getImage_string())) {
                pollutionReportRequest
                        .setDo_downstream_image(downSteamIamgeObject
                                .getImageId());
            } else {
                pollutionReportRequest.setDo_downstream_image("");
            }
            data.put("do_downstream_image",
                    pollutionReportRequest.getDo_downstream_image());

            pollutionReportRequest.setAmmonia(ammonia.getText().toString());
            data.put("ammonia", pollutionReportRequest.getAmmonia());

            pollutionReportRequest.setFish_kill(fishKill.getText().toString());
            data.put("fish_kill", pollutionReportRequest.getFish_kill());
            data.put("failed_asset", pollutionReportRequest.getFailed_asset());
        }

        pollutionReportRequest.setIndicative_cause(indicativeCause.getText()
                .toString());
        data.put("indicative_cause",
                pollutionReportRequest.getIndicative_cause());

        ArrayList<String> myList = new ArrayList<String>(
                Arrays.asList(additionalED.getText().toString().split(",")));
        String equipmentdeployed = "";
        for (int i = 0; i < myList.size(); i++) {
            equipmentdeployed += myList.get(i) + ",";
        }
        equipmentdeployed = equipmentdeployed.substring(0,
                equipmentdeployed.length() - 1);
        if (equipmentdeployed.contains("elect")) {
            equipmentdeployed = "";
        }
        pollutionReportRequest.setEquipment_deployed(equipmentdeployed);

        data.put("equipment_deployed",
                pollutionReportRequest.getEquipment_deployed());

        Log.d(Utils.LOG_TAG, " url - : " + CommsConstant.HOST
                + CommsConstant.POLLUTION_REPORT_UPLOAD + "/" + Utils.work_id);
        Log.d(Utils.LOG_TAG, " request "
                + GsonConverter.getInstance().encodeToJsonString(data));
        try {
            CheckOutObject checkOutObject = JobViewerDBHandler
                    .getCheckOutRemember(this);
            if (checkOutObject != null
                    && Utils.isNullOrEmpty(checkOutObject.getWorkId())) {
                Utils.work_id = checkOutObject.getWorkId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Utils.isInternetAvailable(PollutionActivity.this)
                && !savedButtonPressed) {
            Utils.SendHTTPRequest(this, CommsConstant.HOST
                    + CommsConstant.POLLUTION_REPORT_UPLOAD + "/"
                    + Utils.work_id, data, getPollutionReportHandler());
        } else {
            if (!savedButtonPressed)
                savePollutionReportInBackLogDb();
            else
                savePollutionReport();
        }
    }

    private Handler getPollutionReportHandler() {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HttpConnection.DID_SUCCEED:
                        Utils.StopProgress();
                        Log.d(Utils.LOG_TAG, "pollution report success");

                        Intent addPhotosActivityIntent = new Intent(
                                PollutionActivity.this, AddPhotosActivity.class);
                        startActivity(addPhotosActivityIntent);
                        break;
                    case HttpConnection.DID_ERROR:
                        String error = (String) msg.obj;
                        Utils.StopProgress();
                        VehicleException exception = GsonConverter
                                .getInstance()
                                .decodeFromJsonString(error, VehicleException.class);
                        ExceptionHandler.showException(PollutionActivity.this,
                                exception, "Info");
                    /*
                     * Intent addfailurePhotosActivityIntent = new
					 * Intent(PollutionActivity.this, AddPhotosActivity.class);
					 * startActivity(addfailurePhotosActivityIntent);
					 */
                        savePollutionReportInBackLogDb();
                        break;
                    default:
                        break;
                }
            }
        };
        return handler;
    }

    private void savePollutionReportInBackLogDb() {
        BackLogRequest backLogRequest = new BackLogRequest();
        backLogRequest.setRequestApi(CommsConstant.HOST
                + CommsConstant.POLLUTION_REPORT_UPLOAD + "/" + Utils.work_id);
        backLogRequest.setRequestClassName("PollutionReportRequest");
        backLogRequest.setRequestJson(GsonConverter.getInstance()
                .encodeToJsonString(pollutionReportRequest));
        backLogRequest.setRequestType(Utils.REQUEST_TYPE_WORK);
        JobViewerDBHandler.saveBackLog(getApplicationContext(), backLogRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == com.jobviewer.util.Constants.UPSTREAM_RESULT_CODE
                && resultCode == RESULT_OK) {
            upStreamImageObject = new ImageObject();
            upStreamImageObject = prepareImageObject(upStreamImageObject);

            mTakePicUpStream.setText(null);
            mTakePicUpStream
                    .setCompoundDrawablesWithIntrinsicBounds(null,
                            ResourcesCompat.getDrawable(getResources(),
                                    R.drawable.pollution_camera_icon, null),
                            null, null);
            mTakePicUpStream.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.red));

        } else if (requestCode == com.jobviewer.util.Constants.DOWNSTREAM_RESULT_CODE
                && resultCode == RESULT_OK) {
            downSteamIamgeObject = new ImageObject();
            downSteamIamgeObject = prepareImageObject(downSteamIamgeObject);
            mTakePicDownStream.setText(null);
            mTakePicDownStream
                    .setCompoundDrawablesWithIntrinsicBounds(null,
                            ResourcesCompat.getDrawable(getResources(),
                                    R.drawable.pollution_camera_icon, null),
                            null, null);
            mTakePicDownStream.setBackgroundColor(ContextCompat.getColor(
                    context, R.color.red));
        }
    }

    private ImageObject prepareImageObject(ImageObject imageObject) {
        String generateUniqueID = Utils.generateUniqueID(this);
        GPSTracker gpsTracker = new GPSTracker(this);
        String geoLocation = "";
        try {
            String lat = String.valueOf(gpsTracker.getLatitude());
            String lon = String.valueOf(gpsTracker.getLongitude());
            geoLocation = lat + "," + lon;
        } catch (Exception e) {
            e.printStackTrace();
        }

        imageObject.setImageId(generateUniqueID);
        imageObject.setCategory("pollution");

        Bitmap photo = Utils.decodeSampledBitmapFromFile(
                file.getAbsolutePath(), 1000, 700);

        Bitmap rotateBitmap = Utils.rotateBitmap(file.getAbsolutePath(), photo);

        String currentImageFile = Utils.getRealPathFromURI(Uri.fromFile(file),
                this);

        String formatDate = "";

        try {
            ExifInterface exif = new ExifInterface(currentImageFile);
            String picDateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
            formatDate = Utils.formatDate(picDateTime);
            // geoLocation = geoLocationCamera.toString();

            Log.i("Android", "formatDateFromOnetoAnother   :" + formatDate);
            Log.i("Android", "geoLocation   :" + geoLocation);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        geoLocation = Utils.getGeoLocationString(this);
        String image_exif = formatDate + "," + geoLocation;
        imageObject.setImage_string(Utils.bitmapToBase64String(rotateBitmap));
        imageObject.setImage_exif(image_exif);
        return imageObject;
    }

    public void enableNextButton(boolean isEnable) {
        if (isEnable) {
            nextButton.setEnabled(true);
            nextButton.setBackgroundResource(R.drawable.red_background);
            nextButton.setOnClickListener(this);
        } else {
            nextButton.setEnabled(false);
            nextButton.setBackgroundResource(R.drawable.dark_grey_background);
            nextButton.setOnClickListener(null);
        }
    }

    public void validateUserInputs() {
        enableNextButton(false);
        if (ptlCheckbox.isChecked() && ptwCheckbox.isChecked()) {
            String extentOfLandPollution = landPollution.getText().toString();
            String landEffected = landAffected.getText().toString();
            String[] langpollutants = {};
            if (!Utils.isNullOrEmpty(landPollutantsText.getText().toString())) {
                pollutionReportRequest.setLand_pollutants(landPollutantsText
                        .getText().toString());
                langpollutants = landPollutantsText.getText().toString()
                        .split(",");
            }

            String indicativeCasueStr = indicativeCause.getText().toString();
            pollutionReportRequest.setIndicative_cause(indicativeCasueStr);
            String extentOfWaterPollution = extentOfWater.getText().toString();
            String waterBoddyEffected = waterBody.getText().toString();
            String[] waterPollutants = {};

            if (!Utils.isNullOrEmpty(waterPollutantsTextView.getText()
                    .toString())) {
                pollutionReportRequest
                        .setWater_pollutants(waterPollutantsTextView.getText()
                                .toString());
                waterPollutants = waterPollutantsTextView.getText().toString()
                        .split(",");
            }
            String ammoniaStr = ammonia.getText().toString();
            String fishKillStr = fishKill.getText().toString();
            if (!extentOfLandPollution.contains("Select")
                    && !landEffected.contains("Select")
                    && langpollutants.length != 0) {
                Log.d(Utils.LOG_TAG, "LandPollutionPassed");
                if (!extentOfWaterPollution.contains("Select")
                        && !waterBoddyEffected.contains("Select")
                        && waterPollutants.length != 0
                        && !ammoniaStr.contains("Select")
                        && !fishKillStr.contains("Select")) {
                    Log.d(Utils.LOG_TAG, "WaterPollutionPassed");
                    if (!indicativeCasueStr.contains("Select")) {
                        Log.d(Utils.LOG_TAG, "IndicativeCausePassed");
                        enableNextButton(true);
                    }
                }
            }
            Log.d(Utils.LOG_TAG, " extentOfLandPollution "
                    + extentOfLandPollution + " landEffected " + landEffected
                    + "indicativeCasueStr " + indicativeCasueStr
                    + " landPollutants Size " + langpollutants.length);

            Log.d(Utils.LOG_TAG, " extentOfWaterPollution "
                    + extentOfWaterPollution + " waterBoddyEffected "
                    + waterBoddyEffected + " ammoniaStr " + ammoniaStr
                    + " fishKillStr " + fishKillStr + " indicativeCasueStr "
                    + indicativeCasueStr + " size of water pollutatnat"
                    + waterPollutants.length);

        } else {
            if (ptlCheckbox.isChecked()) {
                String extentOfLandPollution = landPollution.getText()
                        .toString();
                String landEffected = landAffected.getText().toString();

                String[] langpollutants = {};
                if (!Utils.isNullOrEmpty(landPollutantsText.getText()
                        .toString())) {
                    pollutionReportRequest
                            .setLand_pollutants(landPollutantsText.getText()
                                    .toString());
                    langpollutants = landPollutantsText.getText().toString()
                            .split(",");
                }
                String indicativeCasueStr = indicativeCause.getText()
                        .toString();
                pollutionReportRequest.setIndicative_cause(indicativeCasueStr);
                if (!extentOfLandPollution.contains("Select")
                        && !landEffected.contains("Select")
                        && langpollutants.length != 0) {
                    if (!indicativeCasueStr.contains("Select")) {
                        enableNextButton(true);
                    }
                }

                Log.d(Utils.LOG_TAG, " extentOfLandPollution "
                        + extentOfLandPollution + " landEffected "
                        + landEffected + "indicativeCasueStr "
                        + indicativeCasueStr);
            }
            if (ptwCheckbox.isChecked()) {
                String extentOfWaterPollution = extentOfWater.getText()
                        .toString();
                String waterBoddyEffected = waterBody.getText().toString();

                String[] waterPollutants = {};

                if (!Utils.isNullOrEmpty(waterPollutantsTextView.getText()
                        .toString())) {
                    pollutionReportRequest
                            .setWater_pollutants(waterPollutantsTextView
                                    .getText().toString());
                    waterPollutants = waterPollutantsTextView.getText()
                            .toString().split(",");
                }

                String ammoniaStr = ammonia.getText().toString();
                String fishKillStr = fishKill.getText().toString();
                String indicativeCasueStr = indicativeCause.getText()
                        .toString();
                pollutionReportRequest.setIndicative_cause(indicativeCasueStr);
                if (!extentOfWaterPollution.contains("Select")
                        && !waterBoddyEffected.contains("Select")
                        && waterPollutants.length != 0
                        && !ammoniaStr.contains("Select")
                        && !fishKillStr.contains("Select")) {
                    if (!indicativeCasueStr.contains("Select")) {
                        enableNextButton(true);
                    }
                }

                Log.d(Utils.LOG_TAG, " extentOfWaterPollution "
                        + extentOfWaterPollution + " waterBoddyEffected "
                        + waterBoddyEffected + " ammoniaStr " + ammoniaStr
                        + " fishKillStr " + fishKillStr
                        + " indicativeCasueStr " + indicativeCasueStr
                        + " size of water pollutatnat" + waterPollutants.length);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(this, ActivityPageActivity.class);
        homeIntent.putExtra(Utils.SHOULD_SHOW_WORK_IN_PROGRESS, true);
        homeIntent.putExtra(Utils.CALLING_ACTIVITY,
                ActivityConstants.POLLUTION_ACTIVITY);
        startActivity(homeIntent);
    }

    private synchronized void sendUpStreamWorkImageToServer(
            ImageObject imageObject) {
        if (imageObject != null) {
            ContentValues data = new ContentValues();
            data.put("temp_id", imageObject.getImageId());
            data.put("category", imageObject.getCategory());
            data.put("image_string", Constants.IMAGE_STRING_INITIAL
                    + imageObject.getImage_string());
            data.put("image_exif", imageObject.getImage_exif());
            Log.d(Utils.LOG_TAG,
                    " pollutiion activity sendUpStreamWorkImageToServer");
            Utils.SendHTTPRequest(this, CommsConstant.HOST
                            + CommsConstant.SURVEY_PHOTO_UPLOAD, data,
                    getSendWorkUpImageHandler(imageObject));
        } else {
            return;
        }

    }

    private synchronized void sendDownStreamWorkImageToServer(
            ImageObject imageObject) {
        if (imageObject != null) {
            ContentValues data = new ContentValues();
            data.put("temp_id", imageObject.getImageId());
            data.put("category", imageObject.getCategory());
            data.put("image_string", Constants.IMAGE_STRING_INITIAL
                    + imageObject.getImage_string());
            data.put("image_exif", imageObject.getImage_exif());
            Log.d(Utils.LOG_TAG,
                    " pollutiion activity sendDownStreamWorkImageToServer");
            Utils.SendHTTPRequest(this, CommsConstant.HOST
                            + CommsConstant.SURVEY_PHOTO_UPLOAD, data,
                    getSendWorkDownImageHandler(imageObject));
        } else {
            return;
        }

    }

    private Handler getSendWorkUpImageHandler(final ImageObject imageObject) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HttpConnection.DID_SUCCEED:
                        Log.d(Utils.LOG_TAG,
                                " pollutiion activity getSendWorkUpImageHandler handleMessage DID_SUCCEED");
                    /*
                     * Intent intent = new Intent(getActivity(),
					 * RiskAssessmentActivity.class); startActivity(intent);
					 */

                        sendDownStreamWorkImageToServer(downSteamIamgeObject);
                        break;
                    case HttpConnection.DID_ERROR:

                        String error = (String) msg.obj;
                    /*
                     * VehicleException exception = GsonConverter .getInstance()
					 * .decodeFromJsonString(error, VehicleException.class);
					 * ExceptionHandler.showException(getApplicationContext(),
					 * exception, "Info");
					 */
                        Log.d(Utils.LOG_TAG,
                                " pollutiion activity getSendWorkUpImageHandler handleMessage DID_ERROR "
                                        + error);
                        sendDownStreamWorkImageToServer(downSteamIamgeObject);

                        Utils.saveWorkImageInBackLogDb(getApplicationContext(),
                                upStreamImageObject);
                        break;
                    default:
                        break;
                }
            }
        };
        return handler;
    }

    private Handler getSendWorkDownImageHandler(final ImageObject imageObject) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HttpConnection.DID_SUCCEED:
                        Log.d(Utils.LOG_TAG,
                                " pollutiion activity getSendWorkDownImageHandler handleMessage DID_SUCCEED");
                    /*
                     * Intent intent = new Intent(getActivity(),
					 * RiskAssessmentActivity.class); startActivity(intent);
					 */

                        sendPollutionReportToServer();
                        break;
                    case HttpConnection.DID_ERROR:

                        String error = (String) msg.obj;
                    /*
					 * VehicleException exception = GsonConverter .getInstance()
					 * .decodeFromJsonString(error, VehicleException.class);
					 * ExceptionHandler.showException(getApplicationContext(),
					 * exception, "Info");
					 */
                        Log.d(Utils.LOG_TAG,
                                " pollutiion activity getSendWorkDownImageHandler handleMessage DID_ERROR "
                                        + error);
                        Utils.saveWorkImageInBackLogDb(getApplicationContext(),
                                downSteamIamgeObject);
                        break;
                    default:
                        break;
                }
            }
        };
        return handler;
    }

    private void savePollutionReport() {

        String jsonFlagObject = JobViewerDBHandler.getJSONFlagObject(this);

        if (Utils.isNullOrEmpty(jsonFlagObject)) {
            jsonFlagObject = "{}";
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonFlagObject);
            if (jsonObject.has(Constants.SAVED_POLLUTION_DATA)) {
                jsonObject.remove(Constants.SAVED_POLLUTION_DATA);
            }
            if (jsonObject.has(Constants.UPSTREAM_IMAGE_OBJECT)) {
                jsonObject.remove(Constants.UPSTREAM_IMAGE_OBJECT);
            }
            if (jsonObject.has(Constants.DOWNSTREAM_IMAGE_OBJECT)) {
                jsonObject.remove(Constants.DOWNSTREAM_IMAGE_OBJECT);
            }
            jsonObject.put(Constants.SAVED_POLLUTION_DATA, GsonConverter
                    .getInstance().encodeToJsonString(pollutionReportRequest));
            jsonObject.put(Constants.UPSTREAM_IMAGE_OBJECT, GsonConverter
                    .getInstance().encodeToJsonString(upStreamImageObject));
            jsonObject.put(Constants.DOWNSTREAM_IMAGE_OBJECT, GsonConverter
                    .getInstance().encodeToJsonString(downSteamIamgeObject));
            String jsonString = jsonObject.toString();
            JobViewerDBHandler.saveFlaginJSONObject(getApplicationContext(),
                    jsonString);
        } catch (JSONException jsoe) {
            jsoe.printStackTrace();
        }

    }

    private void updateUIAsperSavedPollutionReport() {
        String jsonFlagObject = JobViewerDBHandler.getJSONFlagObject(this);
        PollutionReportRequest savedPollutionReportRequest = new PollutionReportRequest();
        if (Utils.isNullOrEmpty(jsonFlagObject)) {
            jsonFlagObject = "{}";
        }
        try {
            JSONObject pollutionObject = new JSONObject(jsonFlagObject);
            if (pollutionObject.has(Constants.SAVED_POLLUTION_DATA)) {
                String pollutionObjectString = pollutionObject
                        .getString(Constants.SAVED_POLLUTION_DATA);
                if (!Utils.isNullOrEmpty(pollutionObjectString)) {
                    savedPollutionReportRequest = GsonConverter.getInstance()
                            .decodeFromJsonString(pollutionObjectString,
                                    PollutionReportRequest.class);
                }
            }
            if (pollutionObject.has(Constants.UPSTREAM_IMAGE_OBJECT)) {
                String upstreamImageObjectString = pollutionObject
                        .getString(Constants.UPSTREAM_IMAGE_OBJECT);
                if (!Utils.isNullOrEmpty(upstreamImageObjectString)) {
                    upStreamImageObject = GsonConverter.getInstance()
                            .decodeFromJsonString(upstreamImageObjectString,
                                    ImageObject.class);
                }
            }
            if (pollutionObject.has(Constants.DOWNSTREAM_IMAGE_OBJECT)) {
                String downStreamImageObjectString = pollutionObject
                        .getString(Constants.DOWNSTREAM_IMAGE_OBJECT);
                if (!Utils.isNullOrEmpty(downStreamImageObjectString)) {
                    downSteamIamgeObject = GsonConverter.getInstance()
                            .decodeFromJsonString(downStreamImageObjectString,
                                    ImageObject.class);
                }
            }
        } catch (JSONException jsoe) {
            jsoe.printStackTrace();
        }
        if (savedPollutionReportRequest.getLand_polluted().equalsIgnoreCase(
                "Yes")) {
            ptlCheckbox.setChecked(true);
            ptlExpandLayout.setVisibility(View.VISIBLE);
            String land_area = savedPollutionReportRequest.getLand_area();
            String land_pollutants = savedPollutionReportRequest
                    .getLand_pollutants();
            String land_type = savedPollutionReportRequest.getLand_type();

            if (!Utils.isNullOrEmpty(land_area)) {
                landPollution.setText(land_area);
            }
            if (!Utils.isNullOrEmpty(land_pollutants)) {
                landPollutantsText.setText(land_pollutants);
            }
            if (!Utils.isNullOrEmpty(land_type)) {
                landAffected.setText(land_type);
            }

        }
        if (savedPollutionReportRequest.getWater_polluted().equalsIgnoreCase(
                "Yes")) {
            ptwCheckbox.setChecked(true);
            ptwExpandLayout.setVisibility(View.VISIBLE);
            String water_area = savedPollutionReportRequest.getWater_area();
            String water_body = savedPollutionReportRequest.getWater_body();
            String water_pollutants = savedPollutionReportRequest
                    .getWater_pollutants();
            String ammonia1 = savedPollutionReportRequest.getAmmonia();
            String fish_kill = savedPollutionReportRequest.getFish_kill();
            String do_upstream = savedPollutionReportRequest.getDo_upstream();
            String do_downstream = savedPollutionReportRequest
                    .getDo_downstream();
            String do_downstream_image = savedPollutionReportRequest
                    .getDo_downstream_image();
            String do_upstream_image = savedPollutionReportRequest
                    .getDo_upstream_image();
            if (!Utils.isNullOrEmpty(water_area)) {
                extentOfWater.setText(water_area);
            }
            if (!Utils.isNullOrEmpty(water_body)) {
                waterBody.setText(water_body);
            }
            if (!Utils.isNullOrEmpty(water_pollutants)) {
                waterPollutantsTextView.setText(water_pollutants);
            }
            if (!Utils.isNullOrEmpty(ammonia1)) {
                ammonia.setText(ammonia1);
            }
            if (!Utils.isNullOrEmpty(fish_kill)) {
                fishKill.setText(fish_kill);
            }
            if (!Utils.isNullOrEmpty(do_upstream)) {
                mUpStreamEditText.setText(do_upstream);
            }
            if (!Utils.isNullOrEmpty(do_downstream)) {
                mDownStreamEdiText.setText(do_downstream);
            }
            if (!Utils.isNullOrEmpty(do_upstream_image)) {
                mTakePicUpStream.setText(null);
                mTakePicUpStream.setCompoundDrawablesWithIntrinsicBounds(null,
                        ResourcesCompat.getDrawable(getResources(),
                                R.drawable.pollution_camera_icon, null), null,
                        null);
                mTakePicUpStream.setBackgroundColor(ContextCompat.getColor(
                        context, R.color.red));

            }
            if (!Utils.isNullOrEmpty(do_downstream_image)) {
                mTakePicDownStream.setText(null);
                mTakePicDownStream.setCompoundDrawablesWithIntrinsicBounds(
                        null, ResourcesCompat.getDrawable(getResources(),
                                R.drawable.pollution_camera_icon, null), null,
                        null);
                mTakePicDownStream.setBackgroundColor(ContextCompat.getColor(
                        context, R.color.red));
            }

        }
        String equipment_deployed = savedPollutionReportRequest
                .getEquipment_deployed();
        if (!Utils.isNullOrEmpty(equipment_deployed)) {
            additionalED.setText(equipment_deployed);
        }
        String indicative_cause = savedPollutionReportRequest
                .getIndicative_cause();
        if (!Utils.isNullOrEmpty(indicative_cause)) {
            indicativeCause.setText(indicative_cause);

        }

        validateUserInputs();
        removeSavedPollutionFlags();
    }

    private void removeSavedPollutionFlags() {
        String jsonFlagObject = JobViewerDBHandler.getJSONFlagObject(this);

        if (Utils.isNullOrEmpty(jsonFlagObject)) {
            jsonFlagObject = "{}";
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonFlagObject);
            if (jsonObject.has(Constants.SAVED_POLLUTION_DATA)) {
                jsonObject.remove(Constants.SAVED_POLLUTION_DATA);
            }
            if (jsonObject.has(Constants.UPSTREAM_IMAGE_OBJECT)) {
                jsonObject.remove(Constants.UPSTREAM_IMAGE_OBJECT);
            }
            if (jsonObject.has(Constants.DOWNSTREAM_IMAGE_OBJECT)) {
                jsonObject.remove(Constants.DOWNSTREAM_IMAGE_OBJECT);
            }
            String jsonString = jsonObject.toString();
            JobViewerDBHandler.saveFlaginJSONObject(getApplicationContext(),
                    jsonString);
        } catch (JSONException jsoe) {
            jsoe.printStackTrace();
        }
    }
}