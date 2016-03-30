package com.raghu;

public class ShoutOutBackLogRequest {
	private String work_id;
	private String survey_type;
	private String related_type;
	private String related_type_reference;
	private String started_at;
	private String completed_at;
	private String survey_json;
	private String created_by;
	private String status;
	private String location_latitude;
	private String location_longitude;
	
	public String getWork_id() {
		return work_id;
	}

	public void setWork_id(String work_id) {
		this.work_id = work_id;
	}

	public String getSurvey_type() {
		return survey_type;
	}

	public void setSurvey_type(String survey_type) {
		this.survey_type = survey_type;
	}

	public String getRelated_type() {
		return related_type;
	}

	public void setRelated_type(String related_type) {
		this.related_type = related_type;
	}

	public String getRelated_type_reference() {
		return related_type_reference;
	}

	public void setRelated_type_reference(String related_type_reference) {
		this.related_type_reference = related_type_reference;
	}

	public String getStarted_at() {
		return started_at;
	}

	public void setStarted_at(String started_at) {
		this.started_at = started_at;
	}

	public String getCompleted_at() {
		return completed_at;
	}

	public void setCompleted_at(String completed_at) {
		this.completed_at = completed_at;
	}

	public String getSurvey_json() {
		return survey_json;
	}

	public void setSurvey_json(String survey_json) {
		this.survey_json = survey_json;
	}

	public String getCreated_by() {
		return created_by;
	}

	public void setCreated_by(String created_by) {
		this.created_by = created_by;
	}

	public String getLocation_latitude() {
		return location_latitude;
	}

	public void setLocation_latitude(String location_latitude) {
		this.location_latitude = location_latitude;
	}

	public String getLocation_longitude() {
		return location_longitude;
	}

	public void setLocation_longitude(String location_longitude) {
		this.location_longitude = location_longitude;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
