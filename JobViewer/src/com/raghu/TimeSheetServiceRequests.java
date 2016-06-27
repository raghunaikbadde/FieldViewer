package com.raghu;

import org.json.JSONException;
import org.json.JSONObject;

public class TimeSheetServiceRequests {

    private String started_at = "";
    private String record_for = "";
    private String is_inactive = "";
    private String is_overriden = "";
    private String override_reason = "";
    private String override_comment = "";
    private String override_timestamp = "";
    private String reference_id = "";
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

    public String getIs_inactive() {
        return is_inactive;
    }

    public void setIs_inactive(String is_inactive) {
        this.is_inactive = is_inactive;
    }

    public String getIs_overriden() {
        return is_overriden;
    }

    public void setIs_overriden(String is_overriden) {
        this.is_overriden = is_overriden;
    }

    public String getOverride_reason() {
        return override_reason;
    }

    public void setOverride_reason(String override_reason) {
        this.override_reason = override_reason;
    }

    public String getOverride_comment() {
        return override_comment;
    }

    public void setOverride_comment(String override_comment) {
        this.override_comment = override_comment;
    }

    public String getOverride_timestamp() {
        return override_timestamp;
    }

    public void setOverride_timestamp(String override_timestamp) {
        this.override_timestamp = override_timestamp;
    }

    public String getReference_id() {
        return reference_id;
    }

    public void setReference_id(String reference_id) {
        this.reference_id = reference_id;
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
            jsonObject.put("started_at", getStarted_at());
            jsonObject.put("record_for", getRecord_for());
            jsonObject.put("is_inactive", getIs_inactive());
            jsonObject.put("is_overriden", getIs_overriden());
            jsonObject.put("override_reason", getOverride_reason());
            jsonObject.put("override_comment", getOverride_comment());
            jsonObject.put("override_timestamp", getOverride_timestamp());
            jsonObject.put("reference_id", getReference_id());
            jsonObject.put("user_id", getUser_id());
        } catch (JSONException jse) {
            jse.printStackTrace();
        }
        return jsonObject.toString();
    }
}
