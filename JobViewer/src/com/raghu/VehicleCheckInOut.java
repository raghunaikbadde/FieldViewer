package com.raghu;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.GsonBuilder;

public class VehicleCheckInOut {

	private String started_at = "";
	private String record_for = "";
	private String registration = "";
	private String mileage = "";
	private String user_id = "";

	public String getStarted_at() {
		return started_at;
	}

	public void setStarted_at(String started_at) {
		this.started_at = started_at;
	}

	public String getRecord_for() {
		return record_for;
	}

	public void setRecord_for(String record_for) {
		this.record_for = record_for;
	}

	public String getRegistration() {
		return registration;
	}

	public void setRegistration(String registration) {
		this.registration = registration;
	}

	public String getMileage() {
		return mileage;
	}

	public void setMileage(String mileage) {
		this.mileage = mileage;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("started_at", started_at);
			jsonObject.put("record_for", started_at);
			jsonObject.put("registration", started_at);
			jsonObject.put("mileage", started_at);
			jsonObject.put("user_id", started_at);
		} catch (JSONException jse) {

		}
		return jsonObject.toString();
	}
}
