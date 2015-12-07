package com.fsl.fslclubs.events;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.util.HttpUtil;
import com.fsl.fslclubs.util.BitmapCacheUtil;
import com.fsl.fslclubs.util.BitmapWorkerTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by B47714 on 11/20/2015.
 */
public class EventAdapter extends SimpleAdapter {
    private Set<BitmapWorkerTask> taskCollection;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<? extends Map<String, ?>> mData;
    private ListView mListView;
    private ArrayList<ClubEvent> clubEventList = new ArrayList<>();
    private ClubEvent clubEvent = null;


    public EventAdapter(Context context, List<? extends Map<String, ?>> data) {
        super(context, data, 0, null, null);
        taskCollection = new HashSet<>();
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        Map<String, ?> dataItem = mData.get(position);
        int eventId = (Integer) dataItem.get("eventId");

        // check whether club event exsit already, because getview induced multiple times when drawing one item sometime.
        boolean isClubExsit = false;
        for (ClubEvent clubEvent : clubEventList) {
            if (clubEvent.getId().equals(String.valueOf(eventId))) {
                this.clubEvent = clubEvent;
                isClubExsit = true;
            }
        }
        if (!isClubExsit) {
            clubEvent = getData(eventId);
        }

        if (clubEvent == null) {
            return null;
        }

        // get view and its widgets
        if (mListView == null) {
            mListView = (ListView) parent;
        }

        if (convertView == null) {
            view = (View) mInflater.inflate(R.layout.adapter_activities, null);
        } else {
            view = convertView;
        }
        ImageView imageView = (ImageView)view.findViewById(R.id.adapter_activity_icon);
        TextView txtName = (TextView)view.findViewById(R.id.adapter_activity_name);
        TextView txtTime = (TextView)view.findViewById(R.id.adapter_activity_time);
        TextView txtAddress = (TextView)view.findViewById(R.id.adapter_activity_address);

        // set icon, name, time, address of the event item
        String bitmapUrl = HttpUtil.ACTIVITY_URL + clubEvent.getIcon();
        imageView.setTag(bitmapUrl);        // incase image disorder
        imageView.setImageDrawable(clubEvent.getDrawable());
        txtName.setText(clubEvent.getName());
        txtTime.setText(clubEvent.getTime());
        txtAddress.setText(clubEvent.getAddress());

        return view;
    }

    public ArrayList<ClubEvent> getClubEventList() {
        return clubEventList;
    }

    /*
        cancel download task
     */
    public void calcelAllTasks() {
        if (taskCollection != null) {
            for (BitmapWorkerTask task : taskCollection) {
                task.cancel(false);
            }
        }
    }

    private ClubEvent getData(int eventId) {
        // try to get club info from preference
        SharedPreferences preferences = mContext.getSharedPreferences("eventInfo" + eventId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String queryResult = preferences.getString("eventInfo" + eventId, "");

        if (queryResult.equals("")) {
            queryResult = downloadEventInfo(eventId);
            if (queryResult == null) {
                return null;
            }
            editor.putString("eventInfo" + eventId, queryResult);
            editor.commit();
        }

        // form Object clubEvent
        String[] info = queryResult.split(",");
        clubEvent = new ClubEvent(info[0], info[1], info[2],
                info[3], info[4], info[5], info[6], null);
        clubEventList.add(clubEvent);

        // get icon image from cache
        final String bitmapUrl = HttpUtil.ACTIVITY_URL + clubEvent.getIcon();
        final BitmapCacheUtil bitmapCache = BitmapCacheUtil.getInstance(mContext);
        BitmapDrawable drawable = bitmapCache.getBitmapFromCache(bitmapUrl);
        clubEvent.setDrawable(drawable);

        // if not exsit in cache, download it and write into cache
        if (drawable == null) {
            final BitmapWorkerTask task = new BitmapWorkerTask(mContext);
            task.setOnFinishListener(new BitmapWorkerTask.OnFinishListener() {
                @Override
                public void onFinish(BitmapDrawable bitmapDrawable) {
                    ImageView imageView = (ImageView) mListView.findViewWithTag(bitmapUrl);
                    if (imageView != null && bitmapDrawable != null) {
                        imageView.setImageDrawable(bitmapDrawable);
                    }
                    taskCollection.remove(task);
                }
            });
            taskCollection.add(task);
            task.execute(bitmapUrl);
        }

        return clubEvent;
    }

    private String downloadEventInfo(int eventId) {
        Map<String, String> params = new HashMap<>();
        params.put("id", eventId + "");

        final String url = HttpUtil.BASE_URL + "activities";
        String queryResult = null;
        try {
            queryResult = HttpUtil.queryStringForPost(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check query result
        if (queryResult == null) {
            Toast.makeText(mContext, mContext.getString(R.string.network_error),
                    Toast.LENGTH_SHORT).show();
        }

        return queryResult;
    }

}
