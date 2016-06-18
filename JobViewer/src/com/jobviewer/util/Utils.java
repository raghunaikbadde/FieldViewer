package com.jobviewer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jobviewer.adapter.MultiChoiceAdapter;
import com.jobviewer.adapter.MultiChoiceItem;
import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.db.objects.ImageSendStatusObject;
import com.jobviewer.network.SendImagesOnBackground;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.ImageUploadResponse;
import com.jobviwer.service.AppKillService;
import com.lanesgroup.jobviewer.BaseActivity;
import com.lanesgroup.jobviewer.LauncherActivity;
import com.lanesgroup.jobviewer.PollutionActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.WelcomeActivity;
import com.raghu.TimeSheetServiceRequests;
import com.raghu.WorkPhotoUpload;
import com.vehicle.communicator.HttpConnection;

public class Utils {
	public static AlarmManager alarmMgr;
	public static final String PROGRESS_1_TO_3 = "Step 1 of 3";
	public static final String PROGRESS_2_TO_3 = "Step 2 of 3";
	public static final String PROGRESS_1_TO_2 = "Step 1 of 2";
	public static final String PROGRESS_2_TO_2 = "Step 2 of 2";
	public static final String PROGRESS_1_TO_1 = "Step 1 of 1";
	public static final String CALLING_ACTIVITY = "callingActivity";
	public static final String SHOULD_SHOW_WORK_IN_PROGRESS = "souldShowWorkInProgress";

	public static final String SHIFT_START = "SHIFT_START";
	public static final String CALL_START = "CALL_START";
	public static final String SHIFT_END = "SHIFT_END";
	public static final String END_CALL = "END_CALL";

	public static final String REQUEST_TYPE_WORK = "WORK";
	public static final String REQUEST_TYPE_UPLOAD = "UPLOAD";
	public static final String REQUEST_TYPE_TIMESHEET = "TIMESHEET";
	public static final String REQUEST_TYPE_POLLUTION = "POLLUTION";

	public static String work_completed_at = "";
	public static String work_engineer_id = "123322";
	public static String work_status = "New";
	public static String work_status_stopped = "Stopped";
	public static String work_status_completed = "Completed";
	public static String work_flooding_status = null;
	public static String work_DA_call_out = "No Call Made";
	public static boolean work_is_redline_captured = false;

	public static String work_id = "";
	public static String UPDATE_RISK_ASSESSMENT_ACTIVITY = "UPDATE_RISK_ASSESSMENT_ACTIVITY";

	public static final String LOG_TAG = "JV";

	static Dialog progressDialog;
	public static CheckOutObject checkOutObject;
	public static TimeSheetRequest timeSheetRequest = null;
	public static TimeSheetRequest endTimeRequest = null;
	public static TimeSheetRequest startTravelTimeRequest = null;
	public static TimeSheetRequest endTravelTimeRequest = null;
	public static TimeSheetRequest startShiftTimeRequest = null;
	public static TimeSheetRequest endShiftRequest = null;

	public static TimeSheetRequest workStartTimeSheetRequest = null;
	public static TimeSheetRequest workEndTimeSheetRequest = null;
	public static String lastest_work_started_at = "";
	public static String lastest_call_started_at = "";
	public static String lastest_shift_started_at = "";

	public static String[] mActivityList = { "Blockage", "CCTV", "Line Clean",
			"Pump Down", "SFOC", "Clean Up", "SROPR", "Enable", "Private",
			"Cover", "Dig Down", "Make Safe", "Lining", "Well Clean" };

	public static String[] mFloodingList = { "No Flooding", "Internal",
			"External", "Internal and External" };

	public static String[] mLandPollutionList = { "Less than 25m/sq",
			"20-50m/sq", "50-100m/sq", "Greater than 100m/sq" };

	public static String[] mLandAffectedList = { "Park", "Gardens",
			"Dry ditch", "Highway" };

	public static String[] mExtentOfWaterList = { "Less than 10m", "10-50m",
			"50-100m", "Greater than 100m" };

	public static String[] mWaterBodyList = { "Pond", "Ditch", "Stream/Brook",
			"River", "Reservoir" };

	public static String[] mIndicativeCause = { "3rd Party", "Blockage",
			"Hydraulic overload", "Failed asset" };

	public static String[] mAmmonia = { "0 Mg/l", "1-3 Mg/l", "3-6 Mg/l",
			"Greater than 6 Mg/l" };

	public static String[] mFishKill = { "None", "1-10", "10-50", "50-100",
			"Greater than 100" };

	public static TimeSheetRequest callStartTimeRequest = null;
	public static TimeSheetRequest callEndTimeRequest = null;
	static int notificationId = 1000;
	private static NotificationManager myNotificationManager;
	public static boolean isExitApplication = false;

	public static void SendHTTPRequest(Context context, String url,
			ContentValues data, Handler handler) {
		new HttpConnection(handler).post(url, data);
	}

	@SuppressWarnings("deprecation")
	public static boolean isInternetAvailable(Context context) {

		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}

		return false;
	}

	public static boolean isNullOrEmpty(String string) {
		if (string == null || string.equalsIgnoreCase(""))
			return true;
		return false;
	}

	public static String getMailId(Context context) {
		String strGmail = "";
		Account[] accounts = AccountManager.get(context).getAccounts();
		Log.e("", "Size: " + accounts.length);
		for (Account account : accounts) {

			String possibleEmail = account.name;
			String type = account.type;

			if (type.equals("com.google")
					&& possibleEmail.contains("lanesgroup.com")) {
				strGmail = possibleEmail;
				break;
			}
		}
		// return strGmail;
		return "richard.stenson@lanesgroup.com";
	}

	public static String getIMEI(Context context) {
		String imei = "";

		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();
		return imei;
	}

	public static void startProgress(Context context) {
		progressDialog = new Dialog(context, R.style.AppCompatDialogStyle);
		progressDialog.setContentView(R.layout.custom_progress_dialog);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	public static void StopProgress() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	public static String getCurrentDateAndTime() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = null;
		System.out.println("Current time => " + c.getTime());
		// df = new SimpleDateFormat("d MMM',' yyyy HH:mm");
		// df = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
		df = new SimpleDateFormat("HH:mm:ss dd MMM yyyy");
		String formattedDate = df.format(c.getTime());
		return formattedDate;
	}
	
	public static String formattedDateFromMillis(String millis){
		long milliSec = Long.valueOf(millis);
		
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss dd MMM yyyy");;
		String formattedDate = df.format(milliSec);
		return formattedDate;
	}
	
	public static String getMillisFromFormattedDate(String dateToBeConverted){
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd MMM yyyy");
		Date date = new Date();
		try {
			date = sdf.parse(dateToBeConverted);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return String.valueOf(date.getTime());
	}

	public static String convertTimeOneToAnotherFormat(String time,
			String fromFormat, String resultFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(fromFormat);
		Date fromDate = null;
		try {
			fromDate = sdf.parse(time);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		SimpleDateFormat formatter = new SimpleDateFormat(resultFormat);

		return formatter.format(fromDate);
	}

	public static String formatDate(String date) {
		String resultformat = "HH:mm:ss dd MMM yyyy";
		String givenformat = "yyyy:MM:dd HH:mm:ss";
		String result = "";
		SimpleDateFormat sdf;
		SimpleDateFormat sdf1;

		try {
			sdf = new SimpleDateFormat(givenformat);
			sdf1 = new SimpleDateFormat(resultformat);
			result = sdf1.format(sdf.parse(date));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} finally {
			sdf = null;
			sdf1 = null;
		}

		return result;
	}

	public static String bitmapToBase64String(Bitmap bitmap) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
		byte[] byteArray = byteArrayOutputStream.toByteArray();
		return Base64.encodeToString(byteArray, Base64.DEFAULT);
	}

	public static Bitmap base64ToBitmap(String base64String) {
		byte[] imageAsBytes = Base64.decode(base64String.getBytes(),
				Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(imageAsBytes, 0,
				imageAsBytes.length);
	}

	public static byte[] getbyteArrayFromBase64String(String base64String) {
		return Base64.decode(base64String.getBytes(), Base64.DEFAULT);
	}

	public static String generateUniqueID(Context context) {
		long timeInMilli = Calendar.getInstance().getTimeInMillis();
		return getIMEI(context) + timeInMilli;
	}

	public static void shakeAnimation(Context context, LinearLayout layout) {
		Animation shake;
		shake = AnimationUtils.loadAnimation(context, R.anim.shake_animation);
		layout.setAnimation(shake);
	}

	public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth,
			int reqHeight) { // BEST QUALITY MATCH

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize, Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		int inSampleSize = 1;

		if (height > reqHeight) {
			inSampleSize = Math.round((float) height / (float) reqHeight);
		}
		int expectedWidth = width / inSampleSize;

		if (expectedWidth > reqWidth) {
			// if(Math.round((float)width / (float)reqWidth) > inSampleSize) //
			// If bigger SampSize..
			inSampleSize = Math.round((float) width / (float) reqWidth);
		}

		options.inSampleSize = inSampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(path, options);
	}

	public static Bitmap rotateBitmap(String src, Bitmap bitmap) {
		try {
			int orientation = getExifOrientation(src);

			if (orientation == 1) {
				return bitmap;
			}

			Matrix matrix = new Matrix();
			switch (orientation) {
			case 2:
				matrix.setScale(-1, 1);
				break;
			case 3:
				matrix.setRotate(180);
				break;
			case 4:
				matrix.setRotate(180);
				matrix.postScale(-1, 1);
				break;
			case 5:
				matrix.setRotate(90);
				matrix.postScale(-1, 1);
				break;
			case 6:
				matrix.setRotate(90);
				break;
			case 7:
				matrix.setRotate(-90);
				matrix.postScale(-1, 1);
				break;
			case 8:
				matrix.setRotate(-90);
				break;
			default:
				return bitmap;
			}

			try {
				Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0,
						bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				bitmap.recycle();
				return oriented;
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				return bitmap;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	private static int getExifOrientation(String src) throws IOException {
		int orientation = 1;

		ExifInterface exif = new ExifInterface(src);
		String orientationString = exif
				.getAttribute(ExifInterface.TAG_ORIENTATION);

		try {
			orientation = Integer.parseInt(orientationString);
		} catch (NumberFormatException e) {
		}

		return orientation;
	}

	public static String getRealPathFromURI(Uri contentURI, Activity activity) {
		Cursor cursor = activity.getContentResolver().query(contentURI, null,
				null, null, null);
		if (cursor == null) { // Source is Dropbox or other similar local file
			// path
			return contentURI.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor
					.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			return cursor.getString(idx);
		}
	}

	public static int getButtonText(String buttonText) {
		if ("save".equalsIgnoreCase(buttonText)) {
			return R.string.save;
		} else if ("resume".equalsIgnoreCase(buttonText)) {
			return R.string.resume;
		} else {
			return R.string.cancel;
		}
	}

	@SuppressWarnings("deprecation")
	public static void startNotification(Context context) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context);
		mBuilder.setContentTitle("FieldViewer is running");
		mBuilder.setContentText("Data can be sent to the Lanes office");
		// mBuilder.setTicker("Explicit: New Message Received!");
		mBuilder.setSmallIcon(R.drawable.ic_launcher);

		// Increase notification number every time a new notification arrives
		mBuilder.setNumber(1);

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(context, LauncherActivity.class);
		resultIntent.putExtra("notificationId", notificationId);

		// This ensures that navigating backward from the Activity leads out of
		// the app to Home page
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

		// Adds the back stack for the Intent
		stackBuilder.addParentStack(WelcomeActivity.class);

		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_ONE_SHOT);
		// start the activity when the user clicks the notification text
		mBuilder.setContentIntent(resultPendingIntent);

		myNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification n;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			n = mBuilder.build();
		} else {
			n = mBuilder.getNotification();
		}
		n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		// pass the Notification object to the system
		myNotificationManager.notify(notificationId, n);
	}

	public static void startService(Context context) {
		Intent intent = new Intent(context, AppKillService.class);
		context.startService(intent);
	}

	public static void stopService(Context context) {
		Intent intent = new Intent(context, AppKillService.class);
		context.stopService(intent);
	}

	public static void saveTimeSheetInBackLogTable(Context mContext,
			TimeSheetRequest timeSheetRequest, String api, String requestType) {
		TimeSheetServiceRequests startOrEndPaidTravel = new TimeSheetServiceRequests();
		startOrEndPaidTravel.setStarted_at(timeSheetRequest.getStarted_at());
		startOrEndPaidTravel.setRecord_for(timeSheetRequest.getRecord_for());
		startOrEndPaidTravel.setIs_inactive(timeSheetRequest.getIs_inactive());
		startOrEndPaidTravel
				.setIs_overriden(timeSheetRequest.getIs_overriden());
		startOrEndPaidTravel.setOverride_reason(timeSheetRequest
				.getOverride_reason());
		startOrEndPaidTravel.setOverride_comment(timeSheetRequest
				.getOverride_comment());
		startOrEndPaidTravel.setOverride_timestamp(timeSheetRequest
				.getOverride_timestamp());
		startOrEndPaidTravel
				.setReference_id(timeSheetRequest.getReference_id());
		startOrEndPaidTravel.setUser_id(timeSheetRequest.getUser_id());

		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.HOST + api);
		backLogRequest.setRequestClassName("TimeSheetServiceRequests");
		backLogRequest.setRequestJson(startOrEndPaidTravel.toString());
		backLogRequest.setRequestType(requestType);

		JobViewerDBHandler.saveBackLog(mContext, backLogRequest);
	}

	public static void saveWorkImageInBackLogDb(Context mContext,
			ImageObject imageObject) {
		WorkPhotoUpload workPhotoUpload = new WorkPhotoUpload();
		workPhotoUpload.setImage_id(imageObject.getImageId());

		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.HOST
				+ CommsConstant.WORK_PHOTO_UPLOAD + "/" + Utils.work_id);
		backLogRequest.setRequestClassName("WorkPhotoUpload");
		backLogRequest.setRequestJson(GsonConverter.getInstance()
				.encodeToJsonString(workPhotoUpload));
		backLogRequest.setRequestType(Utils.REQUEST_TYPE_UPLOAD);
		JobViewerDBHandler.saveBackLog(mContext, backLogRequest);
	}

	public static Location locationOfUser = null;

	@SuppressWarnings("static-access")
	public static Location getCurrentLocation(Context context) {

		LocationManager locationManager = (LocationManager) context
				.getSystemService(context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				locationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {

					@Override
					public void onStatusChanged(String provider, int status,
							Bundle extras) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onProviderEnabled(String provider) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onProviderDisabled(String provider) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onLocationChanged(Location location) {
						// TODO Auto-generated method stub
						locationOfUser = location;
					}
				});
		return locationOfUser;
	}

	public static void sendImagesToserver(Context context) {
		SendImagesOnBackground sendImagesOnBackground = new SendImagesOnBackground();
		sendImagesOnBackground.getAndSendImagesToServer(context);
	}

	public static boolean checkIfStartDateIsGreater(String startDate,
			String endDate) {
		SimpleDateFormat dfDate = new SimpleDateFormat(Constants.TIME_FORMAT);

		boolean b = false;

		try {
			if (dfDate.parse(startDate).before(dfDate.parse(endDate))) {
				b = true; // If start date is before end date.
			} else if (dfDate.parse(startDate).equals(dfDate.parse(endDate))) {
				b = true; // If two dates are equal.
			} else {
				b = false; // If start date is after the end date.
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return b;
	}

	public static void dailogboxSelector(final Activity activity,
			final String[] list, int resorce, final TextView seleTextView,
			String header) {

		View view = activity.getLayoutInflater().inflate(resorce, null);
		CustomDialogAdapter mAgeAndLocationAdapter = new CustomDialogAdapter(
				activity, list, seleTextView.getText().toString().trim());

		ListView listView = (ListView) view.findViewById(R.id.list);
		TextView headerTxt = (TextView) view.findViewById(R.id.dialog_info);
		headerTxt.setText(header);
		listView.setAdapter(mAgeAndLocationAdapter);

		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(view);
		dialog.show();
		dialog.setCancelable(false);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				seleTextView.setText(list[position]);
				if(activity instanceof PollutionActivity){
					((PollutionActivity) activity).validateUserInputs();
				}
				dialog.dismiss();
			}
		});
	}

	public static void createMultiSelectDialog(final Context context,
			List<MultiChoiceItem> multiChoiceItems, String title,
			final TextView multiChoiceTextView) {
		
		
		View view = ((Activity) context).getLayoutInflater().inflate(
				R.layout.multichoice_dialog_screen, null);
		final MultiChoiceAdapter adapter = new MultiChoiceAdapter(context,
				multiChoiceItems);
		ListView listView = (ListView) view.findViewById(R.id.list);
		TextView headerTxt = (TextView) view.findViewById(R.id.dialog_title);
		headerTxt.setText(title);

		Button dialog_cancel = (Button) view.findViewById(R.id.dialog_cancel);
		Button dialog_ok = (Button) view.findViewById(R.id.dialog_ok);
		listView.setAdapter(adapter);
		final Dialog dialog = new Dialog(context, R.style.dialogMultipleTheme);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(view);
		dialog.show();
		dialog.setCancelable(false);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MultiChoiceItem multiChoiceItem = adapter.getList().get(
						position);
				if (multiChoiceItem.isChecked()) {
					multiChoiceItem.setChecked(false);
				} else {
					multiChoiceItem.setChecked(true);
				}
				adapter.refresh(position, multiChoiceItem);
			}
		});
		// dialog.getWindow().setBackgroundDrawable(new
		// ColorDrawable(android.graphics.Color.TRANSPARENT));

		dialog_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();

			}
		});
		dialog_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				List<MultiChoiceItem> list = adapter.getList();
				StringBuffer sb = new StringBuffer();

				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).isChecked()) {
						if (!Utils.isNullOrEmpty(sb.toString())) {
							sb.append(",");
						}
						sb.append(list.get(i).getText());
					}

				}
				if (Utils.isNullOrEmpty(sb.toString())) {
					multiChoiceTextView.setText(v.getContext().getResources()
							.getString(R.string.select_spinner_str));
				} else {
					multiChoiceTextView.setText(sb);
				}
				dialog.dismiss();
				if(((Activity) context) instanceof PollutionActivity){			
					PollutionActivity pollutionActivity = (PollutionActivity)((Activity) context);
					pollutionActivity.validateUserInputs();
				}
				
			}
		});
	}
	/**
	 * Returns number of Hours and Minutes from number of milliseconds
	 * Ex:long millis = 876738162L; 
	 * getTimeInHHMMFromNumberOfMillis(millis)
	 * gives output of 243h 32 m
	 * @param milliseconds
	 * @return
	 */
	public static String getTimeInHHMMFromNumberOfMillis(long milliseconds){
		String HHMMString = "";
		if (milliseconds >= 1000){
	        int seconds = (int) (milliseconds / 1000) % 60;
	        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
	        int hours = (int) ((milliseconds / (1000 * 60 * 60)));
	        HHMMString = hours + "h "+minutes +" m";
	    } else{
	    	HHMMString = "Less than a second ago.";
	    }
		return HHMMString;
	}
	
	
	public static void sendCapturedImageToServer(
			ImageObject imageObject) {
		if (Utils.isInternetAvailable(BaseActivity.context)) {
			sendWorkImageToServer(imageObject);
		}
	}
	
	public static void sendWorkImageToServer(ImageObject imageObject) {
		ContentValues values = new ContentValues();
		values.put("temp_id", imageObject.getImageId());
		values.put("category", imageObject.getCategory());
		values.put("image_string", imageObject.getImage_string());
		Log.i("Android", "Image 23 :"+imageObject.getImage_string().substring(0, 50));
		values.put("image_exif", imageObject.getImage_exif());
		Utils.SendHTTPRequest(BaseActivity.context, CommsConstant.HOST
				+ CommsConstant.SURVEY_PHOTO_UPLOAD, values,
				getSaveImageHandler(imageObject));

	}
	
	public static Handler getSaveImageHandler(final ImageObject imageObject) {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					String result = (String) msg.obj;
					Log.i("Android", result);
					ImageUploadResponse decodeFromJsonString = GsonConverter
							.getInstance().decodeFromJsonString(result,
									ImageUploadResponse.class);
					ImageSendStatusObject imageSendStatusObject = new ImageSendStatusObject();
					imageSendStatusObject.setImageId(decodeFromJsonString
							.getTemp_id());
					imageSendStatusObject
							.setStatus(ActivityConstants.IMAGE_SEND_STATUS);
					JobViewerDBHandler.saveImageStatus(BaseActivity.context,
							imageSendStatusObject);

					break;
				case HttpConnection.DID_ERROR:
					if (!Utils.isNullOrEmpty(imageObject.getImage_string())) {
						JobViewerDBHandler
								.saveImage(BaseActivity.context, imageObject);
					}
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}
	
	public static String getGeoLocationString(Context context){
		String geoLocation = "";
		GPSTracker gpsTracker = new GPSTracker(context);
		Double lat = gpsTracker.getLatitude();
		Double lon = gpsTracker.getLongitude();
		if(lat!=null && lon !=null && lat != 0.0 && lon != 0.0){
			geoLocation = String.valueOf(lat) +";"+String.valueOf(lon);	
		}
		Log.d(Utils.LOG_TAG," getGeoLocationString "+geoLocation);
		return geoLocation;
	}
	
	public static boolean isGPSEnabled(Context context){
		final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );

	    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        return false;
	    }
	    return true;
	}
	
	public static boolean isConfinedStartedFromAddPhoto(Context context){
		String jsonStr = JobViewerDBHandler.getJSONFlagObject(context);
		boolean isStartedFromAddPhotos =  false;
		if(Utils.isNullOrEmpty(jsonStr)){
			jsonStr = "{}";
		}
		try{
			JSONObject flagJSON = new JSONObject(jsonStr);
			if(flagJSON.has(Constants.FLAG_CONFINED_STARTED_FROM_ADD_PHOTOS)){
				isStartedFromAddPhotos = flagJSON.getBoolean(Constants.FLAG_CONFINED_STARTED_FROM_ADD_PHOTOS);
			}
		}catch(Exception e){
			isStartedFromAddPhotos = false;
		}
		return isStartedFromAddPhotos;
	}
	
	public static long OVETTIME_ALERT_TOGGLE = 12*60*60*1000; //12 HOUR
	public static long OVETTIME_ALERT_INTERVAL = 60*60*1000; //1HOUR
}
