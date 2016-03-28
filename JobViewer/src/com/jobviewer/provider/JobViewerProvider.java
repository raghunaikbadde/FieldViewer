package com.jobviewer.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.jobviewer.provider.JobViewerProviderContract.Image;
import com.jobviewer.provider.JobViewerProviderContract.QuestionSetTable;
import com.jobviewer.provider.JobViewerProviderContract.TimeSheet;
import com.jobviewer.provider.JobViewerProviderContract.User;

public class JobViewerProvider extends ContentProvider {

	private JobViewerHelper mDbHelper;

	// helper constants for use with the UriMatcher
	private static final int TABLE_USER = 1;
	private static final int TABLE_USER_ID = 2;
	private static final int TABLE_TIMESHEET = 3;
	private static final int TABLE_IMAGES = 4;
	private static final int TABLE_CHECK_OUT_REMEMBER = 5;
	private static final int TABLE_QUESTION_SET = 6;

	// prepare the UriMatcher
	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(JobViewerProviderContract.AUTHORITY, "User",
				TABLE_USER);
		URI_MATCHER.addURI(JobViewerProviderContract.AUTHORITY, "User/#",
				TABLE_USER_ID);
		URI_MATCHER.addURI(JobViewerProviderContract.AUTHORITY, "Timesheet",
				TABLE_TIMESHEET);
		URI_MATCHER.addURI(JobViewerProviderContract.AUTHORITY, "Images",
				TABLE_IMAGES);
		URI_MATCHER.addURI(JobViewerProviderContract.AUTHORITY,
				"CheckOutRemember", TABLE_CHECK_OUT_REMEMBER);
		URI_MATCHER.addURI(JobViewerProviderContract.AUTHORITY,
				"QuestionSetTable", TABLE_QUESTION_SET);
	}

	// system calls onCreate() when it starts up the provider.
	@Override
	public boolean onCreate() {
		// get access to the database helper
		mDbHelper = new JobViewerHelper(getContext());
		return false;
	}

	// Return the MIME type corresponding to a content URI
	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case TABLE_USER:
			return User.CONTENT_TYPE;
		case TABLE_USER_ID:
			return User.CONTENT_USER_TYPE;
		case TABLE_TIMESHEET:
			return TimeSheet.CONTENT_TYPE;
		case TABLE_IMAGES:
			return Image.CONTENT_TYPE;
		case TABLE_CHECK_OUT_REMEMBER:
			return Image.CONTENT_TYPE;
		case TABLE_QUESTION_SET:
			return QuestionSetTable.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	// The insert() method adds a new row to the appropriate table, using the
	// values
	// in the ContentValues argument. If a column name is not in the
	// ContentValues argument,
	// you may want to provide a default value for it either in your provider
	// code or in
	// your database schema.
	@Override
	public Uri insert(Uri uri, ContentValues values) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		long id;
		switch (URI_MATCHER.match(uri)) {
		case TABLE_USER:
			id = db.insert(JobViewerSchema.TABLE_USER, null, values);
			return getUriForId(id, uri);
		case TABLE_TIMESHEET:
			id = db.insert(JobViewerSchema.TABLE_TIMESHEET, null, values);
			return getUriForId(id, uri);
		case TABLE_IMAGES:
			id = db.insert(JobViewerSchema.TABLE_IMAGE, null, values);
			return getUriForId(id, uri);
		case TABLE_CHECK_OUT_REMEMBER:
			id = db.insert(JobViewerSchema.TABLE_CHECK_OUT_REMEMBER, null,
					values);
			return getUriForId(id, uri);
		case TABLE_QUESTION_SET:
			id = db.insert(JobViewerSchema.TABLE_QUESTION_SET, null, values);
			return getUriForId(id, uri);
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	private Uri getUriForId(long id, Uri uri) {
		if (id > 0) {
			Uri itemUri = ContentUris.withAppendedId(uri, id);
			// notify all listeners of changes:
			getContext().getContentResolver().notifyChange(uri, null);
			return itemUri;
		}
		// s.th. went wrong:
		throw new SQLException("Problem while inserting into uri: " + uri);
	}

	// The query() method must return a Cursor object, or if it fails,
	// throw an Exception. If you are using an SQLite database as your data
	// storage,
	// you can simply return the Cursor returned by one of the query() methods
	// of the
	// SQLiteDatabase class. If the query does not match any rows, you should
	// return a
	// Cursor instance whose getCount() method returns 0. You should return null
	// only
	// if an internal error occurred during the query process.
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		switch (URI_MATCHER.match(uri)) {
		case TABLE_USER:
			queryBuilder.setTables(JobViewerSchema.TABLE_USER);
			break;
		case TABLE_USER_ID:
			queryBuilder.setTables(JobViewerSchema.TABLE_USER);
			queryBuilder.appendWhere(User._ID + "="
					+ uri.getPathSegments().get(1));
			break;
		case TABLE_TIMESHEET:
			queryBuilder.setTables(JobViewerSchema.TABLE_TIMESHEET);
			break;
		case TABLE_IMAGES:
			queryBuilder.setTables(JobViewerSchema.TABLE_IMAGE);
			break;
		case TABLE_CHECK_OUT_REMEMBER:
			queryBuilder.setTables(JobViewerSchema.TABLE_CHECK_OUT_REMEMBER);
			break;
		case TABLE_QUESTION_SET:
			queryBuilder.setTables(JobViewerSchema.TABLE_QUESTION_SET);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;

	}

	// The delete() method deletes rows based on the seletion or if an id is
	// provided then it deleted a single row. The methods returns the numbers
	// of records delete from the database. If you choose not to delete the data
	// physically then just update a flag here.
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int deleteCount;

		switch (URI_MATCHER.match(uri)) {
		case TABLE_USER:
			deleteCount = db.delete(JobViewerSchema.TABLE_USER, selection,
					selectionArgs);
			break;
		case TABLE_USER_ID:
			deleteCount = db.delete(JobViewerSchema.TABLE_USER, selection,
					selectionArgs);
			break;
		case TABLE_TIMESHEET:
			deleteCount = db.delete(JobViewerSchema.TABLE_TIMESHEET, selection,
					selectionArgs);
			break;
		case TABLE_IMAGES:
			deleteCount = db.delete(JobViewerSchema.TABLE_IMAGE, selection,
					selectionArgs);
			break;
		case TABLE_CHECK_OUT_REMEMBER:
			deleteCount = db.delete(JobViewerSchema.TABLE_CHECK_OUT_REMEMBER,
					selection, selectionArgs);
			break;
		case TABLE_QUESTION_SET:
			deleteCount = db.delete(JobViewerSchema.TABLE_QUESTION_SET,
					selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return deleteCount;
	}

	// The update method() is same as delete() which updates multiple rows
	// based on the selection or a single row if the row id is provided. The
	// update method returns the number of updated rows.
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int updateCount;
		String id;
		switch (URI_MATCHER.match(uri)) {
		case TABLE_USER:
			updateCount = db.update(JobViewerSchema.TABLE_USER, values,
					selection, selectionArgs);
			break;
		case TABLE_USER_ID:
			id = uri.getPathSegments().get(1);
			selection = User._ID
					+ "="
					+ id
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : "");
			updateCount = db.update(JobViewerSchema.TABLE_USER, values,
					selection, selectionArgs);
			break;
		case TABLE_TIMESHEET:
			updateCount = db.update(JobViewerSchema.TABLE_TIMESHEET, values,
					selection, selectionArgs);
			break;
		case TABLE_IMAGES:
			updateCount = db.update(JobViewerSchema.TABLE_IMAGE, values,
					selection, selectionArgs);
			break;
		case TABLE_CHECK_OUT_REMEMBER:
			updateCount = db.update(JobViewerSchema.TABLE_CHECK_OUT_REMEMBER,
					values, selection, selectionArgs);
			break;
		case TABLE_QUESTION_SET:
			updateCount = db.update(JobViewerSchema.TABLE_QUESTION_SET, values,
					selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return updateCount;
	}
}