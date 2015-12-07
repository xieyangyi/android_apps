package com.fsl.fslclubs.myEvents;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by B47714 on 10/16/2015.
 */
public class MyEventProvider extends ContentProvider {
    public final static String MY_ACTIVITY_DB_NAME = "my_activity.db";
    public final static int MY_ACTIVITY_DB_VERSION = 8;
    public final static String MY_ACTIVITY_TBL_NAME = "my_activity_tbl";
    public final static String TBL_ID = "_id";          // must have a column named as "_id" in simpleCursorAdapter
    public final static String TBL_ACTIVITY_ID = "activity_id";
    public final static String TBL_NAME = "name";
    public final static String TBL_ICON = "icon";
    public final static String TBL_WEBSITE = "website";
    public final static String TBL_ADDRESS = "address";
    public final static String TBL_TIME = "time";
    public final static String TBL_EXPIRE_TIME = "expire_time";
    public final static String TBL_ALARM_DATE = "alarm_date";
    public final static String TBL_ALARM_TIME = "alarm_time";
    public final static String TBL_ALARM_IS_ON = "is_alarm_on";
    public final static int ALL_ACTIVITY = 1;
    public final static int ONE_ACTIVITY = 2;
    public final static String TYPE_ALL_ACTIVITY = "all";
    public final static String TYPE_ONE_ACTIVITY = "one";
    public final static String AUTHOURITY = "com.fsl.fslclubs.myactivity.provider";
    public final static Uri USER_TBL_URI = Uri.parse("content://" + AUTHOURITY + "/" + MY_ACTIVITY_TBL_NAME);

    private MyActivityOpenHelper myActivityOpenHelper;
    private static UriMatcher uriMatcher;
    private static HashMap<String, String> projectionMap;


    private class MyActivityOpenHelper extends  SQLiteOpenHelper {
        public MyActivityOpenHelper(Context context) {
            super(context, MY_ACTIVITY_DB_NAME, null, MY_ACTIVITY_DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "create table " + MY_ACTIVITY_TBL_NAME + "("
                    + TBL_ID + " integer primary key,"
                    + TBL_ACTIVITY_ID + " text,"
                    + TBL_NAME + " text,"
                    + TBL_ICON + " text,"
                    + TBL_WEBSITE + " text,"
                    + TBL_ADDRESS + " text,"
                    + TBL_TIME + " text,"
                    + TBL_EXPIRE_TIME + " text,"
                    + TBL_ALARM_DATE + " text,"
                    + TBL_ALARM_TIME + " text,"
                    + TBL_ALARM_IS_ON + " integer"
                    + ");";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String sql = "drop table " + MY_ACTIVITY_TBL_NAME + ";";
            db.execSQL(sql);
            onCreate(db);
        }
    }
    @Override
    public boolean onCreate() {
        myActivityOpenHelper = new MyActivityOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case ALL_ACTIVITY:
                queryBuilder.setTables(MY_ACTIVITY_TBL_NAME);
                queryBuilder.setProjectionMap(projectionMap);
                break;
            case ONE_ACTIVITY:
                queryBuilder.setTables(MY_ACTIVITY_TBL_NAME);
                queryBuilder.setProjectionMap(projectionMap);
                queryBuilder.appendWhere(TBL_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("uri error" + uri);
        }

        SQLiteDatabase db = myActivityOpenHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALL_ACTIVITY:
                return TYPE_ALL_ACTIVITY;
            case ONE_ACTIVITY:
                return TYPE_ONE_ACTIVITY;
            default:
                throw new IllegalArgumentException("uri error: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != ALL_ACTIVITY) {
            throw new IllegalArgumentException("uri error: " + uri);
        }

        SQLiteDatabase db = myActivityOpenHelper.getWritableDatabase();
        long rowId = db.insert(MY_ACTIVITY_TBL_NAME, null, values);
        if (rowId > 0) {
            Uri taskUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(taskUri, null);
            return taskUri;
        }
        throw new IllegalArgumentException("insert row error, uri: " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = myActivityOpenHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case ALL_ACTIVITY:
                count = db.delete(MY_ACTIVITY_TBL_NAME, selection, selectionArgs);
                break;
            case ONE_ACTIVITY:
                String noteId = uri.getPathSegments().get(1);
                selection = (TBL_ID + "=" + noteId)
                        + (!TextUtils.isEmpty(selection) ? (" and (" + selection + ")") : "");
                count = db.delete(MY_ACTIVITY_TBL_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("uri error: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = myActivityOpenHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case ALL_ACTIVITY:
                count = db.update(MY_ACTIVITY_TBL_NAME, values, selection, selectionArgs);
                break;
            case ONE_ACTIVITY:
                String noteId = uri.getPathSegments().get(1);
                selection = (TBL_ID + "=" + noteId)
                        + (!TextUtils.isEmpty(selection) ? (" and (" + selection + ")") : "");
                count = db.update(MY_ACTIVITY_TBL_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("uri error: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHOURITY, MY_ACTIVITY_TBL_NAME, ALL_ACTIVITY);
        uriMatcher.addURI(AUTHOURITY, MY_ACTIVITY_TBL_NAME + "/#", ONE_ACTIVITY);

        projectionMap = new HashMap<>();
        projectionMap.put(TBL_ID, TBL_ID);
        projectionMap.put(TBL_ACTIVITY_ID, TBL_ACTIVITY_ID);
        projectionMap.put(TBL_NAME, TBL_NAME);
        projectionMap.put(TBL_ICON, TBL_ICON);
        projectionMap.put(TBL_WEBSITE, TBL_WEBSITE);
        projectionMap.put(TBL_ADDRESS, TBL_ADDRESS);
        projectionMap.put(TBL_TIME, TBL_TIME);
        projectionMap.put(TBL_EXPIRE_TIME, TBL_EXPIRE_TIME);
        projectionMap.put(TBL_ALARM_DATE, TBL_ALARM_DATE);
        projectionMap.put(TBL_ALARM_TIME, TBL_ALARM_TIME);
        projectionMap.put(TBL_ALARM_IS_ON, TBL_ALARM_IS_ON);
    }
}
