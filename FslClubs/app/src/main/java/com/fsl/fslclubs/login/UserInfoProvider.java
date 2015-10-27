package com.fsl.fslclubs.login;

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

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by B47714 on 10/16/2015.
 */
public class UserInfoProvider extends ContentProvider {
    public final static String USER_INFO_DB_NAME = "user_info.db";
    public final static int USER_INFO_DB_VERSION = 1;
    public final static String USER_TBL_NAME = "user_tbl";
    public final static String TBL_ID = "id";
    public final static String TBL_PHONE = "phone";
    public final static String TBL_PASSWORD = "password";
    public final static String TBL_NAME = "name";
    public final static String TBL_CLUB = "club";
    public final static String TBL_SEX = "sex";
    public final static String TBL_EMAIL = "email";
    public final static String TBL_COREID = "core_id";
    public final static String TBL_SIGNATURE = "signature";
    public final static String TBL_LEGALID = "legal_id";
    public final static int ALL_USER = 1;
    public final static int ONE_USER = 2;
    public final static String TYPE_ALL_USER = "all";
    public final static String TYPE_ONE_USER = "one";
    public final static String AUTHOURITY = "com.fsl.fslclubs.provider";
    public final static Uri USER_TBL_URI = Uri.parse("content://" + AUTHOURITY + "/" + USER_TBL_NAME);

    private UserInfoOpenHelper userInfoOpenHelper;
    private static UriMatcher uriMatcher;
    private static HashMap<String, String> projectionMap;


    private class UserInfoOpenHelper extends  SQLiteOpenHelper {
        public UserInfoOpenHelper(Context context) {
            super(context, USER_INFO_DB_NAME, null, USER_INFO_DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "create table " + USER_TBL_NAME + "("
                    + TBL_ID + " integer primary key,"
                    + TBL_PHONE + " text,"
                    + TBL_PASSWORD + " text,"
                    + TBL_NAME + " text,"
                    + TBL_CLUB + " text,"
                    + TBL_SEX + " text,"
                    + TBL_EMAIL + " text,"
                    + TBL_COREID + " text,"
                    + TBL_SIGNATURE + " text,"
                    + TBL_LEGALID + " text"
                    + ");";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String sql = "drop table " + TBL_NAME + ";";
            db.execSQL(sql);
            onCreate(db);
        }
    }
    @Override
    public boolean onCreate() {
        userInfoOpenHelper = new UserInfoOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case ALL_USER:
                queryBuilder.setTables(USER_TBL_NAME);
                queryBuilder.setProjectionMap(projectionMap);
                break;
            case ONE_USER:
                queryBuilder.setTables(USER_TBL_NAME);
                queryBuilder.setProjectionMap(projectionMap);
                queryBuilder.appendWhere(TBL_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("uri error" + uri);
        }

        SQLiteDatabase db = userInfoOpenHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), null);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALL_USER:
                return TYPE_ALL_USER;
            case ONE_USER:
                return TYPE_ONE_USER;
            default:
                throw new IllegalArgumentException("uri error: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != ALL_USER) {
            throw new IllegalArgumentException("uri error: " + uri);
        }

        SQLiteDatabase db = userInfoOpenHelper.getWritableDatabase();
        long rowId = db.insert(USER_TBL_NAME, TBL_PHONE, values);
        if (rowId > 0) {
            Uri taskUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(taskUri, null);
            return taskUri;
        }
        throw new IllegalArgumentException("insert row error, uri: " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = userInfoOpenHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case ALL_USER:
                count = db.delete(USER_TBL_NAME, selection, selectionArgs);
                break;
            case ONE_USER:
                String noteId = uri.getPathSegments().get(1);
                selection = (TBL_ID + "=" + noteId)
                        + (!TextUtils.isEmpty(selection) ? (" and (" + selection + ")") : "");
                count = db.delete(USER_TBL_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("uri error: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = userInfoOpenHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case ALL_USER:
                count = db.update(USER_TBL_NAME, values, selection, selectionArgs);
                break;
            case ONE_USER:
                String noteId = uri.getPathSegments().get(1);
                selection = (TBL_ID + "=" + noteId)
                        + (!TextUtils.isEmpty(selection) ? (" and (" + selection + ")") : "");
                count = db.update(USER_TBL_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("uri error: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHOURITY, USER_TBL_NAME, ALL_USER);
        uriMatcher.addURI(AUTHOURITY, USER_TBL_NAME + "/#", ONE_USER);

        projectionMap = new HashMap<>();
        projectionMap.put(TBL_ID, TBL_ID);
        projectionMap.put(TBL_PHONE, TBL_PHONE);
        projectionMap.put(TBL_PASSWORD, TBL_PASSWORD);
        projectionMap.put(TBL_NAME, TBL_NAME);
        projectionMap.put(TBL_CLUB, TBL_CLUB);
        projectionMap.put(TBL_SEX, TBL_SEX);
        projectionMap.put(TBL_EMAIL, TBL_EMAIL);
        projectionMap.put(TBL_COREID, TBL_COREID);
        projectionMap.put(TBL_SIGNATURE, TBL_SIGNATURE);
        projectionMap.put(TBL_LEGALID, TBL_LEGALID);
    }
}
