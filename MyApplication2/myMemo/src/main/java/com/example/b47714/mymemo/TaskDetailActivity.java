package com.example.b47714.mymemo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
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
import com.example.b47714.mymemo.TaskList.*;

import org.w3c.dom.Text;

import java.util.Calendar;

/**
 * Created by B47714 on 9/30/2015.
 */
public class TaskDetailActivity extends Activity {
    private ListView listView;
    private Button btnReturn;
    private int mYear, mMonth, mDay;
    private int mHour, mMinute;
    private LayoutInflater layoutInflater;
    private CheckedTextView checkedTextView1, checkedTextView2;
    private String content, date1, time1;
    private int id1;
    private boolean isOn, isSoundOn;
    private TextView dataName, dataDesc;
    private TextView timeName, timeDesc;
    private TextView contentName, contentDesc;
    private final int DATE_DIALOG_ID = 1;
    private final int TIME_DIALOG_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_detail_activity);
        listView = (ListView)findViewById(R.id.list_detail_task);
        btnReturn = (Button)findViewById(R.id.btn_return);

        // get and set ListView
        final ViewAdapter myAdapter = new ViewAdapter();
        listView.setAdapter(myAdapter);
        layoutInflater = getLayoutInflater();

        // get Calendar and set data and time by current
        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);      // Time.MONTH and Calendar.MONTH is 0-11 by default, add 1
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);     // 24 hours
        mMinute = calendar.get(Calendar.MINUTE);

        // set item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case TaskDetailItem.CONTENT:
                        myShowDialog("please input conent: ");
                        break;
                    case TaskDetailItem.TIME:
                        showDialog(TIME_DIALOG_ID);
                        break;
                    case TaskDetailItem.DATE:
                        showDialog(DATE_DIALOG_ID);
                        break;
                    case TaskDetailItem.IS_ON:
                        checkedTextView1 = (CheckedTextView) view;
                        if (checkedTextView1.isChecked()) {
                            isOn = false;       // already checked, so this click is to cancel checked
                            setAlarm(false);
                        } else {
                            isOn = true;
                            setAlarm(true);
                        }
                        break;
                    case TaskDetailItem.IS_SOUND_ON:
                        checkedTextView2 = (CheckedTextView) view;
                        isSoundOn = !checkedTextView2.isChecked();
                        break;
                    default:
                        break;
                }
                myAdapter.notifyDataSetChanged();     // invalidate listview
            }
        });

        // button return
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskDetailActivity.this, TaskListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle bundle = getIntent().getBundleExtra("bundle");
        if(bundle != null) {
            // get the data from bundle
            id1 = bundle.getInt("id");
            content = bundle.getString("content");
            date1 = bundle.getString("date");
            time1 = bundle.getString("time");
            isOn = bundle.getBoolean("isOn");
            isSoundOn = bundle.getBoolean("isSoundOn");

            if(date1 != null && date1.length() > 0) {
                String[] strs = date1.split("/");
                mYear = Integer.parseInt(strs[0]);
                mMonth = Integer.parseInt(strs[1]) - 1;     // add one when save in database already, must let it be 0~11
                mDay = Integer.parseInt(strs[2]);
            }
            if(time1 != null && time1.length() > 0) {
                String[] strs = time1.split(":");
                mHour = Integer.parseInt(strs[0]);
                mMinute = Integer.parseInt(strs[1]);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveOrUpdate();
    }

    class ViewAdapter extends BaseAdapter {
        String strs[] = new String[]{"Content", "Time", "Date", "IsOn", "IsSoundOn"};

        @Override
        public int getCount() {
            return strs.length;
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
            View view = layoutInflater.inflate(R.layout.task_detail_item, null);
            switch (position) {
                case TaskDetailItem.CONTENT:
                    contentName = (TextView) view.findViewById(R.id.name);
                    contentDesc = (TextView) view.findViewById(R.id.desc);
                    contentName.setText(strs[position]);
                    contentDesc.setText(content);
                    return view;
                case TaskDetailItem.TIME:
                    timeName = (TextView) view.findViewById(R.id.name);
                    timeDesc = (TextView) view.findViewById(R.id.desc);
                    timeName.setText(strs[position]);
                    timeDesc.setText(mHour + ":" + mMinute);
                    return view;
                case TaskDetailItem.DATE:
                    dataName = (TextView) view.findViewById(R.id.name);
                    dataDesc = (TextView) view.findViewById(R.id.desc);
                    dataName.setText(strs[position]);
                    dataDesc.setText(mYear + "/" + (mMonth + 1) + "/" + mDay);
                    return view;
                case TaskDetailItem.IS_ON:
                    checkedTextView1 = (CheckedTextView) layoutInflater.inflate(
                            android.R.layout.simple_list_item_multiple_choice, null
                    );
                    checkedTextView1.setText(strs[position]);
                    checkedTextView1.setChecked(isOn);
                    return checkedTextView1;
                case TaskDetailItem.IS_SOUND_ON:
                    checkedTextView2 = (CheckedTextView) layoutInflater.inflate(
                            android.R.layout.simple_list_item_multiple_choice, null
                    );
                    checkedTextView2.setText(strs[position]);
                    checkedTextView2.setChecked(isSoundOn);
                    return checkedTextView2;
                default:
                    return null;
            }

        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(
                        this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                mYear = year;
                                mMonth = monthOfYear;
                                mDay = dayOfMonth;
                                dataDesc.setText(mYear + "/" + (mMonth+1) + "/" + mDay);
                            }
                        },
                        mYear,
                        mMonth,
                        mDay
                );
            case TIME_DIALOG_ID:
                return new TimePickerDialog(
                        this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                mHour = hourOfDay;
                                mMinute = minute;
                                timeDesc.setText(mHour + ":" + mMinute);
                            }
                        },
                        mHour,
                        mMinute,
                        false
                );
        }

        return null;
    }

    private void myShowDialog(String msg) {
        View view = layoutInflater.inflate(R.layout.item_content, null);
        final EditText edtContent = (EditText)view.findViewById(R.id.content);
        edtContent.setText(content);
        new AlertDialog.Builder(this)
                .setView(view)
                .setTitle("content")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        content = edtContent.getText().toString();
                        contentDesc.setText(content);
                    }
                })
                .show();
    }

    private void saveOrUpdate() {
        ContentValues values = new ContentValues();
        values.clear();
        values.put(Tasks.CONTENT, contentDesc.getText().toString());
        values.put(Tasks.TIME1, timeDesc.getText().toString());
        values.put(Tasks.DATE1, dataDesc.getText().toString());
        values.put(Tasks.IS_ON, checkedTextView1.isChecked() ? 1 : 0);
        values.put(Tasks.IS_SOUND_ON, checkedTextView2.isChecked() ? 1 : 0);

        if (id1 != 0) {
            // update
            Uri uri = ContentUris.withAppendedId(TaskList.Tasks.CONTENT_URI, id1);
            getContentResolver().update(uri, values, null, null);
        } else {
            // save
            Uri uri = TaskList.Tasks.CONTENT_URI;
            getContentResolver().insert(uri, values);
        }
    }

    private void setAlarm(boolean isToSet) {
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        Bundle bundle = new Bundle();
        bundle.putString("content", content);
        bundle.putBoolean("isSoundOn", isSoundOn);

        final String ALRAM_BROADCAST_ACTION = "com.example.b47714.mymemo.TaskReceiver";
        Intent intent = new Intent(ALRAM_BROADCAST_ACTION);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        long currentTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth, mDay, mHour, mMinute);
        long calendarTime = calendar.getTimeInMillis();

        if(isToSet && (calendarTime > currentTime)) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendarTime, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
        }
    }
}