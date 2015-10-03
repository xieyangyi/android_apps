package com.example.b47714.mymemo;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by B47714 on 9/28/2015.
 */
public final class TaskList {
    public static final String AUTHORITY = "com.example.b47714.mymemo.TaskList";

    private TaskList () {}          // can't create sub class outside

    public static final class Tasks implements BaseColumns {
        private Tasks() {}

        // Uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/taskLists");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.amaker.taskList";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.amaker.taskList";

        public static final String DEFAULT_SORT_ORDER = "created DESC";
        public static final String CONTENT = "content";
        public static final String CREATED = "created";
        public static final String DATE1 = "date1";
        public static final String TIME1 = "time1";
        public static final String IS_ON = "isOn";
        public static final String IS_SOUND_ON = "isSoundOn";

    }

    public final static class TaskDetailItem {
        private TaskDetailItem() {}

        public static final int CONTENT = 0;
        public static final int TIME = 1;
        public static final int DATE = 2;
        public static final int IS_ON = 3;
        public static final int IS_SOUND_ON = 4;
    }
}
