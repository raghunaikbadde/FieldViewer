package com.jobviewer.db.objects;

public class ImageObject {
	private String imageId;
	private String image_string;
	private String image_url;
	private String category;
	private String image_exif;

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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getImage_exif() {
		return image_exif;
	}

	public void setImage_exif(String image_exif) {
		this.image_exif = image_exif;
	}

}
