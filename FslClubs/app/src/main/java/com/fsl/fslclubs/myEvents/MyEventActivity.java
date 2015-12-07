package com.fsl.fslclubs.myEvents;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.Calendar;
import com.fsl.fslclubs.R;
import com.fsl.fslclubs.main.MainActivity;

/**
 * Created by B47714 on 10/3/2015.
 */
public class MyEventActivity extends Activity {
    private ListView listViewItem;
    private Button btnSave;
    private LayoutInflater layoutInflater;
    private String itemContent, itemDate, itemTime;
    private Boolean itemIsOn = false, itemIsSoundOn = false;
    private int year, month, dayOfMonth, hourOfDay, minute;
    private TextView txtContentName, txtContentDesc, txtDateName, txtDateDesc,
            txtTimeName, txtTimeDesc;
    private CheckedTextView cTextView1;
    private int itemId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);     // set action bar navigation
        listViewItem = (ListView)findViewById(R.id.list_my_activity_alarm);
        btnSave = (Button)findViewById(R.id.btn_activity_alarm_save);

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
                    case 0:
                        break;
                    case 1:
                        showDialog(1);
                        break;
                    case 2:
                        showDialog(2);
                        break;
                    case 3:
                        cTextView1 = (CheckedTextView) view;
                        if (cTextView1.isChecked()) {
                            itemIsOn = false;
                            setAlarm(false);
                        } else {
                            itemIsOn = true;
                            setAlarm(true);
                        }
                        break;
                    default:
                        break;
                }

                // invalidate listview
                adapter.notifyDataSetChanged();
            }
        });

        // save alarm info button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrUpdate();
                Bundle bundle = new Bundle();
                bundle.putInt("radioButtonCheckId", R.id.main_rbtn_friends);
                Intent intent = new Intent(MyEventActivity.this, MainActivity.class);
                intent.putExtras(bundle);
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
            itemId = bundle.getInt(MyEventProvider.TBL_ID);
            itemContent = bundle.getString(MyEventProvider.TBL_NAME);
            itemDate = bundle.getString(MyEventProvider.TBL_ALARM_DATE);
            itemTime = bundle.getString(MyEventProvider.TBL_ALARM_TIME);
            itemIsOn = bundle.getBoolean(MyEventProvider.TBL_ALARM_IS_ON);

            if (itemDate != null && !itemDate.equals("")) {
                String strs[] = itemDate.split("/");
                year = Integer.parseInt(strs[0]);
                month = Integer.parseInt(strs[1]) - 1;      // make it 0~11
                dayOfMonth = Integer.parseInt(strs[2]);
            }

            if (itemTime != null && !itemTime.equals("")) {
                String strs[] = itemTime.split(":");
                hourOfDay = Integer.parseInt(strs[0]);
                minute = Integer.parseInt(strs[1]);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new DatePickerDialog(
                        this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                MyEventActivity.this.year = year;
                                MyEventActivity.this.month = monthOfYear;
                                MyEventActivity.this.dayOfMonth = dayOfMonth;
                                txtDateDesc.setText(year + "/" + (month+1) + "/" + dayOfMonth);
                            }
                        },
                        year,
                        month,
                        dayOfMonth
                );
            case 2:
                return new TimePickerDialog(
                        this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                MyEventActivity.this.hourOfDay = hourOfDay;
                                MyEventActivity.this.minute = minute;
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

    private void saveOrUpdate() {
        ContentValues values = new ContentValues();
        values.clear();

        values.put(MyEventProvider.TBL_NAME, txtContentDesc.getText().toString());
        values.put(MyEventProvider.TBL_ALARM_DATE, txtDateDesc.getText().toString());
        values.put(MyEventProvider.TBL_ALARM_TIME, txtTimeDesc.getText().toString());
        values.put(MyEventProvider.TBL_ALARM_IS_ON, cTextView1.isChecked() ? 1 : 0);

        if(itemId != 0) {
            Uri uri = ContentUris.withAppendedId(MyEventProvider.USER_TBL_URI, itemId);
            getContentResolver().update(uri, values, null, null);
        } else {
            Uri uri = MyEventProvider.USER_TBL_URI;
            getContentResolver().insert(uri, values);
        }
    }

    private void setAlarm(boolean isToSet) {
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        // get pendingIntent
        Bundle bundle = new Bundle();
        bundle.putString(MyEventProvider.TBL_NAME, itemContent);
        bundle.putBoolean(MyEventProvider.TBL_ALARM_IS_ON, true);

        final String BC_ACTION = "com.fsl.fslclubs.AlarmReceiver";
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
                getString(R.string.activity_name),
                getString(R.string.activity_date),
                getString(R.string.activity_time),
                getString(R.string.activity_is_alarm_on),
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
            View view = layoutInflater.inflate(R.layout.adapter_my_activity_alarm, null);

            switch (position) {
                case 0:
                    txtContentName = (TextView)view.findViewById(R.id.name);
                    txtContentDesc = (TextView)view.findViewById(R.id.desc);
                    txtContentName.setText(itemNames[position]);
                    txtContentDesc.setText(itemContent);
                    return view;
                case 1:
                    txtDateName = (TextView)view.findViewById(R.id.name);
                    txtDateDesc = (TextView)view.findViewById(R.id.desc);
                    txtDateName.setText(itemNames[position]);
                    txtDateDesc.setText(year + "/" + (month+1) + "/" + dayOfMonth); // month is 0~11 in Calendar
                    return view;
                case 2:
                    txtTimeName = (TextView)view.findViewById(R.id.name);
                    txtTimeDesc = (TextView) view.findViewById(R.id.desc);
                    txtTimeName.setText(itemNames[position]);
                    txtTimeDesc.setText(hourOfDay + ":" + minute);
                    return view;
                case 3:
                    cTextView1 = (CheckedTextView)layoutInflater.inflate(
                            android.R.layout.simple_list_item_multiple_choice, null
                    );
                    cTextView1.setText(itemNames[position]);
                    cTextView1.setChecked(itemIsOn);
                    return cTextView1;
                default:
                    return null;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
