package com.raghu;

import org.json.JSONException;
import org.json.JSONObject;

public class WorkRequest {

	private String started_at = "";
	private String reference_id = "";
	private String engineer_id = "";
	private String status = "";
	private String completed_at = "";
	private String activity_type = "";
	private String flooding_status = "";
	private String DA_call_out = "";
	private boolean is_redline_captured = false;
	private String location_latitude = "";
	private String location_longitude = "";
	private String created_by="";

	public String getStarted_at() {
		return started_at;
	}

	public void setStarted_at(String started_at) {
		this.started_at = started_at;
	}

	public String getReference_id() {
		return reference_id;
	}

	public void setReference_id(String reference_id) {
		this.reference_id = reference_id;
	}

	public String getEngineer_id() {
		return engineer_id;
	}

	public void setEngineer_id(String engineer_id) {
		this.engineer_id = engineer_id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String isCompleted_at() {
		return completed_at;
	}

	public void setCompleted_at(String completed_at) {
		this.completed_at = completed_at;
	}
	
	public String getcompleted_at(){
		return completed_at;
	}
	
	public String getActivity_type() {
		return activity_type;
	}

	public void setActivity_type(String activity_type) {
		this.activity_type = activity_type;
	}

	public String getFlooding_status() {
		return flooding_status;
	}

	public void setFlooding_status(String flooding_status) {
		this.flooding_status = flooding_status;
	}

	public String getDA_call_out() {
		return DA_call_out;
	}

	public void setDA_call_out(String dA_call_out) {
		DA_call_out = dA_call_out;
	}

	public boolean isIs_redline_captured() {
		return is_redline_captured;
	}

	public void setIs_redline_captured(boolean is_redline_captured) {
		this.is_redline_captured = is_redline_captured;
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

	public String getCreated_by() {
		return created_by;
	}

	public void setCreated_by(String created_by) {
		this.created_by = created_by;
	}

}
