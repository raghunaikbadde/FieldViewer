package com.lanesgroup.jobviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

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
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.survey.object.util.GeoLocationCamera;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Constants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.fragment.WorkCompleteFragment;
import com.raghu.WorkPhotoUpload;
import com.vehicle.communicator.HttpConnection;

public class AddPhotosActivity extends BaseActivity implements OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressStep;
	private ImageButton mAddInfo, mStop, mUser, mClickPhoto,mCaptureCallingCard,mUpdateRiskActivity;
	private Button mSave, mLeaveSite;
	private ListView mListView;
	private View mRootView;
	private ArrayList<HashMap<String, Object>> mPhotoList;
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
		/*mAdapter = new SimpleAdapter(this, mPhotoList, R.layout.add_photo_list,
				new String[] { "picture", "time" }, new int[] {
						R.id.captured_image1, R.id.date_time_text1 });*/
		
		mAdapter = new AddPhotosAdapter(mContext, mPhotoList);
		/*mAdapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if (data == null) {
					view.setVisibility(View.GONE);
					return true;
				}
				view.setVisibility(View.VISIBLE);
				return false;
			}
		});*/

		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(mAdapter);
		mCaptureCallingCard = (ImageButton)findViewById(R.id.detail_imageButton);
		mUpdateRiskActivity = (ImageButton)findViewById(R.id.video_imageButton);
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
		mCaptureCallingCard.setOnClickListener(this);
		enableLeaveSiteButton(false);
	}

	@Override
	public void onClick(View view) {
		if (view == mSave) {

		} else if(view == mUpdateRiskActivity){
			Intent intent = new Intent(view.getContext(), UpdateRiskAssessmentActivity.class);
			startActivity(intent);
		} else if (view == mLeaveSite) {
			for(WorkPhotoUpload workPhotoToUpload : arrayListOfWokImagesUpload){
				sendDetailsOrSaveCapturedImageInBacklogDb(workPhotoToUpload.getImage(),workPhotoToUpload.getImage_exit());
			}
			showWorkCompleteFragemnt();
		} else if (view == mClickPhoto || view == mCaptureCallingCard) {
			if(view==mCaptureCallingCard){
				Toast.makeText(view.getContext(), view.getContext().getResources().getString(R.string.capture_calling_Card), Toast.LENGTH_SHORT).show();
			}
			file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "image.jpg");
			Intent intent = new Intent(
					com.jobviewer.util.Constants.IMAGE_CAPTURE_ACTION);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent,
					com.jobviewer.util.Constants.RESULT_CODE);
			//Intent intent = new Intent(Constants.IMAGE_CAPTURE_ACTION);
			//startActivityForResult(intent, Constants.RESULT_CODE);
		}
	}

	private void showWorkCompleteFragemnt() {
		getFragmentManager().beginTransaction()
				.add(android.R.id.content, new WorkCompleteFragment())
				.commit();
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
			hashMap.put("time",formatDate);
			mPhotoList.add(hashMap);
			mAdapter.notifyDataSetChanged();
			if(mPhotoList.size()>=4){
				enableLeaveSiteButton(true);
			}
			WorkPhotoUpload workPhotoUpload = new WorkPhotoUpload();
			workPhotoUpload.setImage(Utils.bitmapToBase64String(rotateBitmap));
			workPhotoUpload.setImage_exit(formatDate);
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
	
	private void sendDetailsOrSaveCapturedImageInBacklogDb(String mImageBase64,String mImage_exif_string){
		if(Utils.isInternetAvailable(this)){
			sendWorkImageToServer(mImageBase64,mImage_exif_string);
		} else {
			Utils.saveWorkImageInBackLogDb(this, mImageBase64, mImage_exif_string);
			
		}
	}
	
	private synchronized void sendWorkImageToServer(String mImageBase64,String mImage_exif_string){
		ContentValues data = new ContentValues();
		data.put("image", mImageBase64);
		data.put("image_exif", mImage_exif_string);

		Utils.SendHTTPRequest(AddPhotosActivity.this, CommsConstant.HOST
				+ CommsConstant.WORK_PHOTO_UPLOAD+"/"+Utils.work_id, data, getSendWorkImageHandler(mImageBase64,mImage_exif_string));
		
		
	}
	private Handler getSendWorkImageHandler(final String mImageBase64,final String mImage_exif_string){
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					showWorkCompleteFragemnt();
					break;
				case HttpConnection.DID_ERROR:
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(AddPhotosActivity.this, exception, "Info");
					Utils.saveWorkImageInBackLogDb(AddPhotosActivity.this, mImageBase64, mImage_exif_string);
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}
	private class AddPhotosAdapter extends BaseAdapter{
		Context mContext;
		ArrayList<HashMap<String, Object>> hashMapOfCapturedIamges;
		public AddPhotosAdapter(Context mContext,ArrayList<HashMap<String, Object>> hashMapOfCapturedIamges) {
			this.mContext = mContext;
			this.hashMapOfCapturedIamges = hashMapOfCapturedIamges;
		}
		@Override
		public int getCount() {
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
			if(convertView==null){
				convertView = getLayoutInflater().inflate(R.layout.add_photo_list, null);
				ViewHolder vh = new ViewHolder(convertView);
				vh.dateTime.setText(hashMapOfCapturedIamges.get(position).get("time").toString());
				vh.imageView.setImageBitmap((Bitmap)hashMapOfCapturedIamges.get(position).get("photo"));
				convertView.setTag(vh);
			} else {
				ViewHolder vh = (ViewHolder)convertView.getTag();
				vh.dateTime.setText(hashMapOfCapturedIamges.get(position).get("time").toString());
				vh.imageView.setImageBitmap((Bitmap)hashMapOfCapturedIamges.get(position).get("photo"));
				convertView.setTag(vh);
			}
			
			return convertView;
		}
		
		private class ViewHolder {
			public TextView dateTime;
			public ImageView imageView;
			public ViewHolder(View converView) {
				dateTime = (TextView)converView.findViewById(R.id.date_time_text1);
				imageView = (ImageView)converView.findViewById(R.id.captured_image1);
			}
		}
	}
}
