package com.example.b47714.mymemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Created by B47714 on 9/30/2015.
 */
public class TaskListActivity extends Activity {
    private final static String[] tableColumnNames = new String[] {
            TaskList.Tasks._ID,         // 0
            TaskList.Tasks.CONTENT,     //1
            TaskList.Tasks.CREATED,     //2
            TaskList.Tasks.IS_SOUND_ON,       //3
            TaskList.Tasks.DATE1,       //4
            TaskList.Tasks.TIME1,       //5
            TaskList.Tasks.IS_ON,      //6
    };
    private Button btnAdd;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list_activity);

        listView = (ListView)findViewById(R.id.list_tasks);
        btnAdd = (Button)findViewById(R.id.btn_add);

        // query data from memo, use simpleCursorAdapter to display listView
        displayListView(listView);

        // click for task list items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v("TaskListActivity", "listview item click" + id);
                Uri uri = ContentUris.withAppendedId(TaskList.Tasks.CONTENT_URI, id);
                ContentResolver resolver = TaskListActivity.this.getContentResolver();
                Cursor cursor1 = resolver.query(uri, tableColumnNames, null, null, TaskList.Tasks.DEFAULT_SORT_ORDER);
                if (cursor1.moveToNext()) {
                    int id1 = cursor1.getInt(0);
                    String content = cursor1.getString(1);
                    String created = cursor1.getString(2);
                    boolean isSoundOn = (cursor1.getInt(3) != 0) ? true : false;
                    String date1 = cursor1.getString(4);
                    String time1 = cursor1.getString(5);
                    boolean isOn = (cursor1.getInt(6) != 0) ? true : false;

                    Bundle bundle = new Bundle();
                    bundle.putInt("id", id1);
                    bundle.putString("content", content);
                    bundle.putString("created", created);
                    bundle.putString("date", date1);
                    bundle.putString("time", time1);
                    bundle.putBoolean("isOn", isOn);
                    bundle.putBoolean("isSoundOn", isSoundOn);

                    Intent intent = new Intent(TaskListActivity.this, TaskDetailActivity.class);
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                }
            }
        });

        // log click for task list items
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v("TaskListActivity", "listview item long click" + id);
                final Uri uri = ContentUris.withAppendedId(TaskList.Tasks.CONTENT_URI, id);
                final ContentResolver resolver = TaskListActivity.this.getContentResolver();

                new AlertDialog.Builder(TaskListActivity.this)
                        .setTitle("delete")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resolver.delete(uri, null, null);
                                displayListView(listView);
                                listView.postInvalidate();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return true;
            }
        });

        // click for add new task button
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskListActivity.this, TaskDetailActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        displayListView(listView);
    }

    private void displayListView(ListView listView) {
        Cursor cursor = getContentResolver().query(
                TaskList.Tasks.CONTENT_URI,
                tableColumnNames,
                null,
                null,
                TaskList.Tasks.DEFAULT_SORT_ORDER
        );
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.task_list_item,
                cursor,
                new String[]{TaskList.Tasks.CONTENT, TaskList.Tasks.DATE1, TaskList.Tasks.TIME1},
                new int[]{R.id.txt_task_item_contet, R.id.txt_task_item_date, R.id.txt_task_item_time},
                0
        );
        listView.setAdapter(adapter);
    }

}
