package com.example.b47714.mymemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.example.b47714.mymemo.MemoConstants.*;


public class MemoListActivity extends Activity {
    private Button btnAdd;
    private ListView listViewMemo;
    private final String projection[] = new String[] {
            TableColumnNames._ID,
            TableColumnNames.CONTENT,
            TableColumnNames.DATE,
            TableColumnNames.TIME,
            TableColumnNames.IS_ON,
            TableColumnNames.IS_SOUND_ON,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list);

        btnAdd = (Button)findViewById(R.id.btn_add);
        listViewMemo = (ListView)findViewById(R.id.list_memos);

        // display memoList
        displayMemoList();

        // set item click for memoList
        listViewMemo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = ContentUris.withAppendedId(MemoProvider.URI, id);
                ContentResolver resolver = MemoListActivity.this.getContentResolver();
                Cursor cursor = resolver.query(uri, projection, null, null, null);

                if(cursor.moveToNext()) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(TableColumnNames._ID, cursor.getInt(0));
                    bundle.putString(TableColumnNames.CONTENT, cursor.getString(1));
                    bundle.putString(TableColumnNames.DATE, cursor.getString(2));
                    bundle.putString(TableColumnNames.TIME, cursor.getString(3));
                    bundle.putBoolean(TableColumnNames.IS_ON, (cursor.getInt(4) != 0) ? true : false);
                    bundle.putBoolean(TableColumnNames.IS_SOUND_ON, (cursor.getInt(5) != 0) ? true : false);

                    Intent intent = new Intent(MemoListActivity.this, MemoItemActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        // set item long click for memolist
        listViewMemo.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Uri uri = ContentUris.withAppendedId(MemoProvider.URI, id);
                final ContentResolver resolver = MemoListActivity.this.getContentResolver();

                new AlertDialog.Builder(MemoListActivity.this)
                        .setTitle("delete")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resolver.delete(uri, null, null);
                                displayMemoList();
                            }
                        });

                return true;
            }
        });

        // set add button
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemoListActivity.this, MemoItemActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        displayMemoList();
    }

    private void displayMemoList() {

        ContentResolver resolver = getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(MemoProvider.URI, projection, null, null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.adapter_memo_list,
                cursor,
                new String[]{TableColumnNames.CONTENT, TableColumnNames.DATE, TableColumnNames.TIME},
                new int[]{R.id.txt_memo_item_contet, R.id.txt_memo_item_date, R.id.txt_memo_item_time},
                0
        );
        listViewMemo.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_memo_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
