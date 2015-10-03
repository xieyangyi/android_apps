package com.example.b47714.mymemo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.b47714.mymemo.MemoConstants.*;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by B47714 on 10/3/2015.
 */
public class MemoItemActivity extends Activity {
    private Button btnReturn;
    private ListView listViewItem;
    private LayoutInflater layoutInflater;
    private String itemContent, itemDate, itemTime;
    private Boolean itemIsOn = false, itemIsSoundOn = false;
    private int year, month, dayOfMonth, hourOfDay, minute;
    private TextView txtContentName, txtContentDesc, txtDateName, txtDateDesc,
            txtTimeName, txtTimeDesc;
    private CheckedTextView cTextView1, cTextView2;
    private int itemId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_item);
        btnReturn = (Button)findViewById(R.id.btn_return);
        listViewItem = (ListView)findViewById(R.id.list_memo_item);

        // display item list
        final MemoItemAdapter adapter = new MemoItemAdapter();
        listViewItem.setAdapter(adapter);
        layoutInflater = getLayoutInflater();

        // use current time when activity first created
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        // set item click listener
        listViewItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case MemoItemIds.CONTENT:
                        showContentDialog();
                        break;
                    case MemoItemIds.DATE:
                        showDialog(MemoItemIds.DATE);
                        break;
                    case MemoItemIds.TIME:
                        showDialog(MemoItemIds.TIME);
                        break;
                    case MemoItemIds.IS_ON:
                        cTextView1 = (CheckedTextView)view;
                        if(cTextView1.isChecked()) {
                            itemIsOn = false;
                            setAlarm(false);
                        } else {
                            itemIsOn = true;
                            setAlarm(true);
                        }
                        break;
                    case MemoItemIds.IS_SOUND_ON:
                        cTextView2 = (CheckedTextView)view;
                        if(cTextView2.isChecked()) {
                            itemIsSoundOn = false;
                        } else {
                            itemIsSoundOn = true;
                        }
                        break;
                    default:
                        break;
                }

                // invalidate listview
                adapter.notifyDataSetChanged();
            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemoItemActivity.this, MemoListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            itemId = bundle.getInt(TableColumnNames._ID);
            itemContent = bundle.getString(TableColumnNames.CONTENT);
            itemDate = bundle.getString(TableColumnNames.DATE);
            itemTime = bundle.getString(TableColumnNames.TIME);
            itemIsOn = bundle.getBoolean(TableColumnNames.IS_ON);
            itemIsSoundOn = bundle.getBoolean(TableColumnNames.IS_SOUND_ON);

            if (itemDate != null) {
                String strs[] = itemDate.split("/");
                year = Integer.parseInt(strs[0]);
                month = Integer.parseInt(strs[1]) - 1;      // make it 0~11
                dayOfMonth = Integer.parseInt(strs[2]);
            }

            if (itemTime != null) {
                String strs[] = itemTime.split(":");
                hourOfDay = Integer.parseInt(strs[0]);
                minute = Integer.parseInt(strs[1]);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveOrUpdate();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case MemoItemIds.DATE:
                return new DatePickerDialog(
                        this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                MemoItemActivity.this.year = year;
                                MemoItemActivity.this.month = monthOfYear;
                                MemoItemActivity.this.dayOfMonth = dayOfMonth;
                                txtDateDesc.setText(year + "/" + (month+1) + "/" + dayOfMonth);
                            }
                        },
                        year,
                        month,
                        dayOfMonth
                );
            case MemoItemIds.TIME:
                return new TimePickerDialog(
                        this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                MemoItemActivity.this.hourOfDay = hourOfDay;
                                MemoItemActivity.this.minute = minute;
                                txtTimeDesc.setText(hourOfDay + ":" + minute);
                            }
                        },
                        hourOfDay,
                        minute,
                        false
                );
        }
        return null;
    }

    private void showContentDialog() {
        View view = layoutInflater.inflate(R.layout.item_content, null);
        final EditText edtContent = (EditText)view.findViewById(R.id.content);
        edtContent.setText(itemContent);
        new AlertDialog.Builder(this)
                .setView(view)
                .setTitle("Content")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemContent = edtContent.getText().toString();
                        txtContentDesc.setText(itemContent);
                    }
                })
                .show();
    }

    private void saveOrUpdate() {
        ContentValues values = new ContentValues();
        values.clear();

        values.put(TableColumnNames.CONTENT, txtContentDesc.getText().toString());
        values.put(TableColumnNames.DATE, txtDateDesc.getText().toString());
        values.put(TableColumnNames.TIME, txtTimeDesc.getText().toString());
        values.put(TableColumnNames.IS_ON, cTextView1.isChecked() ? 1 : 0);
        values.put(TableColumnNames.IS_SOUND_ON, cTextView2.isChecked() ? 1 : 0);

        if(itemId != 0) {
            Uri uri = ContentUris.withAppendedId(MemoProvider.URI, itemId);
            getContentResolver().update(uri, values, null, null);
        } else {
            Uri uri = MemoProvider.URI;
            getContentResolver().insert(uri, values);
        }
    }

    private void setAlarm(boolean isToSet) {
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        // get pendingIntent
        Bundle bundle = new Bundle();
        bundle.putString(TableColumnNames.CONTENT, itemContent);
        bundle.putBoolean(TableColumnNames.IS_SOUND_ON, itemIsSoundOn);

        final String BC_ACTION = "com.example.b47714.mymemo.AlarmReceiver";
        Intent intent = new Intent(BC_ACTION);
        intent.putExtras(bundle);

        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        // compare set time and current time, then set or cancel
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, hourOfDay, minute);
        long setedTime = calendar.getTimeInMillis();
        long currentTime = System.currentTimeMillis();


        if (isToSet && setedTime > currentTime) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, setedTime, pi);
        } else {
            alarmManager.cancel(pi);
        }

    }

     class MemoItemAdapter extends BaseAdapter {
        String itemNames[] = new String[] {
                TableColumnNames.CONTENT,
                TableColumnNames.DATE,
                TableColumnNames.TIME,
                TableColumnNames.IS_ON,
                TableColumnNames.IS_SOUND_ON,
        };
        @Override
        public int getCount() {
            return itemNames.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = layoutInflater.inflate(R.layout.adapter_memo_item, null);

            switch (position) {
                case MemoItemIds.CONTENT:
                    txtContentName = (TextView)view.findViewById(R.id.name);
                    txtContentDesc = (TextView)view.findViewById(R.id.desc);
                    txtContentName.setText(itemNames[position]);
                    txtContentDesc.setText(itemContent);
                    return view;
                case MemoItemIds.DATE:
                    txtDateName = (TextView)view.findViewById(R.id.name);
                    txtDateDesc = (TextView)view.findViewById(R.id.desc);
                    txtDateName.setText(itemNames[position]);
                    txtDateDesc.setText(year + "/" + (month+1) + "/" + dayOfMonth); // month is 0~11 in Calendar
                    return view;
                case MemoItemIds.TIME:
                    txtTimeName = (TextView)view.findViewById(R.id.name);
                    txtTimeDesc = (TextView)view.findViewById(R.id.desc);
                    txtTimeName.setText(itemNames[position]);
                    txtTimeDesc.setText(hourOfDay + ":" + minute);
                    return view;
                case MemoItemIds.IS_ON:
                    cTextView1 = (CheckedTextView)layoutInflater.inflate(
                            android.R.layout.simple_list_item_multiple_choice, null
                    );
                    cTextView1.setText(itemNames[position]);
                    cTextView1.setChecked(itemIsOn);
                    return cTextView1;
                case MemoItemIds.IS_SOUND_ON:
                    cTextView2 = (CheckedTextView)layoutInflater.inflate(
                            android.R.layout.simple_list_item_multiple_choice, null
                    );
                    cTextView2.setText(itemNames[position]);
                    cTextView2.setChecked(itemIsSoundOn);
                    return cTextView2;
                default:
                    return null;
            }
        }
    }
}
