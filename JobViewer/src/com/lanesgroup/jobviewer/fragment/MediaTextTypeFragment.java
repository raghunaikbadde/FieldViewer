package com.lanesgroup.jobviewer.fragment;

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
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.db.objects.ImageSendStatusObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.Images;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.GeoLocationCamera;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.ImageUploadResponse;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.R;
import com.vehicle.communicator.HttpConnection;

import java.io.File;
import java.io.IOException;

public class MediaTextTypeFragment extends Fragment implements OnClickListener {

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
		currentScreen = QuestionManager.getInstance().getCurrentScreen();
		checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(getActivity());
		if (ActivityConstants.EXCAVATION.equalsIgnoreCase(checkOutRemember
				.getAssessmentSelected())) {
			screenTitle.setText(getActivity().getResources().getString(
					R.string.excavation_risk_str));
		} else {
			screenTitle.setText(getActivity().getResources().getString(
					R.string.non_excavation_risk_assessment_str));
		}
		mProgressStep.setText(currentScreen.get_progress() + "%");
		mProgress.setProgress(Integer.parseInt(currentScreen.get_progress()));
		questionTitle.setText(currentScreen.getTitle());
		question.setText(currentScreen.getText());
		screenTitle.setText("Excavation");
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
	}

	private void checkAndEnableNextButton() {
		int count = 0;
		for (int i = 0; i < currentScreen.getImages().length; i++) {
			if (!Utils.isNullOrEmpty(currentScreen.getImages()[i].getTemp_id())) {
				count++;
			}
		}
		if (count !=0 && count >= Integer.parseInt(currentScreen.getRequired_images())) {
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

	private void checkAndLoadSavedImages() {
		for (int i = 0; i < currentScreen.getImages().length; i++) {
			String image_string = currentScreen.getImages()[i].getTemp_id();
			if (!Utils.isNullOrEmpty(image_string)) {
				ImageObject imageById = JobViewerDBHandler.getImageById(
						getActivity(), image_string);
				/*
				 * Bitmap base64ToBitmap = Utils.base64ToBitmap(imageById
				 * .getImage_string());
				 */
				byte[] getbyteArrayFromBase64String = Utils
						.getbyteArrayFromBase64String(imageById
								.getImage_string());
				Log.i("Android", "Image 26 :"
						+ imageById.getImage_string().substring(0, 50));
				loadImages(getbyteArrayFromBase64String);
			}
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

	@SuppressWarnings("static-access")
	@Override
	public void onClick(View view) {
		if (view == mSave) {
			if ("save".equalsIgnoreCase(mSave.getText().toString())) {

				for (int i = 0; i < currentScreen.getImages().length; i++) {
					if (!Utils.isNullOrEmpty(currentScreen.getImages()[i]
							.getTemp_id())) {
						ImageObject imageObject = JobViewerDBHandler
								.getImageById(getActivity(), currentScreen
										.getImages()[i].getTemp_id());
						sendDetailsOrSaveCapturedImageInBacklogDb(imageObject);
					}
				}

				QuestionManager.getInstance().updateScreenOnQuestionMaster(
						currentScreen);

				QuestionManager.getInstance().saveAssessment(
						checkOutRemember.getAssessmentSelected());

				Intent intent = new Intent(view.getContext(),
						ActivityPageActivity.class);
				intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		} else if (view == mNext) {
			String input = mDescribe.getText().toString();
			if (Utils.isNullOrEmpty(input)) {
				currentScreen.setInput(input);
			}

			for (int i = 0; i < currentScreen.getImages().length; i++) {
				if (!Utils.isNullOrEmpty(currentScreen.getImages()[i]
						.getTemp_id())) {
					ImageObject imageObject = JobViewerDBHandler.getImageById(
							getActivity(),
							currentScreen.getImages()[i].getTemp_id());
					sendDetailsOrSaveCapturedImageInBacklogDb(imageObject);
				}
			}

			QuestionManager.getInstance().updateScreenOnQuestionMaster(
					currentScreen);
			loadNextFragement();
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

	private void sendDetailsOrSaveCapturedImageInBacklogDb(
			ImageObject imageObject) {
		if (Utils.isInternetAvailable(getActivity())) {
			sendWorkImageToServer(imageObject);
		} /*
		 * else { JobViewerDBHandler.saveImage(getActivity(), imageObject); //
		 * loadNextFragement(); }
		 */

	}

	private synchronized void sendWorkImageToServer(ImageObject imageObject) {
		ContentValues values = new ContentValues();
		values.put("temp_id", imageObject.getImageId());
		values.put("category", imageObject.getCategory());
		if (imageObject.getImage_string().contains(
				Constants.IMAGE_STRING_INITIAL)) {
			values.put("image_string", imageObject.getImage_string());
		} else {
			values.put("image_string", Constants.IMAGE_STRING_INITIAL
					+ imageObject.getImage_string());
		}
		Log.i("Android", "Image 27 :" + Constants.IMAGE_STRING_INITIAL
				+ imageObject.getImage_string().substring(0, 50));
		values.put("image_exif", imageObject.getImage_exif());
		Utils.SendHTTPRequest(getActivity(), CommsConstant.HOST
				+ CommsConstant.SURVEY_PHOTO_UPLOAD, values,
				getSaveImageHandler(imageObject));

	}

	private Handler getSaveImageHandler(final ImageObject imageObject) {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					String result = (String) msg.obj;
					ImageUploadResponse decodeFromJsonString = GsonConverter
							.getInstance().decodeFromJsonString(result,
									ImageUploadResponse.class);
					/*
					 * JobViewerDBHandler.deleteImageById(getActivity(),
					 * decodeFromJsonString.getTemp_id());
					 */
					ImageSendStatusObject imageSendStatusObject = new ImageSendStatusObject();
					imageSendStatusObject.setImageId(decodeFromJsonString
							.getTemp_id());
					imageSendStatusObject
							.setStatus(ActivityConstants.IMAGE_SEND_STATUS);
					JobViewerDBHandler.saveImageStatus(getActivity(),
							imageSendStatusObject);
					break;
				case HttpConnection.DID_ERROR:
					JobViewerDBHandler.saveImage(getActivity(), imageObject);
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

	private void loadNextFragement() {
		QuestionManager.getInstance().loadNextFragment(
				currentScreen.getButtons().getButton()[2].getActions()
						.getClick().getOnClick());
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
					imageObject.setImage_string(Utils
							.bitmapToBase64String(rotateBitmap));
					Log.i("Android", "Image 10 :"
							+ imageObject.getImage_string().substring(0, 50));
					imageString = imageObject.getImage_string();
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
