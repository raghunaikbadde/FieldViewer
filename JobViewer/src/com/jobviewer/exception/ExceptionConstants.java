package com.jobviewer.exception;

public class ExceptionConstants {
	public static class startUpError {
		public static int startUpErrorCode403 = 403;
		public static int startUpErrorCode404 = 404;
		public static int startUpErrorCode500 = 500;
	}

	public static class startUpErrorMsg {
		public static String startUpErrorCode403 = "Email does not have permission to access the app.";
		public static String startUpErrorCode400 = "Bad request. Requied email missing.";
		public static String startUpErrorCode500 = "Internal Error";
	}

}
