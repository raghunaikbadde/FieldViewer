package com.jobviewer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
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
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.BackLogRequest;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.network.SendImagesOnBackground;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.service.AppKillService;
import com.lanesgroup.jobviewer.LauncherActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.WelcomeActivity;
import com.raghu.TimeSheetServiceRequests;
import com.raghu.WorkPhotoUpload;
import com.vehicle.communicator.HttpConnection;

public class Utils {
	public static final String PROGRESS_1_TO_3 = "Step 1 of 3";
	public static final String PROGRESS_2_TO_3 = "Step 2 of 3";
	public static final String PROGRESS_1_TO_2 = "Step 1 of 2";
	public static final String PROGRESS_2_TO_2 = "Step 2 of 2";
	public static final String PROGRESS_1_TO_1 = "Step 1 of 1";
	public static final String CALLING_ACTIVITY = "callingActivity";
	public static final String SHOULD_SHOW_WORK_IN_PROGRESS = "souldShowWorkInProgress";

	public static final String REQUEST_TYPE_WORK = "WORK";
	public static final String REQUEST_TYPE_UPLOAD = "UPLOAD";
	public static final String REQUEST_TYPE_TIMESHEET = "TIMESHEET";

	public static String work_completed_at = "";
	public static String work_engineer_id = "123322";
	public static String work_status = "New";
	public static String work_flooding_status = null;
	public static String work_DA_call_out = "No Call Made";
	public static boolean work_is_redline_captured = false;

	public static String work_id = "22345";
	public static String UPDATE_RISK_ASSESSMENT_ACTIVITY = "UPDATE_RISK_ASSESSMENT_ACTIVITY";
	
	static Dialog progressDialog;
	public static CheckOutObject checkOutObject;
	public static TimeSheetRequest timeSheetRequest = null;
	public static TimeSheetRequest endTimeRequest = null;
	public static TimeSheetRequest startTravelTimeRequest = null;
	public static TimeSheetRequest endTravelTimeRequest = null;
	public static TimeSheetRequest startShiftTimeRequest = null;
	public static TimeSheetRequest endShiftRequest = null;
	
	public static TimeSheetRequest workEndTimeSheetRequest = null;
	public static String lastest_work_started_at = null;
	public static String[] mActivityList = { "Blockage", "CCTV", "Line Clean",
			"Pumo Down", "SFOC", "Clean Up", "SROPR", "Enable" };
	public static TimeSheetRequest callStartTimeRequest = null;
	public static TimeSheetRequest callEndTimeRequest = null;
	static int notificationId = 1000;
	private static NotificationManager myNotificationManager;

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
		workPhotoUpload.setImage(imageObject.getImageId());

		BackLogRequest backLogRequest = new BackLogRequest();
		backLogRequest.setRequestApi(CommsConstant.HOST
				+ CommsConstant.WORK_PHOTO_UPLOAD + "/" + Utils.work_id);
		backLogRequest.setRequestClassName("WorkPhotoUpload");
		backLogRequest.setRequestJson(workPhotoUpload.toString());
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
					dialog.dismiss();
					// ((ICustomSpinnerItemClick)activity).onQuestionsSpinnerItemClick(position);
				}
			});
		}
}
