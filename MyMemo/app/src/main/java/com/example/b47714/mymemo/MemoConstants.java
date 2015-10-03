package com.example.b47714.mymemo;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by B47714 on 10/3/2015.
 */
public final class MemoConstants {
    private MemoConstants() {}          // avoid build object

    public final class MemoDB {
        private MemoDB() {}

        public final static String NAME = "myMemo.db";
        public final static int VERSION = 1;
        public final static String TABLE_NAME = "my_memo";
        public final static int URI_TABLE = 1;
        public final static int URI_TABLE_ID = 2;
        public final static String TABLE_TYPE = "vnd.android.cursor.dir/com.example.b47714.mymemo";
        public final static String TABLE_ITEM_TYPE = "vnd.android.cursor.item/com.example.b47714.mymemo";
    }

    public static final class MemoProvider {
        private MemoProvider() {}

        public final static String AUTHORITY = "com.example.b47714.mymemo.provider";
        public final static Uri URI = Uri.parse("content://" + AUTHORITY + "/" + MemoDB.TABLE_NAME);
    }

    public final class TableColumnNames implements BaseColumns{
        private TableColumnNames() {}

        public final static String CONTENT = "Content";
        public final static String DATE = "Date";
        public final static String TIME = "Time";
        public final static String IS_ON = "IsOn";
        public final static String IS_SOUND_ON = "IsSoundOn";
    }

    public final class MemoItemIds {
        private MemoItemIds() {}

        public final static int CONTENT = 0;
        public final static int DATE = 1;
        public final static int TIME = 2;
        public final static int IS_ON = 3;
        public final static int IS_SOUND_ON = 4;
    }
}
