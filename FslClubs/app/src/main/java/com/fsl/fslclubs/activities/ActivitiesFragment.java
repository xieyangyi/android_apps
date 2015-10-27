package com.fsl.fslclubs.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.login.HttpUtil;
import com.fsl.fslclubs.login.User;
import com.fsl.fslclubs.main.MainActivity;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by B47714 on 10/14/2015.
 */
public class ActivitiesFragment extends Fragment {
    public final int ORIGIN_ACTIVITY_COUNT = 5;
    public final int UP_ADD_ACTIVITY_COUNT = 2;
    public final int DOWN_ADD_ACTIVITY_COUNT = 2;
    public final int MSG_NEW_ACTIVITY = 0x123;
    public final int MSG_SET_ACTIVITY_ICON = 0x124;
    public final int MSG_SET_ITEM_ICON = 0x125;

    private ArrayList<String> activityIdList = new ArrayList<>();
    private ArrayList<String> nameList = new ArrayList<>();
    private ArrayList<String> iconAddrList = new ArrayList<>();
    private ArrayList<String> websiteList = new ArrayList<>();
    private ArrayList<String> addressList = new ArrayList<>();
    private ArrayList<String> timeList = new ArrayList<>();
    private ArrayList<String> expireTimeList = new ArrayList<>();

    private ListView listView;
    private List<Map<String, Object>> listItems = new ArrayList<>();
    private SimpleAdapter adapter = null;
    private RadioButton rButton;
    private boolean isNewActivityFound = false;

    private int lastItem;
    private int count;
    private int latestActivityId = 0;
    private int oldestActivityId = 0;

    private Timer timer = new Timer();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_ACTIVITY:
                    isNewActivityFound = true;
                    rButton = (RadioButton)getActivity().findViewById(R.id.main_rbtn_activities);
                    rButton.setBackgroundColor(Color.GRAY);
                    rButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isNewActivityFound) {
                                getNewActivity();
                                isNewActivityFound = false;
                                rButton.setBackgroundColor(Color.WHITE);
                            }
                        }
                    });
                    break;
                case MSG_SET_ACTIVITY_ICON:
                    for (int i = listView.getFirstVisiblePosition() ; i <= listView.getLastVisiblePosition(); i++) {
                        setActivityIcon(i, iconAddrList.get(i));
                    }
                    break;
                case MSG_SET_ITEM_ICON:
                    int listItemIndex = msg.arg1;
                    Bitmap bitmap = (Bitmap)msg.obj;
                    LinearLayout layout =  (LinearLayout)listView.getChildAt(listItemIndex);
                    ImageView imageView = (ImageView)layout.findViewById(R.id.adapter_activity_icon);
                    imageView.setImageBitmap(bitmap);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the latest activity info and form the origin list items data
        latestActivityId = getLatestActivityId();
        oldestActivityId = (latestActivityId >= ORIGIN_ACTIVITY_COUNT) ? (latestActivityId - ORIGIN_ACTIVITY_COUNT + 1) : 1;
        for(int i = latestActivityId; i >= oldestActivityId; i--) {
           formListItems(i);
        }

        count = listItems.size();

        // set adapter if network is good
        if (count > 0) {
            adapter = new SimpleAdapter(
                    getActivity(),
                    listItems,
                    R.layout.adapter_activities,
                    new String[]{"name", "time", "address"},
                    new int[]{R.id.adapter_activity_name, R.id.adapter_activity_time, R.id.adapter_activity_address}
            );
            checkNewActivity();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities, null);
        rButton = (RadioButton)getActivity().findViewById(R.id.main_rbtn_activities);
        listView = (ListView)view.findViewById(R.id.fragment_activities_list);
        listView.setAdapter(adapter);

        // set listview icon, must using post method because setApdater is not a synchronise method.
        listView.post(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(MSG_SET_ACTIVITY_ICON);
            }
        });

        // listview item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString("activityId", activityIdList.get(position));
                bundle.putString("name", nameList.get(position));
                bundle.putString("icon", iconAddrList.get(position));
                bundle.putString("website", websiteList.get(position));
                bundle.putString("address", addressList.get(position));
                bundle.putString("time", timeList.get(position));
                bundle.putString("expireTime", expireTimeList.get(position));
                Intent intent = new Intent(getActivity(), ActivitiesActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        // down scroll to get more activities
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (lastItem == count && scrollState == SCROLL_STATE_IDLE) {
                    if (oldestActivityId > 1) {
                        int tmpActivityId = oldestActivityId - 1;
                        oldestActivityId = (tmpActivityId >= DOWN_ADD_ACTIVITY_COUNT) ? (tmpActivityId - DOWN_ADD_ACTIVITY_COUNT + 1) : 1;
                        for (int i = tmpActivityId; i >= oldestActivityId; i--) {
                            formListItems(i);
                        }
                        count = listItems.size();
                        adapter.notifyDataSetChanged();
                        handler.sendEmptyMessage(MSG_SET_ACTIVITY_ICON);
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.already_oldest), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItem = firstVisibleItem + visibleItemCount;
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private String[] getActivityInfo(int ActivityId) {
         /* query server for phoneNo and password */
        String[] results = null;
        Map<String, String> params = new HashMap<>();
        params.put("id", ActivityId + "");

        final String url = HttpUtil.BASE_URL + "activities";
        String queryResult = null;
        try {
            queryResult = HttpUtil.queryStringForPost(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check query result
        if (queryResult == null) {
            Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        } else {
            results = queryResult.split(",");
        }

        return results;
    }

    private int getLatestActivityId() {
         /* query server */
        int result = 0;
        Map<String, String> params = new HashMap<>();
        params.put("id", "0");          // id=0 means the latest activity

        final String url = HttpUtil.BASE_URL + "activities";
        String queryResult = null;
        try {
            queryResult = HttpUtil.queryStringForPost(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check query result
        if (queryResult == null) {
            Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        } else {
            result = Integer.valueOf(queryResult.split(",")[0]);
        }

        return result;
    }

    private void formListItems(int activityId) {
        // get activity info: name, icon, website
        String[] info = getActivityInfo(activityId);
        if (info == null)
            return;

        String id = info[0];
        String name = info[1];
        String icon = info[2];
        String website = info[3];
        String address = info[4];
        String time = info[5];
        String expireTime = info[6];
        activityIdList.add(id);
        nameList.add(name);
        iconAddrList.add(icon);
        websiteList.add(website);
        addressList.add(address);
        timeList.add(time);
        expireTimeList.add(expireTime);

        Map<String, Object> map = new HashMap<>();
        map.put("icon", null);
        map.put("name", name);
        map.put("time", time);
        map.put("address", address);
        listItems.add(map);
    }

    private void getNewActivity() {
        latestActivityId = getLatestActivityId();
        listItems.clear();
        for (int i = latestActivityId; i >= oldestActivityId; i--) {
            formListItems(i);
        }
        count = listItems.size();
        adapter.notifyDataSetChanged();
    }

    private void checkNewActivity() {
        if (timer != null) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (getLatestActivityId() > latestActivityId) {
                        handler.sendEmptyMessage(0x123);
                    }
                }
            }, 1000, 30000);
        }
    }

    private void setActivityIcon(final int listItemIndex, final String iconAddr) {

        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(HttpUtil.ACTIVITY_URL + iconAddr);
                    InputStream inputStream = url.openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();

                    // form message
                    Message msg = handler.obtainMessage();
                    msg.what = MSG_SET_ITEM_ICON;
                    msg.arg1 = listItemIndex;
                    msg.obj = bitmap;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
