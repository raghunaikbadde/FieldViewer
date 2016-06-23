package com.lanesgroup.jobviewer.fragment;

import java.io.File;
import java.io.IOException;

import android.app.Fragment;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.db.objects.ShoutAboutSafetyObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.Images;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.GeoLocationCamera;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.GPSTracker;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.R;
import com.raghu.ShoutOutBackLogRequest;
import com.vehicle.communicator.HttpConnection;

public class ShoutOutMediaTextTypeFragment extends Fragment implements
		OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressStep, screenTitle, questionTitle, question;
	private EditText mDescribe;
	private Button mSave, mNext;
	private LinearLayout mLinearLayout;
	private View mRootView;
	private ImageView mCapturedImage;
	private int imageCount = 0;
	public static final int RESULT_OK = -1;
	private Screen currentScreen;
	private static File file;
	private CheckOutObject checkOutRemember;
	private LinearLayout linearLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mRootView = inflater.inflate(R.layout.question_media_text_fragment,
				container, false);
		initUI();
		updateData();
		return mRootView;
	}

	private void updateData() {
		currentScreen = ShoutOutActivity.getQuestionMaster().getScreens()
				.getScreen()[0];
		checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		if (currentScreen.getTitle().equals("Report a hazard or near miss")) {
			mProgressStep
					.setText(getString(R.string.progress_step_end_on_call));
		} else {
			mProgressStep.setText(currentScreen.get_progress() + "%");
		}
		mProgress.setProgress(Integer.parseInt(currentScreen.get_progress()));
		questionTitle.setText(currentScreen.getTitle());
		question.setText(currentScreen.getText());
		screenTitle.setText(getActivity().getResources().getString(
				R.string.shout_about_safety));
		if (!Utils.isNullOrEmpty(currentScreen.getAnswer())) {
			mDescribe.setText(currentScreen.getAnswer());
		}
		checkAndEnableNextButton();
		com.jobviewer.survey.object.Button[] buttons = currentScreen
				.getButtons().getButton();

		for (int i = 0; i < buttons.length; i++) {
			if (ActivityConstants.TRUE
					.equalsIgnoreCase(buttons[i].getDisplay())
					&& !"next".equalsIgnoreCase(buttons[i].getName())) {
				mSave.setText(getResources().getString(
						Utils.getButtonText(buttons[i].getName())));
				break;
			}
		}

		checkAndLoadSavedImages();
	}

	private void checkAndLoadSavedImages() {
		for (int i = 0; i < currentScreen.getImages().length; i++) {
			String image_string = currentScreen.getImages()[i].getTemp_id();
			if (!Utils.isNullOrEmpty(image_string)) {
				ImageObject imageById = JobViewerDBHandler.getImageById(
						getActivity(), image_string);
				String base4String = imageById.getImage_string().replace(
						Constants.IMAGE_STRING_INITIAL, "");
				byte[] getbyteArrayFromBase64String = Utils
						.getbyteArrayFromBase64String(base4String);
				loadImages(getbyteArrayFromBase64String);
			}
		}

	}

	private void loadImages(byte[] getbyteArrayFromBase64String) {
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				500, 420);
		layoutParams.setMargins(0, 30, 0, 30);
		mCapturedImage = new ImageView(getActivity());
		Glide.with(getActivity()).load(getbyteArrayFromBase64String).asBitmap()
				.into(mCapturedImage);
		linearLayout.addView(mCapturedImage, layoutParams);
	}

	private void initUI() {
		mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
		mProgressStep = (TextView) mRootView
				.findViewById(R.id.progress_step_text);
		screenTitle = (TextView) mRootView.findViewById(R.id.screenTitle);
		questionTitle = (TextView) mRootView.findViewById(R.id.questionTitle);
		question = (TextView) mRootView.findViewById(R.id.question);
		mDescribe = (EditText) mRootView.findViewById(R.id.describe_edittext);
		linearLayout = (LinearLayout) mRootView.findViewById(R.id.imageslinear);
		mLinearLayout = (LinearLayout) mRootView
				.findViewById(R.id.capture_layout);
		mLinearLayout.setOnClickListener(this);

		mSave = (Button) mRootView.findViewById(R.id.button1);
		mSave.setOnClickListener(this);
		mNext = (Button) mRootView.findViewById(R.id.button2);
		mNext.setOnClickListener(this);
		mDescribe.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				checkAndEnableNextButton();

			}
		});
	}

	private void checkAndEnableNextButton() {
		Log.e("TAG", "checkAndEnableNextButton");
		int count = 0;
		for (int i = 0; i < currentScreen.getImages().length; i++) {
			if (!Utils.isNullOrEmpty(currentScreen.getImages()[i].getTemp_id())) {
				count++;
			}
		}
		if ((count != 0 && count >= Integer.parseInt(currentScreen
				.getRequired_images()))
				|| !Utils.isNullOrEmpty(mDescribe.getText().toString())) {
			enableNextButton(true);
		} else {
			enableNextButton(false);
		}
	}

	public void enableNextButton(boolean isEnable) {
		if (isEnable) {
			mNext.setEnabled(true);
			mNext.setBackgroundResource(R.drawable.red_background);
		} else {
			mNext.setEnabled(false);
			mNext.setBackgroundResource(R.drawable.dark_grey_background);
		}

	}

	@Override
	public void onClick(View view) {
		if (view == mSave) {
			ShoutAboutSafetyObject obj = new ShoutAboutSafetyObject();
			Screen[] screen = ShoutOutActivity.getQuestionMaster().getScreens()
					.getScreen();
			currentScreen.setAnswer(mDescribe.getText().toString());
			screen[0] = currentScreen;
			ShoutOutActivity.getQuestionMaster().getScreens().setScreen(screen);
			obj.setQuestionSet(GsonConverter.getInstance().encodeToJsonString(
					ShoutOutActivity.getQuestionMaster()));
			obj.setOptionSelected(ShoutOutActivity.getOptionSelected());
			obj.setStartedAt(ShoutOutActivity.getStartedAt());
			JobViewerDBHandler.saveShoutAboutSafety(getActivity(), obj);
			((BaseActivity) getActivity()).goBackToStartScreenFromShoutOut();
		} else if (view == mNext) {
			ShoutAboutSafetyObject obj = new ShoutAboutSafetyObject();
			Screen[] screen = ShoutOutActivity.getQuestionMaster().getScreens()
					.getScreen();
			currentScreen.setAnswer(mDescribe.getText().toString());
			screen[0] = currentScreen;
			obj.setQuestionSet(GsonConverter.getInstance().encodeToJsonString(
					ShoutOutActivity.getQuestionMaster()));
			obj.setOptionSelected(ShoutOutActivity.getOptionSelected());
			obj.setStartedAt(ShoutOutActivity.getStartedAt());
			executeShoutAboutSafetyService(obj);
		} else if (view == mLinearLayout) {
			addPicObjectInScreenIfRequired();
			file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "image.jpg");
			Intent intent = new Intent(
					com.jobviewer.util.Constants.IMAGE_CAPTURE_ACTION);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent,
					com.jobviewer.util.Constants.RESULT_CODE);
		}
	}

	private void executeShoutAboutSafetyService(ShoutAboutSafetyObject obj) {
		Utils.startProgress(getActivity());
		if (Utils.isInternetAvailable(getActivity())) {

			for (int i = 0; i < currentScreen.getImages().length; i++) {
				if (!Utils.isNullOrEmpty(currentScreen.getImages()[i]
						.getTemp_id())) {
					ImageObject imageById = JobViewerDBHandler.getImageById(
							getActivity(),
							currentScreen.getImages()[i].getTemp_id());
					Utils.sendCapturedImageToServer(imageById);
				}
			}
		}
		if (Utils.isInternetAvailable(getActivity())) {
			ContentValues values = new ContentValues();
			CheckOutObject checkOutRemember2 = JobViewerDBHandler
					.getCheckOutRemember(getActivity());
			User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
			if (checkOutRemember2 == null
					|| Utils.isNullOrEmpty(checkOutRemember2.getWorkId())) {
				values.put("work_id", "");
			} else
				values.put("work_id", checkOutRemember2.getWorkId());
			values.put("survey_type", getWorkType(obj.getOptionSelected()));
			values.put("related_type", "Work");
			if (checkOutRemember2 == null
					|| Utils.isNullOrEmpty(checkOutRemember2.getVistecId())) {
				values.put("related_type_reference", "");
			} else
				values.put("related_type_reference",
						checkOutRemember2.getVistecId());
			values.put("started_at", ShoutOutActivity.getStartedAt());
			values.put("completed_at", Utils.getCurrentDateAndTime());

			values.put("created_by", userProfile.getEmail());

			GPSTracker gpsTracker = new GPSTracker(getActivity());

			values.put("survey_json", obj.getQuestionSet());
			Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
					+ CommsConstant.SURVEY_STORE_API, values,
					getSendSurveyHandler());
		} else {
			saveInBackLog(obj);
		}
	}

	private void saveInBackLog(ShoutAboutSafetyObject obj) {
		ShoutOutBackLogRequest shoutOutBackLogRequest = new ShoutOutBackLogRequest();
		CheckOutObject checkOutRemember2 = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		User userProfile = JobViewerDBHandler.getUserProfile(getActivity());
		shoutOutBackLogRequest.setWork_id(checkOutRemember2.getWorkId());
		shoutOutBackLogRequest.setSurvey_type(getWorkType(obj
				.getOptionSelected()));
		shoutOutBackLogRequest.setRelated_type("Work");
		shoutOutBackLogRequest.setRelated_type_reference(checkOutRemember2
				.getVistecId());
		shoutOutBackLogRequest.setStarted_at(ShoutOutActivity.getStartedAt());
		shoutOutBackLogRequest.setCompleted_at(Utils.getCurrentDateAndTime());
		shoutOutBackLogRequest.setSurvey_json(obj.getQuestionSet());
		shoutOutBackLogRequest.setCreated_by(userProfile.getEmail());
		shoutOutBackLogRequest.setStatus("Completed");
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
		Utils.StopProgress();
		JobViewerDBHandler.deleteShoutAboutSafety(getActivity());
		ShoutOutActivity.loadNextFragment(new ShoutOutCompleteFragment());
	}

	private Handler getSendSurveyHandler() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:

					String result = (String) msg.obj;
					Log.i("Android", result);
					JobViewerDBHandler.deleteShoutAboutSafety(getActivity());
					ShoutOutActivity
							.loadNextFragment(new ShoutOutCompleteFragment());
					Utils.StopProgress();
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

	private String getWorkType(String option) {
		if (option.equalsIgnoreCase(ActivityConstants.HAZARD)) {
			return "Shout About Safety Near Miss";
		} else if (option.equalsIgnoreCase(ActivityConstants.IDEA)) {
			return "Shout About Safety Idea";
		} else {
			return "Shout About Safety Good Safety";
		}
	}

	private void addPicObjectInScreenIfRequired() {
		boolean isAllImagedAdded = false;
		for (int i = 0; i < currentScreen.getImages().length; i++) {
			isAllImagedAdded = !Utils.isNullOrEmpty(currentScreen.getImages()[i].getTemp_id());
		}

		if (isAllImagedAdded) {
			Images[] images = new Images[currentScreen.getImages().length + 1];
			for (int i = 0; i < currentScreen.getImages().length; i++) {
				images[i] = currentScreen.getImages()[i];
			}
			images[currentScreen.getImages().length] = new Images();
			currentScreen.setImages(images);
			Log.i("Android", "");
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 500 && resultCode == RESULT_OK) {
			String imageString = null;
			Bitmap photo = Utils.decodeSampledBitmapFromFile(
					file.getAbsolutePath(), 1000, 700);

			Bitmap rotateBitmap = Utils.rotateBitmap(file.getAbsolutePath(),
					photo);
			String currentImageFile = Utils.getRealPathFromURI(
					Uri.fromFile(file), getActivity());
			String formatDate = "";
			String geoLocation = "";

			try {
				ExifInterface exif = new ExifInterface(currentImageFile);
				String picDateTime = exif
						.getAttribute(ExifInterface.TAG_DATETIME);
				formatDate = Utils.formatDate(picDateTime);
				GeoLocationCamera geoLocationCamera = new GeoLocationCamera(
						exif);
				geoLocation = geoLocationCamera.toString();

				Log.i("Android", "formatDateFromOnetoAnother   :" + formatDate);
				Log.i("Android", "geoLocation   :" + geoLocation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			geoLocation = Utils.getGeoLocationString(getActivity());
			for (int i = 0; i < currentScreen.getImages().length; i++) {
				if (Utils.isNullOrEmpty(currentScreen.getImages()[i]
						.getTemp_id())) {
					String image_exif = formatDate + "," + geoLocation;
					// currentScreen.getImages()[i].setImage_exif(image_exif);
					ImageObject imageObject = new ImageObject();
					String generateUniqueID = Utils
							.generateUniqueID(getActivity());
					imageObject.setImageId(generateUniqueID);
					imageObject.setCategory("surveys");
					imageObject.setImage_exif(image_exif);
					imageString = Utils.bitmapToBase64String(rotateBitmap);
					imageObject.setImage_string(Constants.IMAGE_STRING_INITIAL
							+ imageString);
					Log.i("Android", "Image 12 :"
							+ imageObject.getImage_string().substring(0, 50));
					currentScreen.getImages()[i].setTemp_id(generateUniqueID);
					JobViewerDBHandler.saveImage(getActivity(), imageObject);
					break;
				}
			}
			loadImages(Utils.getbyteArrayFromBase64String(imageString));
			imageCount++;
			checkAndEnableNextButton();

		}
	}
}
