package com.fsl.fslclubs.myEvents;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.util.SlidingLayout;

import java.util.Calendar;

/**
 * Created by B47714 on 10/14/2015.
 */
public class MyEventsFragment extends Fragment {
    private SlidingLayout slidingLayout;
    private ListView listView;
    private SimpleCursorAdapter adapter;
    private static final String [] PROJECTION = new String[] {
            MyEventProvider.TBL_ID,              // 0
            MyEventProvider.TBL_ACTIVITY_ID,     // 1
            MyEventProvider.TBL_NAME,            // 2
            MyEventProvider.TBL_ICON,            // 3
            MyEventProvider.TBL_WEBSITE,         // 4
            MyEventProvider.TBL_ADDRESS,         // 5
            MyEventProvider.TBL_TIME,            // 6
            MyEventProvider.TBL_EXPIRE_TIME,     // 7
            MyEventProvider.TBL_ALARM_DATE,      // 8
            MyEventProvider.TBL_ALARM_TIME,      // 9
            MyEventProvider.TBL_ALARM_IS_ON,     // 10
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x123:
                    for (int i = 0; i < listView.getChildCount(); i++) {
                        LinearLayout layout = (LinearLayout) listView.getChildAt(i);
                        TextView txtActivityName = (TextView) layout.findViewById(R.id.adapter_my_activity_name);
                        TextView txtActivityTime = (TextView) layout.findViewById(R.id.adapter_my_activity_time);
                        String time = txtActivityTime.getText().toString();

                        if (isActivityPast(time)) {
                            txtActivityName.setTextColor(Color.BLACK);
                        } else {
                            txtActivityName.setTextColor(Color.RED);
                        }

                        // display alarm date and time if alarm is on
                        TextView txtAlarmDate = (TextView)layout.findViewById(R.id.adapter_my_activity_alarm_date);
                        TextView txtAlarmTime = (TextView)layout.findViewById(R.id.adapter_my_activity_alarm_time);
                        TextView txtAlarmIsOn = (TextView)layout.findViewById(R.id.adapter_my_activity_alarm_is_on);
                        boolean isAlarmOn = txtAlarmIsOn.getText().toString().equals("1") ? true : false;
                        if (!isAlarmOn) {
                            txtAlarmDate.setText("");
                            txtAlarmTime.setText("");
                        }
                        txtAlarmIsOn.setText("");
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = MyEventProvider.USER_TBL_URI;
        Cursor cursor = getActivity().getContentResolver().query(uri, PROJECTION, null, null, null);
        adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.adapter_my_activity,
                cursor,
                new String[] { MyEventProvider.TBL_NAME, MyEventProvider.TBL_TIME,
                        MyEventProvider.TBL_ADDRESS, MyEventProvider.TBL_ALARM_DATE,
                        MyEventProvider.TBL_ALARM_TIME, MyEventProvider.TBL_ALARM_IS_ON },
                new int[] { R.id.adapter_my_activity_name, R.id.adapter_my_activity_time,
                        R.id.adapter_my_activity_address, R.id.adapter_my_activity_alarm_date,
                        R.id.adapter_my_activity_alarm_time, R.id.adapter_my_activity_alarm_is_on },
                0
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_activity, null);
        listView = (ListView)view.findViewById(R.id.fragment_my_activity_list);
        listView.setAdapter(adapter);
        slidingLayout = (SlidingLayout) getActivity().findViewById(R.id.sliding_layout);
        slidingLayout.setScrollEvent(listView);
        listView.post(new Runnable() {
            @Override
            public void run() {
                // set activity name text color as red if activity not past
                handler.sendEmptyMessage(0x123);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = ContentUris.withAppendedId(MyEventProvider.USER_TBL_URI, id);
                Cursor cursor = getActivity().getContentResolver().query(uri, PROJECTION, null, null, null);
                if (cursor.moveToNext()) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(MyEventProvider.TBL_ID, cursor.getInt(0));
                    bundle.putString(MyEventProvider.TBL_NAME, cursor.getString(2));
                    bundle.putString(MyEventProvider.TBL_ALARM_DATE, cursor.getString(8));
                    bundle.putString(MyEventProvider.TBL_ALARM_TIME, cursor.getString(9));
                    bundle.putBoolean(MyEventProvider.TBL_ALARM_IS_ON, (cursor.getInt(10) == 0) ? false : true);

                    Intent intent = new Intent(getActivity(), MyEventActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private boolean isActivityPast(String time) {
        String[] timeStr = time.split("-");
        int year = Integer.valueOf(timeStr[0]);
        int month = Integer.valueOf(timeStr[1]);
        int dayOfMonth = Integer.valueOf(timeStr[2]);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, dayOfMonth, 20, 0);       // month is from 0~11 in calendar

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            return true;
        } else {
            return false;
        }
    }
}
