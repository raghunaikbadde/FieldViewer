package com.jobviewer.network;

import java.util.ArrayList;
import java.util.List;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.ImageObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.Utils;
import com.jobviwer.response.object.ImageUploadResponse;
import com.vehicle.communicator.HttpConnection;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SendImagesOnBackground {
	static int index = 0;
	static List<ImageObject> allSavedImages = new ArrayList<ImageObject>();
	Context context;

	public void getAndSendImagesToServer(Context context) {
		allSavedImages = JobViewerDBHandler.getAllSavedImages(context);
		this.context = context;
		if (allSavedImages.size() != 0) {
			sendImage(allSavedImages.get(0));
		}

	}

	private void sendImage(ImageObject imageObject) {
		ContentValues values = new ContentValues();
		values.put("temp_id", imageObject.getImageId());
		values.put("category", imageObject.getCategory());
		values.put("image_string", imageObject.getImage_string());
		values.put("image_exif", imageObject.getImage_exif());
		Utils.SendHTTPRequest(context, CommsConstant.HOST
				+ CommsConstant.IMAGE_UPLOAD, values, getSaveImageHandler());
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
					JobViewerDBHandler.deleteImageById(context,
							decodeFromJsonString.getTemp_id());
					if (index <= allSavedImages.size()) {
						index++;
						sendImage(allSavedImages.get(index));
					} else {
						allSavedImages = JobViewerDBHandler
								.getAllSavedImages(context);
						index = 0;
						if (allSavedImages.size() > 0) {
							sendImage(allSavedImages.get(0));
						}
					}
					break;
				case HttpConnection.DID_ERROR:
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
