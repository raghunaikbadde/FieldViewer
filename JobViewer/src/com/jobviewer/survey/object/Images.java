package com.jobviewer.survey.object;

public class Images {
	private String image_exif;

	private String image_string;

	public String getImage_exif() {
		return image_exif;
	}

	public void setImage_exif(String image_exif) {
		this.image_exif = image_exif;
	}

	public String getImage_string() {
		return image_string;
	}

	public void setImage_string(String image_string) {
		this.image_string = image_string;
	}

	@Override
	public String toString() {
		return "ClassPojo [image_exif = " + image_exif + ", image_string = "
				+ image_string + "]";
	}
}
