package com.lanesgroup.jobviewer;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GeoLocationCamera;
import com.jobviewer.util.Constants;
import com.jobviewer.util.Utils;

public class CaptureVistecActivity extends BaseActivity implements
		OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressStep, number_text;
	private Button mSave, mNext, mCaptureVistec;
	static File file;

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
			finish();
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

				Log.i("Android", "formatDateFromOnetoAnother   :" + formatDate);
				Log.i("Android", "geoLocation   :" + geoLocation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ImageObject imageObject = new ImageObject();
			String generateUniqueID = Utils.generateUniqueID(this);
			imageObject.setImageId(generateUniqueID);
			JobViewerDBHandler.saveImage(this, imageObject);
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(this);
			checkOutRemember.setVistectImageId(generateUniqueID);
			// TODO: Add server communicator for sending vistec image
			Intent intent = new Intent(CaptureVistecActivity.this,
					RiskAssessmentActivity.class);
			startActivity(intent);
		}
	}

}
