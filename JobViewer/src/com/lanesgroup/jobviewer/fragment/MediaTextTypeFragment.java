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
import android.widget.Toast;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.Images;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.GeoLocationCamera;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.ImageUploadResponse;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.R;
import com.vehicle.communicator.HttpConnection;

public class MediaTextTypeFragment extends Fragment implements OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressStep, screenTitle, questionTitle, question;
	private EditText mDescribe;
	private Button mSave, mNext;
	private LinearLayout mLinearLayout;
	private View mRootView;
	private ImageView mCapturedImage;
	int imageCount = 0;
	public static final int RESULT_OK = -1;
	Screen currentScreen;
	static File file;
	CheckOutObject checkOutRemember;

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
	}

	private void initUI() {
		mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
		mProgressStep = (TextView) mRootView
				.findViewById(R.id.progress_step_text);
		screenTitle = (TextView) mRootView.findViewById(R.id.screenTitle);
		questionTitle = (TextView) mRootView.findViewById(R.id.questionTitle);
		question = (TextView) mRootView.findViewById(R.id.question);
		mDescribe = (EditText) mRootView.findViewById(R.id.describe_edittext);

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
			if (!Utils.isNullOrEmpty(currentScreen.getImages()[i]
					.getImage_string())) {
				count++;
			}
		}
		if (count >= Integer.parseInt(currentScreen.getRequired_images())) {
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

	@SuppressWarnings("static-access")
	@Override
	public void onClick(View view) {
		if (view == mSave) {
			if ("save".equalsIgnoreCase(mSave.getText().toString())) {
				
				for (int i = 0; i < currentScreen.getImages().length; i++) {
					ImageObject imageObject = new ImageObject();
					String generateUniqueID = Utils.generateUniqueID(getActivity());
					imageObject.setImageId(generateUniqueID);
					imageObject.setCategory("surveys");
					imageObject.setImage_exif(currentScreen.getImages()[i].getImage_exif());
					imageObject.setImage_string(currentScreen.getImages()[i]
							.getImage_string());
					
					currentScreen.getImages()[i].setImage_string(generateUniqueID);
					JobViewerDBHandler.saveImage(view.getContext(), imageObject);
					
					sendDetailsOrSaveCapturedImageInBacklogDb(imageObject);
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
				ImageObject imageObject = new ImageObject();
				String generateUniqueID = Utils.generateUniqueID(getActivity());
				imageObject.setImageId(generateUniqueID);
				imageObject.setImage_string(currentScreen.getImages()[i]
						.getImage_string());
				imageObject.setCategory("surveys");
				imageObject.setImage_exif(currentScreen.getImages()[i]
						.getImage_exif());
				currentScreen.getImages()[i].setImage_string(generateUniqueID);
				sendDetailsOrSaveCapturedImageInBacklogDb(imageObject);
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
		} else {
			JobViewerDBHandler.saveImage(getActivity(), imageObject);
			// loadNextFragement();
		}

	}

	private synchronized void sendWorkImageToServer(ImageObject imageObject) {
		ContentValues values = new ContentValues();
		values.put("temp_id", imageObject.getImageId());
		values.put("category", imageObject.getCategory());
		values.put("image_string", imageObject.getImage_string());
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
					JobViewerDBHandler.deleteImageById(getActivity(),
							decodeFromJsonString.getTemp_id());

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
			if (!Utils.isNullOrEmpty(currentScreen.getImages()[i]
					.getImage_string())) {
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
			Log.i("Android", "");
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		LinearLayout linearLayout = (LinearLayout) mRootView
				.findViewById(R.id.imageslinear);
		if (requestCode == 500 && resultCode == RESULT_OK) {
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

			for (int i = 0; i < currentScreen.getImages().length; i++) {
				if (Utils.isNullOrEmpty(currentScreen.getImages()[i]
						.getImage_string())) {
					String image_exif = formatDate + "," + geoLocation;
					currentScreen.getImages()[i].setImage_exif(image_exif);
					String image_base_64 = Utils
							.bitmapToBase64String(rotateBitmap);
					currentScreen.getImages()[i].setImage_string(image_base_64);
					// sendDetailsOrSaveCapturedImageInBacklogDb(image_base_64,image_exif);
					break;
				}
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					310, 220);
			layoutParams.setMargins(0, 30, 0, 0);
			mCapturedImage = new ImageView(getActivity());
			mCapturedImage.setImageBitmap(rotateBitmap);
			linearLayout.addView(mCapturedImage, layoutParams);
			imageCount++;
			Toast.makeText(getActivity(), "Number of images are " + imageCount,
					3000).show();
			checkAndEnableNextButton();

		}
	}
}
