package com.fsl.fslclubs.events;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.util.HttpUtil;
import com.fsl.fslclubs.util.BitmapCacheUtil;
import com.fsl.fslclubs.util.SlidingLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by B47714 on 10/14/2015.
 */
public class EventsFragment extends Fragment {
    public final int ORIGIN_ACTIVITY_COUNT = 4;
    public final int UP_ADD_ACTIVITY_COUNT = 2;
    public final int DOWN_ADD_ACTIVITY_COUNT = 2;
    public final int MSG_NEW_ACTIVITY = 0x123;
    public final int MSG_ADAPTER_DATA_CHANGED = 0x124;

    private SlidingLayout slidingLayout;
    private RefreshableView refreshableView;
    private ListView listView;
    private EventAdapter adapter = null;
    private List<Map<String, Object>> listItems = new ArrayList<>();
    private RadioButton rButton;
    private boolean isNewEventFound = false;
    private List<String> listUrl = new ArrayList<>();
    private List<ImageView> indicatorImages = new ArrayList<>();

    private int count;
    private int latestEventId = 0;
    private int oldestEventId = 0;

    private Timer timer = new Timer();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_ACTIVITY:
                    isNewEventFound = true;
                    rButton = (RadioButton)getActivity().findViewById(R.id.main_rbtn_activities);
                    rButton.setBackgroundColor(Color.GRAY);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the latest activity info and form the origin list items data
        latestEventId = getLatestEventId();
        oldestEventId = (latestEventId >= ORIGIN_ACTIVITY_COUNT) ? (latestEventId - ORIGIN_ACTIVITY_COUNT + 1) : 1;
        for(int i = latestEventId; i >= oldestEventId; i--) {
           formListItems(i);
        }

        count = listItems.size();
        // set adapter if network is good
        if (count > 0) {
            adapter = new EventAdapter(getActivity(), listItems);
            checkNewEvent();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_activities, null);
        rButton = (RadioButton)getActivity().findViewById(R.id.main_rbtn_activities);
        refreshableView = (RefreshableView) view.findViewById(R.id.fragment_activities_refreshable_view);
        listView = (ListView)view.findViewById(R.id.fragment_activities_list);
        listView.setAdapter(this.adapter);

        slidingLayout = (SlidingLayout) getActivity().findViewById(R.id.sliding_layout);
        slidingLayout.setScrollEvent(listView);

        // init viewpager
        View headView = inflater.inflate(R.layout.fragment_events_viewpager, null);
        final ViewPager pager = (ViewPager) headView.findViewById(R.id.fragment_events_viewpager);
        View indicatorView = headView.findViewById(R.id.fragment_events_indicator);
        listView.addHeaderView(headView);
        pager.setAdapter(getPagerAdapter());
        initIndicator(indicatorView);
        pager.setCurrentItem(1);        // begin from positon 1, as cycling slide used
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < indicatorImages.size(); i++) {
                    indicatorImages.get(i).setBackgroundColor(getActivity().getResources().getColor(R.color.white));
                }
                indicatorImages.get(position).setBackgroundColor(getActivity().getResources().getColor(R.color.black));

                // cycling slide
                if (listUrl.size() > 1) {
                    if (position < 1) {
                        position = listUrl.size() - 2;
                        pager.setCurrentItem(position, false);
                    } else if (position > listUrl.size() - 2) {
                        position = 1;
                        pager.setCurrentItem(position, false);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // refreshable view onRefresh() & onLoad
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                if (latestEventId < getLatestEventId()) {
                    latestEventId = getLatestEventId();
                    listItems.clear();
//                adapter.getClubEventList().clear();
                    for (int i = latestEventId; i >= oldestEventId; i--) {
                        formListItems(i);
                    }
                    count = listItems.size();
                    adapter.notifyDataSetChanged();
                }
                refreshableView.finishRefreshing();         // resume refresh head view
            }
        }, 0);

        refreshableView.setOnLoadListener(new RefreshableView.PushToLoadListener() {
            @Override
            public void onLoad() {
                int lastItem = refreshableView.getScrollLastItem();
                // as viewpager used as the head of listview, here must use "count + 1"
                if (lastItem == count + 1) {
                    if (oldestEventId > 1) {
                        int tmpEventId = oldestEventId - 1;
                        oldestEventId = (tmpEventId >= DOWN_ADD_ACTIVITY_COUNT) ? (tmpEventId - DOWN_ADD_ACTIVITY_COUNT + 1) : 1;
                        for (int i = tmpEventId; i >= oldestEventId; i--) {
                            formListItems(i);
                        }
                        count = listItems.size();
                        adapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(getActivity(), getString(R.string.already_oldest), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // listview item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                // as viewpager used in as the head of the listview, here must use "position -1"
                bundle.putParcelable("event", adapter.getClubEventList().get(position - 1));
                Intent intent = new Intent(getActivity(), EventsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
//        adapter.flushCache();
        BitmapCacheUtil.getInstance(getActivity()).flushCache();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.calcelAllTasks();
    }

    private int getLatestEventId() {
         /* query server */
        int eventId = 0;
        SharedPreferences preferences = getActivity().getSharedPreferences("latestEventId", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
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
            eventId = preferences.getInt("latestEventId", 5);   // if network error, get eventId from preference
        } else {
            eventId = Integer.valueOf(queryResult.split(",")[0]);
            editor.putInt("latestEventId", eventId);        // store eventId
            editor.commit();
        }

        return eventId;
    }

    private void formListItems(int eventId) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventId", eventId);
        listItems.add(map);
    }

    private ViewPagerAdapter getPagerAdapter() {

        // use 2nd images for cycling slide
        listUrl.add(HttpUtil.ACTIVITY_URL + "1_3.jpg");     // 2nd 3
        listUrl.add(HttpUtil.ACTIVITY_URL + "5_1.jpg");     // 0
        listUrl.add(HttpUtil.ACTIVITY_URL + "3_3.jpg");     // 1
        listUrl.add(HttpUtil.ACTIVITY_URL + "6_1.jpg");     // 2
        listUrl.add(HttpUtil.ACTIVITY_URL + "1_3.jpg");     // 3
        listUrl.add(HttpUtil.ACTIVITY_URL + "5_1.jpg");     // 2nd 0

        return new ViewPagerAdapter(listUrl, getActivity());
    }

    private void checkNewEvent() {
        if (timer != null) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (getLatestEventId() > latestEventId) {
                        handler.sendEmptyMessage(0x123);
                    }
                }
            }, 1000, 30000);
        }
    }

    private void initIndicator(View indicatorView) {

        for (int i = 0; i < listUrl.size(); i++) {
            ImageView imageView = new ImageView(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
            params.setMargins(14, 20, 14, 20);
            imageView.setLayoutParams(params);
            indicatorImages.add(imageView);

            if (i == 1) {
                indicatorImages.get(i).setBackgroundColor(getActivity().getResources().getColor(R.color.black));
            } else {
                indicatorImages.get(i).setBackgroundColor(getActivity().getResources().getColor(R.color.white));
            }
            ((ViewGroup) indicatorView).addView(indicatorImages.get(i));
        }
        // for viewpager cycling slide
        indicatorImages.get(0).setVisibility(View.INVISIBLE);
        indicatorImages.get(listUrl.size() - 1).setVisibility(View.INVISIBLE);
    }
}
