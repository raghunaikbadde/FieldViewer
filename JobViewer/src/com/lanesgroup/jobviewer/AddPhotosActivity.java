package com.lanesgroup.jobviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GeoLocationCamera;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.fragment.MediaTextTypeFragment;
import com.lanesgroup.jobviewer.fragment.MediaTypeFragment;
import com.lanesgroup.jobviewer.fragment.WorkCompleteFragment;
import com.raghu.WorkPhotoUpload;
import com.vehicle.communicator.HttpConnection;

public class AddPhotosActivity extends BaseActivity implements OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressStep, mVistecNumber;
	private ImageButton mAddInfo, mStop, mUser, mClickPhoto,
			mCaptureCallingCard, mUpdateRiskActivity;
	private Button mSave, mLeaveSite;
	private ListView mListView;
	private View mRootView;
	private ArrayList<HashMap<String, Object>> mPhotoList;
	private ArrayList<ImageObject> imageObjects;
	private AddPhotosAdapter mAdapter;
	private Context mContext;
	static File file;
	private ArrayList<WorkPhotoUpload> arrayListOfWokImagesUpload = new ArrayList<WorkPhotoUpload>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_photos_screen);
		initUI();
	}

	private void initUI() {
		mContext = this;
		mPhotoList = new ArrayList<HashMap<String, Object>>();
		imageObjects = new ArrayList<ImageObject>();
		/*
		 * mAdapter = new SimpleAdapter(this, mPhotoList,
		 * R.layout.add_photo_list, new String[] { "picture", "time" }, new
		 * int[] { R.id.captured_image1, R.id.date_time_text1 });
		 */
		HashMap<String, Object> hashMapOfSafeZoneBitmap = new HashMap<String, Object>();
		int count = 0;
		if(MediaTypeFragment.addPhotoActivityimageObject == null)
			MediaTypeFragment.addPhotoActivityimageObject = new ArrayList<ImageObject>();
		for (ImageObject imageObject : MediaTypeFragment.addPhotoActivityimageObject) {
			byte[] decodedString = Base64.decode(imageObject.getImage_string(),
					Base64.DEFAULT);
			Bitmap bitmapOfSafeZone = BitmapFactory.decodeByteArray(
					decodedString, 0, decodedString.length);
			hashMapOfSafeZoneBitmap = new HashMap<String, Object>();
			hashMapOfSafeZoneBitmap.put("photo", bitmapOfSafeZone);
			hashMapOfSafeZoneBitmap.put("time",
					MediaTypeFragment.timeCapturedForAddPhotosActivity
							.get(count));
			mPhotoList.add(hashMapOfSafeZoneBitmap);
			count++;
		}

		// hashMapOfSafeZoneBitmap.put(MediaTypeFragment.addPhotoActivityimageObject.getImage_exif(),
		// bitmapOfSafeZone);

		/*
		 * mAdapter.setViewBinder(new ViewBinder() { public boolean
		 * setViewValue(View view, Object data, String textRepresentation) { if
		 * (data == null) { view.setVisibility(View.GONE); return true; }
		 * view.setVisibility(View.VISIBLE); return false; } });
		 */

		mListView = (ListView) findViewById(R.id.listview);

		mCaptureCallingCard = (ImageButton) findViewById(R.id.detail_imageButton);
		mVistecNumber = (TextView) findViewById(R.id.vistec_number_text);
		CheckOutObject checkOutObject = JobViewerDBHandler
				.getCheckOutRemember(AddPhotosActivity.this);
		String visTecId = checkOutObject.getVistecId();
		mVistecNumber.setText(visTecId);

		mUpdateRiskActivity = (ImageButton) findViewById(R.id.video_imageButton);
		mUpdateRiskActivity.setOnClickListener(this);
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mProgressStep = (TextView) findViewById(R.id.progress_step_text);
		mAddInfo = (ImageButton) findViewById(R.id.detail_imageButton);
		mStop = (ImageButton) findViewById(R.id.video_imageButton);
		mUser = (ImageButton) findViewById(R.id.user_imageButton);
		mClickPhoto = (ImageButton) findViewById(R.id.capture_imageButton);
		mClickPhoto.setOnClickListener(this);
		mSave = (Button) findViewById(R.id.button1);
		mLeaveSite = (Button) findViewById(R.id.button2);
		mLeaveSite.setOnClickListener(this);
		mSave.setOnClickListener(this);
		mCaptureCallingCard.setOnClickListener(this);

		if (mPhotoList.size() >= 4) {
			enableLeaveSiteButton(true);
		} else {
			enableLeaveSiteButton(false);
		}
		List<ImageObject> imageObjects = JobViewerDBHandler
				.getAllAddCardSavedImages(mContext);
		for (ImageObject imageObject : imageObjects) {
			byte[] decodedString = Base64.decode(imageObject.getImage_string(),
					Base64.DEFAULT);
			Bitmap bitmapOfSafeZone = BitmapFactory.decodeByteArray(
					decodedString, 0, decodedString.length);
			hashMapOfSafeZoneBitmap = new HashMap<String, Object>();
			hashMapOfSafeZoneBitmap.put("photo", bitmapOfSafeZone);
			hashMapOfSafeZoneBitmap.put("time",
					imageObject.getImage_exif());
			mPhotoList.add(hashMapOfSafeZoneBitmap);
			count++;
		}

		mAdapter = new AddPhotosAdapter(mContext, mPhotoList);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void onClick(View view) {
		if (view == mSave) {
			for (ImageObject imageObjectToSave : imageObjects) {
				if(imageObjectToSave == null || Utils.isNullOrEmpty(imageObjectToSave.getImage_string())){
					continue;
				}
				JobViewerDBHandler.saveAddPhotoImage(mContext,
						imageObjectToSave);
			}
			Utils.StopProgress();

			Intent intent = new Intent(this, ActivityPageActivity.class);
			intent.putExtra(Utils.SHOULD_SHOW_WORK_IN_PROGRESS, true);
			intent.putExtra(Utils.CALLING_ACTIVITY,
					ActivityConstants.ADD_PHOTOS_ACTIVITY);
			startActivity(intent);

		} else if (view == mUpdateRiskActivity) {			
			Intent intent = new Intent(view.getContext(),
					UpdateRiskAssessmentActivity.class);
			startActivity(intent);
		} else if (view == mLeaveSite) {
			for (WorkPhotoUpload workPhotoToUpload : arrayListOfWokImagesUpload) {
				// sendDetailsOrSaveCapturedImageInBacklogDb(workPhotoToUpload.getImage(),workPhotoToUpload.getImage_exit());
			}
			showWorkCompleteFragemnt();
		} else if (view == mClickPhoto || view == mCaptureCallingCard) {
			if (view == mCaptureCallingCard) {
				Toast.makeText(
						view.getContext(),
						view.getContext().getResources()
								.getString(R.string.capture_calling_Card),
						Toast.LENGTH_SHORT).show();
			}
			file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "image.jpg");
			Intent intent = new Intent(
					com.jobviewer.util.Constants.IMAGE_CAPTURE_ACTION);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent,
					com.jobviewer.util.Constants.RESULT_CODE);
			// Intent intent = new Intent(Constants.IMAGE_CAPTURE_ACTION);
			// startActivityForResult(intent, Constants.RESULT_CODE);
		}
	}

	private void showWorkCompleteFragemnt() {
		getFragmentManager().beginTransaction()
				.add(android.R.id.content, new WorkCompleteFragment()).commit();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 500 && resultCode == RESULT_OK) {
			Bitmap photo = Utils.decodeSampledBitmapFromFile(
					file.getAbsolutePath(), 1000, 700);

			Bitmap rotateBitmap = Utils.rotateBitmap(file.getAbsolutePath(),
					photo);
			String currentImageFile = Utils.getRealPathFromURI(
					Uri.fromFile(file), AddPhotosActivity.this);
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
			HashMap<String, Object> hashMap = new HashMap<String, Object>();
			hashMap.put("photo", rotateBitmap);
			hashMap.put("time", formatDate);
			mPhotoList.add(hashMap);
			mAdapter.notifyDataSetChanged();
			if (mPhotoList.size() >= 4) {
				enableLeaveSiteButton(true);
			}

			String base64 = Utils.bitmapToBase64String(rotateBitmap);

			ImageObject imageObject = new ImageObject();
			String generateUniqueID = Utils.generateUniqueID(this);
			imageObject.setImageId(generateUniqueID);
			imageObject.setCategory("work");
			imageObject.setImage_exif(formatDate + "," + geoLocation);
			imageObject.setImage_string(base64);
			imageObjects.add(imageObject);
			WorkPhotoUpload workPhotoUpload = new WorkPhotoUpload();
			workPhotoUpload.setImage(base64);
			// workPhotoUpload.setImage_exit(formatDate);
			arrayListOfWokImagesUpload.add(workPhotoUpload);

		}
	}

	private void enableLeaveSiteButton(boolean isEnable) {
		if (isEnable) {
			mLeaveSite.setEnabled(true);
			mLeaveSite.setBackgroundResource(R.drawable.red_background);
		} else {
			mLeaveSite.setEnabled(false);
			mLeaveSite.setBackgroundResource(R.drawable.dark_grey_background);
		}

	}

	private void sendDetailsOrSaveCapturedImageInBacklogDb(String mImageBase64,
			String mImage_exif_string) {
		Utils.startProgress(mContext);
		if (Utils.isInternetAvailable(this)) {
			sendWorkImageToServer(mImageBase64, mImage_exif_string);
		} else {
			Utils.StopProgress();
			// Utils.saveWorkImageInBackLogDb(this, mImageBase64,
			// mImage_exif_string);

		}
	}

	private synchronized void sendWorkImageToServer(String mImageBase64,
			String mImage_exif_string) {
		ContentValues data = new ContentValues();
		data.put("image", mImageBase64);
		data.put("image_exif", mImage_exif_string);

		Utils.SendHTTPRequest(AddPhotosActivity.this, CommsConstant.HOST
				+ CommsConstant.WORK_PHOTO_UPLOAD + "/" + Utils.work_id, data,
				getSendWorkImageHandler(mImageBase64, mImage_exif_string));

	}

	private Handler getSendWorkImageHandler(final String mImageBase64,
			final String mImage_exif_string) {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					showWorkCompleteFragemnt();
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(AddPhotosActivity.this,
							exception, "Info");
					// Utils.saveWorkImageInBackLogDb(AddPhotosActivity.this,
					// mImageBase64, mImage_exif_string);
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MediaTypeFragment.addPhotoActivityimageObject = null;
		MediaTypeFragment.timeCapturedForAddPhotosActivity = null;
	}

	private class AddPhotosAdapter extends BaseAdapter {
		Context mContext;
		ArrayList<HashMap<String, Object>> hashMapOfCapturedIamges;

		public AddPhotosAdapter(Context mContext,
				ArrayList<HashMap<String, Object>> hashMapOfCapturedIamges) {
			this.mContext = mContext;
			this.hashMapOfCapturedIamges = hashMapOfCapturedIamges;
		}

		@Override
		public int getCount() {
			mSave.setText("Save(" + hashMapOfCapturedIamges.size() + ")");
			return hashMapOfCapturedIamges.size();
		}

		@Override
		public Object getItem(int position) {
			return hashMapOfCapturedIamges.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.add_photo_list, null);
				ViewHolder vh = new ViewHolder(convertView);
				vh.dateTime.setText(hashMapOfCapturedIamges.get(position)
						.get("time").toString());
				vh.imageView.setImageBitmap((Bitmap) hashMapOfCapturedIamges
						.get(position).get("photo"));
				convertView.setTag(vh);
			} else {
				ViewHolder vh = (ViewHolder) convertView.getTag();
				vh.dateTime.setText(hashMapOfCapturedIamges.get(position)
						.get("time").toString());
				vh.imageView.setImageBitmap((Bitmap) hashMapOfCapturedIamges
						.get(position).get("photo"));
				convertView.setTag(vh);
			}

			return convertView;
		}

		private class ViewHolder {
			public TextView dateTime;
			public ImageView imageView;

			public ViewHolder(View converView) {
				dateTime = (TextView) converView
						.findViewById(R.id.date_time_text1);
				imageView = (ImageView) converView
						.findViewById(R.id.captured_image1);
			}
		}
	}
}
