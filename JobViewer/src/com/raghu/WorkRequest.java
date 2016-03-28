package com.raghu;

import org.json.JSONException;
import org.json.JSONObject;

public class WorkRequest {

	private String started_at = "";
	private String reference_id = "";
	private String engineer_id = "";
	private String status = "";
	private boolean completed_at = false;
	private String activity_type = "";
	private String flooding_status = "";
	private String DA_call_out = "";
	private boolean is_redline_captured = false;
	private String location_latitude = "";
	private String location_longitude = "";

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

	public boolean isCompleted_at() {
		return completed_at;
	}

	public void setCompleted_at(boolean completed_at) {
		this.completed_at = completed_at;
	}
	
	public boolean getcompleted_at(){
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

	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject();
		try{
			jsonObject.put("started_at",getStarted_at());
			jsonObject.put("reference_id",getReference_id());
			jsonObject.put("engineer_id",getEngineer_id());
			jsonObject.put("status",getStatus());
			jsonObject.put("completed_at",isCompleted_at());
			jsonObject.put("activity_type",getActivity_type());
			jsonObject.put("flooding_status",getFlooding_status());
			jsonObject.put("DA_call_out",getDA_call_out());
			jsonObject.put("is_redline_captured",isIs_redline_captured());
			jsonObject.put("location_latitude",getLocation_latitude());
			jsonObject.put("location_longitude",getLocation_longitude());
		}catch(JSONException jse){}
		return jsonObject.toString();
	}
	/*
	 * "started_at": "01:02:20 01 Jan 2016", "reference_id": "1212ABCD",
	 * "engineer_id": "123322", "status": "New", "completed_at": null,
	 * "activity_type": "", "flooding_status": "No Flooding", "DA_call_out":
	 * "No Call Made", "is_redline_captured": true, "location_latitude":
	 * "12.42323", "location_longitude": "32.234321"
	 */

}
