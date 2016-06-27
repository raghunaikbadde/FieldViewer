package com.raghu;

import org.json.JSONException;
import org.json.JSONObject;

public class StartUpRequest {
    private String imei = "";
    private String email = "";

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("imei", getImei());
            jsonObject.put("email", getEmail());
        } catch (JSONException jse) {
            jse.printStackTrace();
        }
        return jsonObject.toString();
    }
}
