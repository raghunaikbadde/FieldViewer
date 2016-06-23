package com.jobviewer.network;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.db.objects.ImageSendStatusObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.ImageUploadResponse;
import com.vehicle.communicator.HttpConnection;

public class SendImagesOnBackground {
	private static int index = 0;
	private static List<ImageObject> allSavedImages = new ArrayList<ImageObject>();
	private Context context;

	public void getAndSendImagesToServer(Context context) {
		try {
			allSavedImages = JobViewerDBHandler.getAllSavedImages(context);
		} catch (OutOfMemoryError oome) {

		}
		this.context = context;
		Log.i("Android", "" + allSavedImages.size());
		if (allSavedImages.size() != 0) {
			sendImage(allSavedImages.get(0));
		}

	}

	private void sendImage(ImageObject imageObject) {
		ImageSendStatusObject imageStatusById = JobViewerDBHandler
				.getImageStatusById(context, imageObject.getImageId());
		if (imageStatusById != null
				&& !ActivityConstants.IMAGE_SEND_STATUS
						.equalsIgnoreCase(imageStatusById.getStatus())) {

			ContentValues values = new ContentValues();
			values.put("temp_id", imageObject.getImageId());
			Log.i("Android", imageObject.getImageId());
			values.put("category", imageObject.getCategory());
			Log.i("Android", imageObject.getCategory());
			String str = "";
			try {
				str = imageObject.getImage_string();
			} catch (OutOfMemoryError oome) {

			}
			values.put("image_string", str);
			Log.i("Android", "Image 20 :" + str);
			values.put("image_exif", imageObject.getImage_exif());
			Utils.SendHTTPRequest(context, CommsConstant.HOST
					+ CommsConstant.SURVEY_PHOTO_UPLOAD, values,
					getSaveImageHandler());
		}
	}

	private Handler getSaveImageHandler() {
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
					 * JobViewerDBHandler.deleteImageById(context,
					 * decodeFromJsonString.getTemp_id());
					 */

					ImageSendStatusObject imageSendStatusObject = new ImageSendStatusObject();
					imageSendStatusObject.setImageId(decodeFromJsonString
							.getTemp_id());
					imageSendStatusObject
							.setStatus(ActivityConstants.IMAGE_SEND_STATUS);
					JobViewerDBHandler.saveImageStatus(context,
							imageSendStatusObject);

					if (index < allSavedImages.size() - 1) {
						index++;
						sendImage(allSavedImages.get(index));
					} else {
						try {
							allSavedImages = JobViewerDBHandler
									.getAllSavedImages(context);
						} catch (OutOfMemoryError oome) {

						}
						index = 0;
						if (allSavedImages.size() > 0) {
							sendImage(allSavedImages.get(0));
						}
					}
					Log.i("Android", "" + index);
					break;
				case HttpConnection.DID_ERROR:
					context.stopService(new Intent(context,
							SendImageService.class));
					Log.i("", "");
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

}
