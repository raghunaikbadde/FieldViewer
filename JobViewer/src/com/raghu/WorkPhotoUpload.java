package com.raghu;

import org.json.JSONException;
import org.json.JSONObject;

public class WorkPhotoUpload {
	private String image = "";
	private String image_exit = "";

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getImage_exit() {
		return image_exit;
	}

	public void setImage_exit(String image_exit) {
		this.image_exit = image_exit;
	}

	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("image", getImage());
			jsonObject.put("image_exit", getImage_exit());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jsonObject.toString();
	}
	/*
	 * "image": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAA...",
	 * "image_exif": "2016-02-01 10:10:05;12.21154545;45.3424324"
	 */
}
