package com.lanesgroup.jobviewer;

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GeoLocationCamera;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Constants;
import com.jobviewer.util.Utils;
import com.vehicle.communicator.HttpConnection;

public class CaptureVistecActivity extends BaseActivity implements
		OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressStep, number_text;
	private Button mSave, mNext, mCaptureVistec;
	static File file;
	private String mImage_exif_string = "";
	private String mImageBase64 = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vistec_screen);
		initUI();
		updateData();
	}

	private void updateData() {
		int progress;
		CheckOutObject checkOutRemember = JobViewerDBHandler
				.getCheckOutRemember(this);
		String isPollutionSelected = checkOutRemember.getIsPollutionSelected();
		if (Utils.isNullOrEmpty(isPollutionSelected)) {
			progress = 100 / 5;
			mProgressStep.setText(getResources().getString(
					R.string.progress_step_capture));
		} else {
			progress = 100 / 6;
			mProgressStep.setText(getResources().getString(
					R.string.progress_step_capture_pollution));
		}
		mProgress.setProgress(progress * 2);
		number_text.setText(checkOutRemember.getVistecId());
	}

	private void initUI() {
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mProgressStep = (TextView) findViewById(R.id.progress_step_text);
		number_text = (TextView) findViewById(R.id.number_text);
		mCaptureVistec = (Button) findViewById(R.id.capture_vistec);
		mCaptureVistec.setOnClickListener(this);
		mSave = (Button) findViewById(R.id.button1);
		mSave.setOnClickListener(this);
		mNext = (Button) findViewById(R.id.button2);
		mNext.setEnabled(false);
		mNext.setBackgroundResource(R.drawable.dark_grey_background);
		mNext.setEnabled(false);
	}

	@Override
	public void onClick(View view) {
		if (view == mSave) {
			//finish();
		} else if (view == mNext) {
				
		} else if (view == mCaptureVistec) {
			file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "image.jpg");
			Intent intent = new Intent(
					com.jobviewer.util.Constants.IMAGE_CAPTURE_ACTION);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent, Constants.RESULT_CODE);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 500 && resultCode == RESULT_OK) {

			Bitmap photo = Utils.decodeSampledBitmapFromFile(
					file.getAbsolutePath(), 1000, 700);

			Bitmap rotateBitmap = Utils.rotateBitmap(file.getAbsolutePath(),
					photo);
			String currentImageFile = Utils.getRealPathFromURI(
					Uri.fromFile(file), this);
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
				geoLocation = Utils.getGeoLocationString(this);
				Log.i("Android", "formatDateFromOnetoAnother   :" + formatDate);
				Log.i("Android", "geoLocation   :" + geoLocation);
				mImage_exif_string = formatDate + ";"+geoLocation;
				mImageBase64 += Utils.bitmapToBase64String(rotateBitmap);
						
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ImageObject imageObject = new ImageObject();
			String generateUniqueID = Utils.generateUniqueID(this);
			imageObject.setImageId(generateUniqueID);
			imageObject.setCategory("works");
			imageObject.setImage_exif(mImage_exif_string);
			imageObject.setImage_string(mImageBase64);
			JobViewerDBHandler.saveImage(this, imageObject);
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(this);
			checkOutRemember.setVistectImageId(generateUniqueID);
			// TODO: Add server communicator for sending vistec image
			if(Utils.isInternetAvailable(CaptureVistecActivity.this)){
				/*Utils.sendCapturedImageToServer(imageObject);
				Intent intent = new Intent(CaptureVistecActivity.this,
						RiskAssessmentActivity.class);
				startActivity(intent);*/
				sendVistecImageToServer(imageObject);
			} else {
				JobViewerDBHandler.saveAddPhotoImage(
						CaptureVistecActivity.this, imageObject);

				Utils.saveWorkImageInBackLogDb(CaptureVistecActivity.this, imageObject);
				Intent intent = new Intent(CaptureVistecActivity.this,
						RiskAssessmentActivity.class);
				startActivity(intent);
			}
			
		}
	}
	private void sendVistecImageToServer(ImageObject imageObject){
		Utils.startProgress(CaptureVistecActivity.this);
		ContentValues data = new ContentValues();
		data.put("temp_id", imageObject.getImageId());

		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.WORK_PHOTO_UPLOAD+"/"+Utils.work_id, data, getSendVisecImageHandler(imageObject));
	}
	private Handler getSendVisecImageHandler(final ImageObject imageObject){
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					try{
						Utils.StopProgress();
					}catch (Exception e) {
						
					}
					String str = JobViewerDBHandler.getJSONFlagObject(getApplicationContext());
					if(Utils.isNullOrEmpty(str)){
						str = "{}";
					}
					try{
						JSONObject jsonObject = new JSONObject(str);
						if(jsonObject.has(Constants.CAPTURE_VISTEC_SCREEN)){
							jsonObject.remove(Constants.CAPTURE_VISTEC_SCREEN);
						}
						
						jsonObject.put(Constants.CAPTURE_VISTEC_SCREEN, false);
						String jsonString = jsonObject.toString();
						JobViewerDBHandler.saveFlaginJSONObject(getApplicationContext(), jsonString);
					}catch(Exception e){
						
					}
					JobViewerDBHandler.saveAddPhotoImage(
							CaptureVistecActivity.this, imageObject);
					Intent intent = new Intent(CaptureVistecActivity.this,
							RiskAssessmentActivity.class);
					startActivity(intent);
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(CaptureVistecActivity.this, exception, "Info");
					Utils.saveWorkImageInBackLogDb(CaptureVistecActivity.this, imageObject);
	//				saveVistecImageInBackLogDb();
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}
	
	@Override
	public void onBackPressed() {
		String str = JobViewerDBHandler.getJSONFlagObject(this);
		if(Utils.isNullOrEmpty(str)){
			str = "{}";
		}
			try{
				JSONObject jsonObject = new JSONObject(str);
				jsonObject.put(Constants.CAPTURE_VISTEC_SCREEN, true);
				String jsonString = jsonObject.toString();
				JobViewerDBHandler.saveFlaginJSONObject(this, jsonString);
			}catch(Exception e){
				
			}
		
		
		
		Intent intent = new Intent(this,ActivityPageActivity.class);
		intent.putExtra(Constants.CAPTURE_VISTEC_SCREEN, Constants.CAPTURE_VISTEC_SCREEN);
		startActivity(intent);
	}
	
}
