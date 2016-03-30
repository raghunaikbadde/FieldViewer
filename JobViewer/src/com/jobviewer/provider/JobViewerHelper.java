package com.jobviewer.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class JobViewerHelper extends SQLiteOpenHelper {

	public JobViewerHelper(Context context) {
		super(context, JobViewerSchema.DATABASE_NAME, null,
				JobViewerSchema.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(JobViewerSchema.CREATE_TABLE_USER);
		db.execSQL(JobViewerSchema.CREATE_TABLE_TIMESHEET);
		db.execSQL(JobViewerSchema.CREATE_TABLE_IMAGES);
		db.execSQL(JobViewerSchema.CREATE_TABLE_CHECK_OUT_REMEMBER);
		db.execSQL(JobViewerSchema.CREATE_TABLE_QUESTION_SET);
		db.execSQL(JobViewerSchema.CREATE_TABLE_BACK_LOG);
		db.execSQL(JobViewerSchema.CREATE_TABLE_ADD_PHOTOS_SCREEN_SAVED_IMAGES);
		db.execSQL(JobViewerSchema.CREATE_TABLE_SHOUT_ABOUT_SAFETY);
		db.execSQL(JobViewerSchema.CREATE_TABLE_START_TRAINING);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
