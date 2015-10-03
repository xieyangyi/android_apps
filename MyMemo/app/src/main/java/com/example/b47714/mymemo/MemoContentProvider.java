package com.example.b47714.mymemo;

import android.app.ActionBar;
import android.content.ContentProvider;
import android.content.ContentResolver;
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

import com.example.b47714.mymemo.MemoConstants.*;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by B47714 on 10/3/2015.
 */
public class MemoContentProvider extends ContentProvider {
    private MemoSQLiteOpenHelper memoSQLiteOpenHelper;
    private static UriMatcher uriMatcher;
    private static HashMap<String, String> projectMap;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MemoProvider.AUTHORITY, MemoDB.TABLE_NAME, MemoDB.URI_TABLE);
        uriMatcher.addURI(MemoProvider.AUTHORITY, MemoDB.TABLE_NAME + "/#", MemoDB.URI_TABLE_ID);

        projectMap = new HashMap<>();
        projectMap.put(TableColumnNames._ID, TableColumnNames._ID);
        projectMap.put(TableColumnNames.CONTENT, TableColumnNames.CONTENT);
        projectMap.put(TableColumnNames.DATE, TableColumnNames.DATE);
        projectMap.put(TableColumnNames.TIME, TableColumnNames.TIME);
        projectMap.put(TableColumnNames.IS_ON, TableColumnNames.IS_ON);
        projectMap.put(TableColumnNames.IS_SOUND_ON, TableColumnNames.IS_SOUND_ON);
    }

    public class MemoSQLiteOpenHelper extends SQLiteOpenHelper {
        public MemoSQLiteOpenHelper(Context context) {
            super(context, MemoDB.NAME, null, MemoDB.VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + MemoDB.TABLE_NAME +" ("
                    + TableColumnNames._ID + " INTEGER PRIMARY KEY,"
                    + TableColumnNames.CONTENT + " TEXT,"
                    + TableColumnNames.DATE + " TEXT,"
                    + TableColumnNames.TIME + " TEXT,"
                    + TableColumnNames.IS_ON + " INTEGER,"
                    + TableColumnNames.IS_SOUND_ON + " INTEGER"
                    + ");"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + MemoDB.TABLE_NAME);
            onCreate(db);
        }
    }
    @Override
    public boolean onCreate() {
        memoSQLiteOpenHelper = new MemoSQLiteOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case MemoDB.URI_TABLE:
                queryBuilder.setTables(MemoDB.TABLE_NAME);
                queryBuilder.setProjectionMap(projectMap);
                break;
            case MemoDB.URI_TABLE_ID:
                queryBuilder.setTables(MemoDB.TABLE_NAME);
                queryBuilder.setProjectionMap(projectMap);
                queryBuilder.appendWhere(TableColumnNames._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("error uri " + uri);
        }

        // use queryBuilder to query, and notification contentResolver
        SQLiteDatabase database = memoSQLiteOpenHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, null);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MemoDB.URI_TABLE:
                return MemoDB.TABLE_TYPE;
            case MemoDB.URI_TABLE_ID:
                return MemoDB.TABLE_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("error uri " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // check uri whether corrent
        if(uriMatcher.match(uri) != MemoDB.URI_TABLE) {
            throw new IllegalArgumentException("error uri " + uri);
        }

        // insert contentValues and return uri with id appended
        SQLiteDatabase db = memoSQLiteOpenHelper.getWritableDatabase();
        long rowId = db.insert(MemoDB.TABLE_NAME, null, values);
        if(rowId > 0) {
            Uri resUri = ContentUris.withAppendedId(MemoProvider.URI, rowId);
            getContext().getContentResolver().notifyChange(resUri, null);
            return resUri;
        }

        throw new IllegalArgumentException("insert error!" + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = memoSQLiteOpenHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case MemoDB.URI_TABLE:
                count = db.delete(MemoDB.TABLE_NAME, selection, selectionArgs);
                break;
            case MemoDB.URI_TABLE_ID:
                String rowId = uri.getPathSegments().get(1);
                String id_selection = TableColumnNames._ID + "=" + rowId
                        + (TextUtils.isEmpty(selection) ? "" : ("AND (" + selection + ")"));
                count = db.delete(MemoDB.TABLE_NAME, id_selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("error uri " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = memoSQLiteOpenHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case MemoDB.URI_TABLE:
                count = db.update(MemoDB.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MemoDB.URI_TABLE_ID:
                String rowId = uri.getPathSegments().get(1);
                String id_selection = TableColumnNames._ID + "=" + rowId
                        + (TextUtils.isEmpty(selection) ? "" : ("AND (" + selection + ")"));
                count = db.update(MemoDB.TABLE_NAME, values, id_selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("error uri " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
