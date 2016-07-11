package com.lanesgroup.jobviewer.fragment;

import java.io.ByteArrayOutputStream;
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
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.Images;
import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.R;
import com.vehicle.communicator.HttpConnection;

public class MediaTypeFragment extends Fragment implements OnClickListener {

    public static final int RESULT_OK = -1;
    private static File file;
    private ProgressBar mProgress;
    private TextView mProgressStep, questionTitle, question, screenTitle;
    private Button mSave, mNext;
    private LinearLayout mLinearLayout;
    private View mRootView;
    private ImageView mCapturedImage;
    private LinearLayout linearLayout;
    private int imageCount = 0;
    private Screen currentScreen;
    private CheckOutObject checkOutRemember;

	/*
     * private boolean formwardImageToAddPhotosActivity = false; public static
	 * ArrayList<ImageObject> addPhotoActivityimageObject; public static
	 * ArrayList<String> timeCapturedForAddPhotosActivity = new
	 * ArrayList<String>();
	 */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mRootView = inflater.inflate(R.layout.question_media_fragment,
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
		/*
		 * if (currentScreen .getText() .toString() .equalsIgnoreCase(
		 * getResources().getString(R.string.capture_safe_zone))) {
		 * formwardImageToAddPhotosActivity = true; }
		 */
        try {
            checkAndLoadSavedImages();
        } catch (Exception e) {
            e.printStackTrace();
        }
        checkAndEnableNextButton();

        com.jobviewer.survey.object.Button[] buttons = currentScreen
                .getButtons().getButton();

        for (com.jobviewer.survey.object.Button button : buttons) {
            if (ActivityConstants.TRUE
                    .equalsIgnoreCase(button.getDisplay())
                    && !"next".equalsIgnoreCase(button.getName())) {
                mSave.setText(getResources().getString(
                        Utils.getButtonText(button.getName())));
                break;
            }
        }

    }

    private void checkAndLoadSavedImages() {
        for (int i = 0; i < currentScreen.getImages().length; i++) {
            String image_string = currentScreen.getImages()[i].getTemp_id();
            if (!Utils.isNullOrEmpty(image_string)) {
                ImageObject imageById = JobViewerDBHandler.getImageById(
                        getActivity(), image_string);
                String image = imageById.getImage_string();
                if (image.contains(Constants.IMAGE_STRING_INITIAL)) {
                    image = image.replace(Constants.IMAGE_STRING_INITIAL, "");
                }
                byte[] getbyteArrayFromBase64String = Utils
                        .getbyteArrayFromBase64String(image);
                loadImages(getbyteArrayFromBase64String);
            }
        }

    }

    private void checkAndEnableNextButton() {
        int count = 0;
        for (int i = 0; i < currentScreen.getImages().length; i++) {
            if (!Utils.isNullOrEmpty(currentScreen.getImages()[i].getTemp_id())) {
                count++;
            }
        }
        if (count >= Integer.parseInt(currentScreen.getRequired_images())) {
            enableNextButton(true);
        } else {
            enableNextButton(false);
        }
    }

    private void initUI() {
        mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
        mProgressStep = (TextView) mRootView
                .findViewById(R.id.progress_step_text);
        screenTitle = (TextView) mRootView.findViewById(R.id.screenTitle);
        questionTitle = (TextView) mRootView.findViewById(R.id.questionTitle);
        question = (TextView) mRootView.findViewById(R.id.question);
        mLinearLayout = (LinearLayout) mRootView
                .findViewById(R.id.camera_layout);
        linearLayout = (LinearLayout) mRootView.findViewById(R.id.imageslinear);
        mLinearLayout.setOnClickListener(this);

        mSave = (Button) mRootView.findViewById(R.id.button1);
        mSave.setOnClickListener(this);
        mNext = (Button) mRootView.findViewById(R.id.button2);
        mNext.setOnClickListener(this);

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

                    if (!Utils.isNullOrEmpty(currentScreen.getImages()[i]
                            .getTemp_id())) {
                        try {
                            ImageObject imageObject = JobViewerDBHandler
                                    .getImageById(getActivity(), currentScreen
                                            .getImages()[i].getTemp_id());
							/*
							 * Log.d(Utils.LOG_TAG,
							 * "formwardImageToAddPhotosActivity " +
							 * formwardImageToAddPhotosActivity);
							 */
                            // if (formwardImageToAddPhotosActivity) {
							/*
							 * JobViewerDBHandler.saveAddPhotoImage(
							 * getActivity(), imageObject);
							 */
                            // }
                            sendDetailsOrSaveCapturedImageInBacklogDb(imageObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
			/* addPhotoActivityimageObject = new ArrayList<ImageObject>(); */
            for (int i = 0; i < currentScreen.getImages().length; i++) {

                if (!Utils.isNullOrEmpty(currentScreen.getImages()[i]
                        .getTemp_id())) {
                    try {
                        ImageObject imageObject = JobViewerDBHandler
                                .getImageById(getActivity(), currentScreen
                                        .getImages()[i].getTemp_id());
						/*
						 * Log.d(Utils.LOG_TAG,
						 * "formwardImageToAddPhotosActivity " +
						 * formwardImageToAddPhotosActivity);
						 */
						/*
						 * if (formwardImageToAddPhotosActivity) {
						 * JobViewerDBHandler.saveAddPhotoImage(getActivity(),
						 * imageObject); }
						 */

                        sendDetailsOrSaveCapturedImageInBacklogDb(imageObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            QuestionManager.getInstance().updateScreenOnQuestionMaster(
                    currentScreen);
            QuestionManager.getInstance().loadNextFragment(
                    currentScreen.getButtons().getButton()[2].getActions()
                            .getClick().getOnClick());
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

    private void addPicObjectInScreenIfRequired() {
        boolean isAllImagedAdded = false;
        for (int i = 0; i < currentScreen.getImages().length; i++) {
            isAllImagedAdded = !Utils
                    .isNullOrEmpty(currentScreen.getImages()[i].getTemp_id());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
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
//				GeoLocationCamera geoLocationCamera = new GeoLocationCamera(
//						exif);
//				geoLocation = geoLocationCamera.toString();

				/*
				 * if (formwardImageToAddPhotosActivity)
				 * timeCapturedForAddPhotosActivity.add(formatDate);
				 */
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
                    Log.i("Android", "Image 11 :"
                            + imageObject.getImage_string().substring(0, 50));
                    imageString = base64;
                    currentScreen.getImages()[i].setTemp_id(generateUniqueID);
                    imageObject.setEmail(JobViewerDBHandler.getUserProfile(getActivity())
							.getEmail());
					imageObject.setReference_id(checkOutRemember.getVistecId());
					imageObject.setStage(currentScreen.getTitle());
                    JobViewerDBHandler.saveImage(getActivity(), imageObject);
                    break;
                }
            }
            if (!Utils.isNullOrEmpty(imageString)) {
                loadImages(Utils.getbyteArrayFromBase64String(imageString));
            }
            imageCount++;
            checkAndEnableNextButton();
        }
    }

    private void loadImages(byte[] getbyteArrayFromBase64String) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        		350, 420);
        layoutParams.setMargins(15, 15, 15, 15);
        mCapturedImage = new ImageView(getActivity());
        Glide.with(getActivity()).load(getbyteArrayFromBase64String).asBitmap()
                .override(350, 420).into(mCapturedImage);
        linearLayout.addView(mCapturedImage, layoutParams);
    
    }

    private void sendDetailsOrSaveCapturedImageInBacklogDb(
            ImageObject imageObject) {
        if (Utils.isInternetAvailable(getActivity())) {
            sendWorkImageToServer(imageObject);
        } /*
		 * else { JobViewerDBHandler.saveImage(getActivity(), imageObject); }
		 */
    }

    private synchronized void sendWorkImageToServer(ImageObject imageObject) {
        ContentValues data = new ContentValues();
        data.put("temp_id", imageObject.getImageId());
        data.put("category", "surveys");

        if (imageObject.getImage_string().contains(
                Constants.IMAGE_STRING_INITIAL)) {
            data.put("image_string", imageObject.getImage_string());
        } else {
            data.put("image_string", Constants.IMAGE_STRING_INITIAL
                    + imageObject.getImage_string());
        }
        data.put("image_exif", imageObject.getImage_exif());
        data.put("email", JobViewerDBHandler.getUserProfile(getActivity())
				.getEmail());
        data.put("reference_id", checkOutRemember.getVistecId());
        data.put("stage", currentScreen.getTitle());
        
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
                        Utils.saveWorkImageInBackLogDb(getActivity(), imageObject);
                        JobViewerDBHandler.saveImage(getActivity(), imageObject);
                        break;
                    default:
                        break;
                }
            }
        };
        return handler;
    }
}
