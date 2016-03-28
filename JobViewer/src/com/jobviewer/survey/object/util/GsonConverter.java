package com.jobviewer.survey.object.util;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonConverter {
	protected Gson gson;
	private static GsonConverter gsonConverter = null;

	public GsonConverter() {
		gson = new GsonBuilder().create();
	}

	public <T> T decodeFromJsonString(String jsonString, Class<T> targetClass) {
		return gson.fromJson(jsonString, targetClass);
	}

	public <T> T decodeFromJsonString(String jsonString, Type targetType) {
		return gson.fromJson(jsonString, targetType);
	}

	public String encodeToJsonString(Object src) {
		return gson.toJson(src);
	}

	public static GsonConverter getInstance() {
		if (gsonConverter == null) {
			gsonConverter = new GsonConverter();
		}
		return gsonConverter;
	}

}
