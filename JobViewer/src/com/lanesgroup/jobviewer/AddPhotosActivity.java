package com.lanesgroup.jobviewer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jobviewer.comms.CommsConstant;
import com.jobviewer.confined.ConfinedAssessmentQuestionsActivity;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GeoLocationCamera;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Constants;
import com.jobviewer.util.JobViewerSharedPref;
import com.jobviewer.util.Utils;
import com.jobviwer.request.object.WorkPhotoUpload;
import com.lanesgroup.jobviewer.fragment.WorkCompleteFragment;
import com.vehicle.communicator.HttpConnection;

public class AddPhotosActivity extends BaseActivity implements OnClickListener {

	public static ArrayList<WorkPhotoUpload> arrayListOfWokImagesUpload = new ArrayList<WorkPhotoUpload>();
	private static File file;
	private TextView mVistecNumber, progress_step_text;
	private ImageButton mCaptureCallingCard, mUpdateRiskActivity,
			mConfinedSpaceRiskActivity;
	private ImageButton ib_pollution;
	private LinearLayout capture_layout;
	private Button mSave, mLeaveSite;
	private ListView mListView;
	private ArrayList<HashMap<String, Object>> mPhotoList;
	private ArrayList<byte[]> photosArrays = new ArrayList<byte[]>();
	private ArrayList<String> photosTimeStamp = new ArrayList<String>();
	private ArrayList<ImageObject> imageObjects;
	private AddPhotosAdapter mAdapter;
	private Context mContext;
	private String visTecId = "";
	private boolean isPollutionReport = false;
	private CheckOutObject checkOutObject;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_photos_screen);
		Bundle extras = getIntent().getExtras();
		if (extras != null
				&& extras.containsKey(Constants.KEY_IS_FROM_POLLUTION)) {
			isPollutionReport = extras
					.getBoolean(Constants.KEY_IS_FROM_POLLUTION);
		}
		initUI();
	}

	private void initUI() {
		mContext = this;
		mPhotoList = new ArrayList<HashMap<String, Object>>();
		imageObjects = new ArrayList<ImageObject>();

		mListView = (ListView) findViewById(R.id.listview);
		progress_step_text = (TextView) findViewById(R.id.progress_step_text);

		mCaptureCallingCard = (ImageButton) findViewById(R.id.detail_imageButton);
		mVistecNumber = (TextView) findViewById(R.id.vistec_number_text);
		checkOutObject = JobViewerDBHandler
				.getCheckOutRemember(AddPhotosActivity.this);
		if (Utils.isNullOrEmpty(checkOutObject.getIsPollutionSelected())) {
			progress_step_text.setText(getResources().getString(
					R.string.add_photo_with_out_pollution_progress));
		} else {
			progress_step_text.setText(getResources().getString(
					R.string.pollution_progress));
		}
		visTecId = checkOutObject.getVistecId();
		mVistecNumber.setText(visTecId);

		mUpdateRiskActivity = (ImageButton) findViewById(R.id.video_imageButton);
		ib_pollution = (ImageButton) findViewById(R.id.ib_pollution);
		mUpdateRiskActivity.setOnClickListener(this);
		capture_layout = (LinearLayout) findViewById(R.id.camera_layout);
		mConfinedSpaceRiskActivity = (ImageButton) findViewById(R.id.user_imageButton);
		mConfinedSpaceRiskActivity.setOnClickListener(this);
		capture_layout.setOnClickListener(this);
		ib_pollution.setOnClickListener(this);
		mSave = (Button) findViewById(R.id.button1);
		mLeaveSite = (Button) findViewById(R.id.button2);
		mLeaveSite.setOnClickListener(this);
		mSave.setOnClickListener(this);
		mCaptureCallingCard.setOnClickListener(this);

		if (isPollutionReport) {
			ib_pollution.setVisibility(View.VISIBLE);
		} else {
			ib_pollution.setVisibility(View.GONE);
		}
		List<ImageObject> imageObjects = JobViewerDBHandler
				.getAllAddCardSavedImages(mContext);
		for (ImageObject imageObject : imageObjects) {
			byte[] decodedString = Base64.decode(imageObject.getImage_string(),
					Base64.DEFAULT);
			photosTimeStamp.add(imageObject.getImage_exif());
			photosArrays.add(decodedString);
		}

		mAdapter = new AddPhotosAdapter(mContext, photosArrays, photosTimeStamp);
		mListView.setAdapter(mAdapter);
		mListView.setScrollbarFadingEnabled(false);
		if (photosArrays.size() >= 4) {
			enableLeaveSiteButton(true);
		} else {
			enableLeaveSiteButton(false);
		}
	}

	@Override
	public void onClick(View view) {
		if (view == mSave) {
			for (ImageObject imageObjectToSave : imageObjects) {
				if (imageObjectToSave == null
						|| Utils.isNullOrEmpty(imageObjectToSave
								.getImage_string())) {
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
			if(isPollutionReport)
				intent.putExtra(Constants.KEY_IS_FROM_POLLUTION,true);					
			CheckOutObject checkOutRemember = JobViewerDBHandler
					.getCheckOutRemember(mContext);
			checkOutRemember.setIsSavedOnAddPhotoScreen("true");
			JobViewerDBHandler.saveCheckOutRemember(mContext, checkOutRemember);
			startActivity(intent);

		} else if (view == mUpdateRiskActivity) {
			Intent intent = new Intent(view.getContext(),
					UpdateRiskAssessmentActivity.class);
			startActivity(intent);
		} else if (view == mLeaveSite) {
			Utils.startProgress(mContext);

			for (ImageObject imageObject : imageObjects) {
				JobViewerDBHandler.saveAddPhotoImage(AddPhotosActivity.this,
						imageObject);
			}

			if (sendWorkUploadImagesToServer()) {
				Utils.StopProgress();
				showWorkCompleteFragemnt();
			}

		} else if (view == capture_layout || view == mCaptureCallingCard) {
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
		} else if (view == mConfinedSpaceRiskActivity) {
			finish();

			Intent confinedWorkintent = new Intent(AddPhotosActivity.this,
					ConfinedAssessmentQuestionsActivity.class);
			setFlagConfinedStartedFromAddPhotos();
			confinedWorkintent.putExtra(Constants.CALLING_ACTIVITY,
					AddPhotosActivity.this.getClass().getSimpleName());
			startActivity(confinedWorkintent);
		} else if (view == ib_pollution) {
			Intent pollutionActivity = new Intent(mContext,
					PollutionActivity.class);
			pollutionActivity.putExtra(Utils.CALLING_ACTIVITY,
					ActivityConstants.ADD_PHOTOS_ACTIVITY);
			startActivity(pollutionActivity);
			finish();
		}
	}

	private boolean sendWorkUploadImagesToServer() {
		int count = 0;
		for (WorkPhotoUpload workPhotoToUpload : arrayListOfWokImagesUpload) {

			sendDetailsOrSaveCapturedImageInBacklogDb(
					workPhotoToUpload.getImage_id(), imageObjects.get(count));
			count++;
		}
		return true;
	}

	private void showWorkCompleteFragemnt() {
		Bundle bundle = new Bundle();
		bundle.putString(Constants.KEY_VISTECID, visTecId);
		// set Fragmentclass Arguments
		WorkCompleteFragment fragobj = new WorkCompleteFragment();
		fragobj.setArguments(bundle);
		getFragmentManager().beginTransaction()
				.add(android.R.id.content, fragobj).commit();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 500 && resultCode == RESULT_OK) {
			Bitmap photo = null;
			try {
				photo = Utils.decodeSampledBitmapFromFile(
						file.getAbsolutePath(), 1000, 700);
			} catch (OutOfMemoryError oome) {
				photo = Utils.decodeSampledBitmapFromFile(
						file.getAbsolutePath(), 100, 70);
			}

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

			geoLocation = Utils.getGeoLocationString(this);
			String base64 = "";
			try {
				base64 = Utils.bitmapToBase64String(rotateBitmap);
				Log.d(Utils.LOG_TAG, "base 64 captured first time");
			} catch (OutOfMemoryError oome) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
					byte[] b = baos.toByteArray();
					base64 = Base64.encodeToString(b, Base64.DEFAULT);
					Log.e(Utils.LOG_TAG, "Out of memory error catched");
				} catch (OutOfMemoryError oomme) {
					Log.e(Utils.LOG_TAG,
							"Out of memory error catched seocnd time");
				}
			}
			photosArrays.add(Utils.getbyteArrayFromBase64String(base64));
			photosTimeStamp.add(formatDate + "," + geoLocation);
			Log.d(Utils.LOG_TAG, "Add photo activity no. of photos "
					+ mPhotoList.size());

			mAdapter.notifyDataSetChanged();
			if (photosArrays.size() >= 4) {
				enableLeaveSiteButton(true);
			}
			ImageObject imageObject = new ImageObject();
			String generateUniqueID = Utils.generateUniqueID(this);
			imageObject.setImageId(generateUniqueID);
			imageObject.setCategory("work");
			imageObject.setImage_exif(formatDate + "," + geoLocation);
			imageObject.setImage_string(base64);
			imageObject.setEmail(JobViewerDBHandler.getUserProfile(mContext)
					.getEmail());
			imageObject.setReference_id(checkOutObject.getVistecId());
			Log.i("Android", "Image 7 :"
					+ imageObject.getImage_string().substring(0, 50));
			imageObjects.add(imageObject);
			WorkPhotoUpload workPhotoUpload = new WorkPhotoUpload();
			workPhotoUpload.setImage_id(imageObject.getImageId());
			arrayListOfWokImagesUpload.add(workPhotoUpload);

		}
	}

	private void enableLeaveSiteButton(boolean isEnable) {
		if (isEnable) {
			mLeaveSite.setEnabled(true);
			mLeaveSite.setOnClickListener(this);
			mLeaveSite.setBackgroundResource(R.drawable.red_background);
		} else {
			mLeaveSite.setEnabled(false);
			mLeaveSite.setOnClickListener(null);
			mLeaveSite.setBackgroundResource(R.drawable.dark_grey_background);
		}

	}

	private void sendDetailsOrSaveCapturedImageInBacklogDb(String imageId,
			ImageObject imageObject) {

		if (Utils.isInternetAvailable(this)) {
			sendWorkImageToServer(imageId, imageObject);
		} else {
			Utils.saveWorkImageInBackLogDb(AddPhotosActivity.this, imageObject);
			JobViewerDBHandler.saveImage(mContext, imageObject);
		}
	}

	private void sendWorkImageToServer(String imageId, ImageObject imageObject) {
		ContentValues data = new ContentValues();
		data.put("temp_id", imageId);
		Log.d(Utils.LOG_TAG, "Add Photos WORK PHOTO UPLOAD temp id"
				+ imageObject.getImageId());

		Utils.SendHTTPRequest(
				AddPhotosActivity.this,
				CommsConstant.HOST
						+ CommsConstant.WORK_PHOTO_UPLOAD
						+ "/"
						+ new JobViewerSharedPref().getSharedPref(context)
								.getString(JobViewerSharedPref.KEY_WORK_ID, ""),
				data, getSendWorkImageHandler(imageId, imageObject));

	}

	private Handler getSendWorkImageHandler(final String imageId,
			final ImageObject imageObject) {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					sendWorkImageToServer(imageObject);
					break;
				case HttpConnection.DID_ERROR:
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(AddPhotosActivity.this,
							exception, "Info");
					Utils.saveWorkImageInBackLogDb(AddPhotosActivity.this,
							imageObject);
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

		Utils.startProgress(this);
		for (ImageObject imageObjectToSave : imageObjects) {
			if (imageObjectToSave == null
					|| Utils.isNullOrEmpty(imageObjectToSave.getImage_string())) {
				continue;
			}
			JobViewerDBHandler.saveAddPhotoImage(mContext, imageObjectToSave);
		}

		Intent intent = new Intent(this, ActivityPageActivity.class);
		intent.putExtra(Utils.SHOULD_SHOW_WORK_IN_PROGRESS, true);
		intent.putExtra(Utils.CALLING_ACTIVITY,				
				ActivityConstants.ADD_PHOTOS_ACTIVITY);
		if(isPollutionReport)
			intent.putExtra(Constants.KEY_IS_FROM_POLLUTION,true);	
		Utils.StopProgress();
		startActivity(intent);
	}

	private void setFlagConfinedStartedFromAddPhotos() {
		String str = JobViewerDBHandler.getJSONFlagObject(mContext);
		if (Utils.isNullOrEmpty(str)) {
			str = "{}";
		}
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.has(Constants.FLAG_CONFINED_STARTED_FROM_ADD_PHOTOS)) {
				jsonObject
						.remove(Constants.FLAG_CONFINED_STARTED_FROM_ADD_PHOTOS);
			}

			jsonObject.put(Constants.FLAG_CONFINED_STARTED_FROM_ADD_PHOTOS,
					true);
			String jsonString = jsonObject.toString();
			JobViewerDBHandler.saveFlaginJSONObject(getApplicationContext(),
					jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized void sendWorkImageToServer(ImageObject imageObject) {
		ContentValues data = new ContentValues();
		data.put("temp_id", imageObject.getImageId());
		Log.d(Utils.LOG_TAG,
				"Add Photos Image Temp Id" + imageObject.getImageId());
		data.put("category", "works");
		data.put("image_string",
				Constants.IMAGE_STRING_INITIAL + imageObject.getImage_string());
		data.put("image_exif", imageObject.getImage_exif());
		data.put("email", JobViewerDBHandler.getUserProfile(mContext)
				.getEmail());
		data.put("reference_id", checkOutObject.getVistecId());
		Utils.SendHTTPRequest(this, CommsConstant.HOST
				+ CommsConstant.SURVEY_PHOTO_UPLOAD, data,
				getSendWorkImageHandler(imageObject));

	}

	private Handler getSendWorkImageHandler(final ImageObject imageObject) {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:

					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					// VehicleException exception = GsonConverter
					// .getInstance()
					// .decodeFromJsonString(error, VehicleException.class);
					Utils.saveWorkImageInBackLogDb(AddPhotosActivity.this,
							imageObject);
					// saveVistecImageInBackLogDb();
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

	private class AddPhotosAdapter extends BaseAdapter {
		Context mContext;
		ArrayList<HashMap<String, Object>> hashMapOfCapturedIamges;
		ArrayList<byte[]> photosArrays;
		ArrayList<String> photosTimeStamp;

		public AddPhotosAdapter(Context mContext, ArrayList<byte[]> photos,
				ArrayList<String> timeStamps) {
			this.mContext = mContext;
			this.photosArrays = photos;
			this.photosTimeStamp = timeStamps;
		}

		@Override
		public int getCount() {
			mSave.setText("Save (" + photosArrays.size() + ")");
			return photosArrays.size();
		}

		@Override
		public Object getItem(int position) {
			return photosArrays.get(position);
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
				String time = photosTimeStamp.get(position).substring(0,
						photosTimeStamp.get(position).indexOf(","));
				vh.dateTime.setText(time);
				Glide.with(mContext).load(photosArrays.get(position))
						.asBitmap().override(350, 420).into(vh.imageView);
				/*
				 * vh.imageView.setImageBitmap((Bitmap) hashMapOfCapturedIamges
				 * .get(position).get("photo"));
				 */
				convertView.setTag(vh);
			} else {
				ViewHolder vh = (ViewHolder) convertView.getTag();
				String time = photosTimeStamp.get(position).substring(0,
						photosTimeStamp.get(position).indexOf(","));
				vh.dateTime.setText(time);
				Glide.with(mContext).load(photosArrays.get(position))
						.asBitmap().override(350, 420).into(vh.imageView);
				/*
				 * vh.imageView.setImageBitmap((Bitmap) hashMapOfCapturedIamges
				 * .get(position).get("photo"));
				 */
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
