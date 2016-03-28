package com.jobviewer.exception;

import android.content.Context;

public class ExceptionHandler {

	public static void showException(final Context context,
			VehicleException ex, String title) {
		CustomErrorDialog dialog = new CustomErrorDialog(context, title,
				ex.getMessage(), "OK", null);
		dialog.show();
	}

}
