package com.example.b47714.mymemo;

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
 * Created by B47714 on 9/29/2015.
 */
public class TaskListProvider extends ContentProvider {
    private static final String DATABASE_NAME = "task_list.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TASK_LIST_TABLE_NAME = "taskLists";
    private static HashMap<String, String> sTaskListProjectionMap;
    private static final int TASKS = 1;
    private static final int TASK_ID = 2;
    private static final UriMatcher sUriMatcher;
    private DatabaseHelper mopenHelper;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TASK_LIST_TABLE_NAME + " ("
                + TaskList.Tasks._ID + " INTEGER PRIMARY KEY,"
                + TaskList.Tasks.DATE1 + " TEXT,"
                + TaskList.Tasks.TIME1 + " TEXT,"
                + TaskList.Tasks.CONTENT + " TEXT,"
                + TaskList.Tasks.IS_ON + " INTEGER,"
                + TaskList.Tasks.IS_SOUND_ON + " INTEGER,"
                + TaskList.Tasks.CREATED + " TEXT"
                + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS taskLists");
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        mopenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case TASKS:
                qb.setTables(TASK_LIST_TABLE_NAME);
                qb.setProjectionMap(sTaskListProjectionMap);
                break;
            case TASK_ID:
                qb.setTables(TASK_LIST_TABLE_NAME);
                qb.setProjectionMap(sTaskListProjectionMap);
                qb.appendWhere(TaskList.Tasks._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Uri error! " + uri);
        }

        String orderBy;
        if(TextUtils.isEmpty(sortOrder)) {
            orderBy = TaskList.Tasks.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mopenHelper.getReadableDatabase();
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case TASKS:         // all the table
                return TaskList.Tasks.CONTENT;
            case TASK_ID:      // one item in the table
                return TaskList.Tasks.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Uri error!" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if(sUriMatcher.match(uri) != TASKS) {
            throw new IllegalArgumentException("Uri error!" + uri);
        }
        ContentValues val;
        if(values != null)
            val = new ContentValues(values);
        else
            val = new ContentValues();

        SQLiteDatabase db = mopenHelper.getWritableDatabase();
        long rowId = db.insert(TASK_LIST_TABLE_NAME, TaskList.Tasks.CONTENT, val);
        if(rowId > 0) {
            Uri taskUri = ContentUris.withAppendedId(TaskList.Tasks.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(taskUri, null);
            return taskUri;
        }
        throw new IllegalArgumentException("insert data error " + uri);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mopenHelper.getWritableDatabase();

        int count;
        switch (sUriMatcher.match(uri)) {
            case TASKS:
                count = db.delete(TASK_LIST_TABLE_NAME, selection, selectionArgs);
                break;
            case TASK_ID:
                String noteId = uri.getPathSegments().get(1);
                String whereClause = TaskList.Tasks._ID + "=" + noteId + (!TextUtils.isEmpty(selection) ? ("AND(" + selection + ")") : "");
                count = db.delete(TASK_LIST_TABLE_NAME, whereClause, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("uri error! " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mopenHelper.getWritableDatabase();

        int count;
        switch(sUriMatcher.match(uri)) {
            case TASKS:
                count = db.update(TASK_LIST_TABLE_NAME, values, selection, selectionArgs);
                break;
            case TASK_ID:
                String noteId = uri.getPathSegments().get(1);
                String whereClause = TaskList.Tasks._ID + "=" + noteId + (!TextUtils.isEmpty(selection) ? ("AND(" + selection + ")") : "");
                count = db.update(TASK_LIST_TABLE_NAME, values, whereClause, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("uri error! " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    // static statement, only run once, for variable initialization
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(TaskList.AUTHORITY, "taskLists", TASKS);
        sUriMatcher.addURI(TaskList.AUTHORITY, "taskLists/#", TASK_ID);

        sTaskListProjectionMap = new HashMap<>();
        sTaskListProjectionMap.put(TaskList.Tasks._ID, TaskList.Tasks._ID);
        sTaskListProjectionMap.put(TaskList.Tasks.CONTENT, TaskList.Tasks.CONTENT);
        sTaskListProjectionMap.put(TaskList.Tasks.CREATED, TaskList.Tasks.CREATED);
        sTaskListProjectionMap.put(TaskList.Tasks.IS_SOUND_ON, TaskList.Tasks.IS_SOUND_ON);
        sTaskListProjectionMap.put(TaskList.Tasks.DATE1, TaskList.Tasks.DATE1);
        sTaskListProjectionMap.put(TaskList.Tasks.TIME1, TaskList.Tasks.TIME1);
        sTaskListProjectionMap.put(TaskList.Tasks.IS_ON, TaskList.Tasks.IS_ON);

    }
}
