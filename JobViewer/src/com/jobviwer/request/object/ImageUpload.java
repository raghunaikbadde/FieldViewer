package com.jobviwer.request.object;

public class ImageUpload {

	private String temp_id = "";
	private String category = "works";
	private String image_string = "";
	private String image_exif = "";
	
	public String getTemp_id() {
		return temp_id;
	}
	public void setTemp_id(String temp_id) {
		this.temp_id = temp_id;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getImage_string() {
		return image_string;
	}
	public void setImage_string(String image_string) {
		this.image_string = image_string;
	}
	public String getImage_exif() {
		return image_exif;
	}
	public void setImage_exif(String image_exif) {
		this.image_exif = image_exif;
	}

}
