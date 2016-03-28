package com.jobviewer.db.objects;

public class ImageObject {
	private String imageId;
	private String image_string;
	private String image_url;

	public String getImage_string() {
		return image_string;
	}

	public void setImage_string(String image_string) {
		this.image_string = image_string;
	}

	public String getImage_url() {
		return image_url;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

}
