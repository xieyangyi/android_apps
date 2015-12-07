package com.fsl.fslclubs.events;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fsl.fslclubs.R;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by B47714 on 11/21/2015.
 */
public class RefreshableView extends LinearLayout
        implements View.OnTouchListener, AbsListView.OnScrollListener {
    public final static int STATUS_PULL_TO_REFRESH = 0;
    public final static int STATUS_RELEASE_TO_REFRESH = 1;
    public final static int STATUS_REFRESHING = 2;
    public final static int STATUS_REFRESH_FINISHED = 3;
    public final static int SCROLL_SPEED = -20;
    public final static int PULL_TO_RELEASE_DISTANCE = 100;    // header top margin larger than this, pull change to release
    public final static String UPDATED_AT = "updated_at";

    private PullToRefreshListener onRefreshListener;
    private PushToLoadListener onLoadListener;
    private int scrollLastItem;

    private SharedPreferences preferences;
    private View header;
    private ListView listView;
    private ProgressBar progressBar;
    private ImageView imgvArrow;
    private TextView txtDesc;
    private TextView txtUpdateAt;

    private MarginLayoutParams headerLayoutParams;
    private int mId = -1;
    private int hideHeaderHeight;

    private int currentStatus = STATUS_REFRESH_FINISHED;
    private int lastStatus = currentStatus;
    private String lastUpdatedTime;

    private float yDown;
    private int touchSlop;
    private boolean isLoadOnce;
    private boolean isAbleToPull;

    public RefreshableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        preferences = context.getSharedPreferences("lastUPdateTime", Context.MODE_PRIVATE);

        header = LayoutInflater.from(context).inflate(R.layout.listview_pull_refresh, null);
        progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
        imgvArrow = (ImageView) header.findViewById(R.id.arrow);
        txtDesc = (TextView) header.findViewById(R.id.descrption);
        txtUpdateAt = (TextView) header.findViewById(R.id.updated_at);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        refreshUpdatedAtValue();
        setOrientation(VERTICAL);
        addView(header, 0);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !isLoadOnce) {
            hideHeaderHeight = -header.getHeight();
            headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
            headerLayoutParams.topMargin = hideHeaderHeight;
            header.setLayoutParams(headerLayoutParams);
            listView = (ListView) getChildAt(1);   // listview must be the first widget in this refresh layout
            listView.setOnTouchListener(this);
            listView.setOnScrollListener(this);
            isLoadOnce = true;
        }
    }

    public int getScrollLastItem() {
        return scrollLastItem;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        setIsAbleToPull(event);
        if (isAbleToPull) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    yDown = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float yMove = event.getRawY();
                    int distance = (int) (yMove - yDown);
                    if (distance < 0 && headerLayoutParams.topMargin <= hideHeaderHeight) {
                        return false;
                    }
                    if (distance < touchSlop) {
                        return false;
                    }
                    if (currentStatus != STATUS_REFRESHING) {
                        if (headerLayoutParams.topMargin > PULL_TO_RELEASE_DISTANCE) {
                            currentStatus = STATUS_RELEASE_TO_REFRESH;
                        } else {
                            currentStatus = STATUS_PULL_TO_REFRESH;
                        }
                        headerLayoutParams.topMargin = (distance / 2) + hideHeaderHeight;
                        header.setLayoutParams(headerLayoutParams);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                default:
                    if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
                        currentStatus = STATUS_REFRESHING;
                        updateHeaderView();
                        if (onRefreshListener != null) {
                            onRefreshListener.onRefresh();
                            return true;            // avoid press listener induced
                        }
                    } else if (currentStatus == STATUS_PULL_TO_REFRESH) {
                        new HideHeaderTask().execute();
                    }
                    break;
            }
        }

        if (currentStatus == STATUS_PULL_TO_REFRESH
                || currentStatus == STATUS_RELEASE_TO_REFRESH) {
            updateHeaderView();
            listView.setPressed(false);
            listView.setFocusable(false);
            listView.setFocusableInTouchMode(false);
            lastStatus = currentStatus;
            return true;
        }

        return false;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        scrollLastItem = firstVisibleItem + visibleItemCount;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            onLoadListener.onLoad();
        }
    }

    public void setOnRefreshListener(PullToRefreshListener listener, int id) {
        this.onRefreshListener = listener;
        mId = id;
    }

    public void setOnLoadListener(PushToLoadListener listener) {
        this.onLoadListener = listener;
    }

    public void finishRefreshing() {
        currentStatus = STATUS_REFRESH_FINISHED;
        Calendar calendar = Calendar.getInstance();
        String currentDate = calendar.get(Calendar.YEAR) + "-"
                + (calendar.get(Calendar.MONTH) + 1) + "-"
                + calendar.get(Calendar.DAY_OF_MONTH) + " "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                + calendar.get(Calendar.MINUTE);
        preferences.edit().putString(UPDATED_AT + mId, currentDate).commit();
        new HideHeaderTask().execute();
    }

    private void setIsAbleToPull(MotionEvent event) {
        View firstChild = listView.getChildAt(0);
        if (firstChild != null) {
            int firstVisiblePos = listView.getFirstVisiblePosition();
            // listview come to the top, enable pull
            if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
                if (!isAbleToPull) {
                    yDown = event.getRawY();
                }
                isAbleToPull = true;
            } else {
                if (headerLayoutParams.topMargin != hideHeaderHeight) {
                    headerLayoutParams.topMargin = hideHeaderHeight;
                    header.setLayoutParams(headerLayoutParams);
                }
                isAbleToPull = false;
            }
        } else {
            // listview don't have any item, enable pull
            isAbleToPull = true;
        }
    }

    private void updateHeaderView() {
       // update header view if status changed
       if (lastStatus != currentStatus) {
           if (currentStatus == STATUS_PULL_TO_REFRESH) {
               txtDesc.setText(getResources().getString(R.string.listview_pull_to_refresh));
               imgvArrow.setVisibility(View.VISIBLE);
               progressBar.setVisibility(View.GONE);
               rotateArrow();
           } else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
               txtDesc.setText(getResources().getString(R.string.listview_release_to_refresh));
               imgvArrow.setVisibility(View.VISIBLE);
               progressBar.setVisibility(View.GONE);
               rotateArrow();
           } else if (currentStatus == STATUS_REFRESHING) {
               txtDesc.setText(getResources().getString(R.string.listview_refreshing));
               progressBar.setVisibility(View.VISIBLE);
               imgvArrow.clearAnimation();
               imgvArrow.setVisibility(View.GONE);
           }
           refreshUpdatedAtValue();
       }
    }

    private void rotateArrow() {
        float pivotX = imgvArrow.getWidth() / 2f;
        float pivotY = imgvArrow.getHeight() / 2f;
        float fromDegrees = 0f;
        float toDegrees = 0f;
        if (currentStatus == STATUS_PULL_TO_REFRESH) {
            fromDegrees = 180f;
            toDegrees = 360f;
        } else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
            fromDegrees = 0f;
            toDegrees = 180f;
        }

        RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
        animation.setDuration(100);
        animation.setFillAfter(true);
        imgvArrow.startAnimation(animation);
    }

    private void refreshUpdatedAtValue() {
        lastUpdatedTime = preferences.getString(UPDATED_AT + mId, "error");
        String updateAtValue;

        if (lastUpdatedTime.equals("error")) {
            updateAtValue = getResources().getString(R.string.listview_not_updated_yet);
        } else {
            updateAtValue = getResources().getString(R.string.listview_update_at) + " " + lastUpdatedTime;
        }
        txtUpdateAt.setText(updateAtValue);
    }

    class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            int topMargin = headerLayoutParams.topMargin;
            while (true) {
                topMargin = topMargin + SCROLL_SPEED;
                if (topMargin <= hideHeaderHeight) {
                    topMargin = hideHeaderHeight;
                    break;
                }
                publishProgress(topMargin);
                sleep(10);
            }
            return topMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            headerLayoutParams.topMargin = values[0];
            header.setLayoutParams(headerLayoutParams);
        }

        @Override
        protected void onPostExecute(Integer topMargin) {
            headerLayoutParams.topMargin = topMargin;
            header.setLayoutParams(headerLayoutParams);
            currentStatus = STATUS_REFRESH_FINISHED;
        }
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface PullToRefreshListener {
        void onRefresh();
    }

    public interface PushToLoadListener {
        void onLoad();
    }
}
