package com.jobviewer.confined.fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jobviewer.comms.CommsConstant;
import com.jobviewer.confined.ConfinedQuestionManager;
import com.jobviewer.confined.CountdownTimer;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.Images;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.GeoLocationCamera;
import com.jobviewer.util.Constants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.R;
import com.vehicle.communicator.HttpConnection;

public class ConfinedTimerWithMediaFragment extends Fragment implements
		OnClickListener {
	private View mRootView;
	ProgressBar progressBar;
	TextView screenTitle, progress_step_text, overhead_text, question_text,
			next_update_text;
	static TextView timer_text;
	LinearLayout skip_timer;
	Button saveBtn;
	static Button nextBtn;
	static Screen currentScreen;
	String time;
	static CountdownTimer timer;
	LinearLayout linearLayout;
	ImageButton capture_imageButton;
	static File file;
	public static final int RESULT_OK = -1;
	private ImageView mCapturedImage;
	int imageCount = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mRootView = inflater.inflate(
				R.layout.confined_space_updates_media_screen, container, false);

		removePhoneKeypad();
		initUI();
		updateData();
		enableNextButton(true);
		return mRootView;
	}

	public void removePhoneKeypad() {
		InputMethodManager inputManager = (InputMethodManager) mRootView
				.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

		IBinder binder = mRootView.getWindowToken();
		inputManager.hideSoftInputFromWindow(binder,
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void updateData() {
		currentScreen = ConfinedQuestionManager.getInstance()
				.getCurrentScreen();
		screenTitle.setText(getResources().getString(
				R.string.confined_space_str));
		progressBar.setProgress(Integer.parseInt(currentScreen.get_progress()));
		progress_step_text.setText(currentScreen.get_progress() + "%");
		overhead_text.setText(currentScreen.getTitle());
		question_text.setText(currentScreen.getText());
		time = currentScreen.getTime();
		if (currentScreen.isAllow_skip()) {
			skip_timer.setOnClickListener(this);
		} else {
			skip_timer.setOnClickListener(null);
		}
		long startTime = getStartTimeForTimer();
		timer = new CountdownTimer(startTime, 1000, timer_text,
				"timer_multiple");
		timer.start();
	}

	public static void enableNextButton(boolean isEnable) {
		if (isEnable) {
			nextBtn.setEnabled(true);
			nextBtn.setBackgroundResource(R.drawable.red_background);
		} else {
			nextBtn.setEnabled(false);
			nextBtn.setBackgroundResource(R.drawable.dark_grey_background);
		}

	}

	private static long getStartTimeForTimer() {
		Pattern pattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");
		Matcher matcher = pattern.matcher(currentScreen.getTime());
		
		if (matcher.matches()) {
			return Long.parseLong(matcher.group(1)) * 3600000L
					+ Long.parseLong(matcher.group(2)) * 60000
					+ Long.parseLong(matcher.group(3)) * 1000;
		}
		return 0;
	}

	private void initUI() {
		screenTitle = (TextView) mRootView.findViewById(R.id.risk_assess_text);
		progressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar);
		progress_step_text = (TextView) mRootView
				.findViewById(R.id.progress_step_text);
		linearLayout = (LinearLayout) mRootView.findViewById(R.id.imageslinear);
		overhead_text = (TextView) mRootView.findViewById(R.id.overhead_text);
		question_text = (TextView) mRootView.findViewById(R.id.question_text);
		next_update_text = (TextView) mRootView
				.findViewById(R.id.next_update_text);
		timer_text = (TextView) mRootView.findViewById(R.id.timer_text);
		skip_timer = (LinearLayout) mRootView.findViewById(R.id.skip_timer);
		capture_imageButton = (ImageButton) mRootView
				.findViewById(R.id.capture_imageButton);
		capture_imageButton.setOnClickListener(this);
		saveBtn = (Button) mRootView.findViewById(R.id.saveBtn);
		saveBtn.setOnClickListener(this);
		nextBtn = (Button) mRootView.findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(this);
	}

	private void sendDetailsOrSaveCapturedImageInBacklogDb(
			ImageObject imageObject) {
		if (Utils.isInternetAvailable(getActivity())) {
			sendWorkImageToServer(imageObject);
		} else {
			JobViewerDBHandler.saveImage(getActivity(), imageObject);
		}

	}

	private synchronized void sendWorkImageToServer(ImageObject imageObject) {
		ContentValues data = new ContentValues();
		data.put("temp_id", imageObject.getImageId());
		data.put("category", "surveys");
		data.put("image_string", imageObject.getImage_string());
		Log.i("Android", "Image 19 :" +imageObject.getImage_string());
		data.put("image_exif", imageObject.getImage_exif());

		Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
				+ CommsConstant.SURVEY_PHOTO_UPLOAD, data,
				getSendWorkImageHandler(imageObject));

	}

	private Handler getSendWorkImageHandler(final ImageObject imageObject) {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					/*
					 * Intent intent = new Intent(getActivity(),
					 * RiskAssessmentActivity.class); startActivity(intent);
					 */
					break;
				case HttpConnection.DID_ERROR:
					/*
					 * String error = (String) msg.obj; VehicleException
					 * exception = GsonConverter .getInstance()
					 * .decodeFromJsonString(error, VehicleException.class);
					 * ExceptionHandler.showException(getActivity(), exception,
					 * "Info");
					 */
					// Utils.saveWorkImageInBackLogDb(getActivity(),
					// imageObject);
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nextBtn:
			timer.cancel();
			for (int i = 0; i < currentScreen.getImages().length; i++) {

				if (!Utils.isNullOrEmpty(currentScreen.getImages()[i]
						.getTemp_id())) {
					try {
						ImageObject imageObject = JobViewerDBHandler
								.getImageById(getActivity(), currentScreen
										.getImages()[i].getTemp_id());
						sendDetailsOrSaveCapturedImageInBacklogDb(imageObject);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			ConfinedQuestionManager.getInstance().updateScreenOnQuestionMaster(
					currentScreen);
			ConfinedQuestionManager.getInstance().loadNextFragment(
					currentScreen.getButtons().getButton()[2].getActions()
							.getClick().getOnClick());
			break;
		case R.id.saveBtn:
			timer.cancel();
			ConfinedQuestionManager.getInstance().saveAssessment("Confined");
			((BaseActivity) getActivity()).finish();
			break;
		case R.id.skip_timer:
			if (currentScreen.isAllow_skip()) {
				currentScreen.setTimer_skipped(true);
				timer.cancel();
				enableNextButton(true);
			}
			break;
		case R.id.capture_imageButton:
			addPicObjectInScreenIfRequired();
			file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "image.jpg");
			Intent intent = new Intent(
					com.jobviewer.util.Constants.IMAGE_CAPTURE_ACTION);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent,
					com.jobviewer.util.Constants.RESULT_CODE);
			break;
		default:
			break;
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
					String base64 = "";
					try {
						base64 = Utils.bitmapToBase64String(rotateBitmap);
					} catch (OutOfMemoryError oome) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 50,
								baos);
						byte[] b = baos.toByteArray();
						base64 = Base64.encodeToString(b, Base64.DEFAULT);
					}
					imageObject.setImage_string(base64);
					Log.i("Android", "Image 3 :"+imageObject.getImage_string());
					imageString = base64;
					currentScreen.getImages()[i].setTemp_id(generateUniqueID);
					JobViewerDBHandler.saveImage(getActivity(), imageObject);
					break;
				}
			}
			if (!Utils.isNullOrEmpty(imageString)) {
				loadImages(Utils.getbyteArrayFromBase64String(imageString));
			}
			imageCount++;
			// checkAndEnableNextButton();
		}

	}

	private void loadImages(byte[] getbyteArrayFromBase64String) {
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				310, 220);
		layoutParams.setMargins(0, 30, 0, 0);
		mCapturedImage = new ImageView(getActivity());
		Glide.with(getActivity()).load(getbyteArrayFromBase64String).asBitmap()
				.into(mCapturedImage);
		linearLayout.addView(mCapturedImage, layoutParams);
	}

	private void addPicObjectInScreenIfRequired() {
		boolean isAllImagedAdded = false;
		for (int i = 0; i < currentScreen.getImages().length; i++) {
			if (!Utils.isNullOrEmpty(currentScreen.getImages()[i].getTemp_id())) {
				isAllImagedAdded = true;
			} else {
				isAllImagedAdded = false;
			}
		}

		if (isAllImagedAdded) {
			Images[] images = new Images[currentScreen.getImages().length + 1];
			for (int i = 0; i < currentScreen.getImages().length; i++) {
				images[i] = currentScreen.getImages()[i];
			}
			images[currentScreen.getImages().length] = new Images();
			currentScreen.setImages(images);
		}

	}

	public static void showMultipleTypeScreen() {
		timer.cancel();
		int on_timer_complete = currentScreen.getOn_timer_complete();
		ConfinedQuestionManager.getInstance().showOntimerCompleteScreen(
				on_timer_complete);
	}

	public static void restartTimer() {
		long startTime = getStartTimeForTimer();
		timer = new CountdownTimer(startTime, 1000, timer_text,
				"timer_multiple");
		timer.start();
	}
}
