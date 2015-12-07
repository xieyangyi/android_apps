package com.fsl.fslclubs.events;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.myEvents.MyEventProvider;
import com.fsl.fslclubs.util.HttpUtil;
import com.fsl.fslclubs.login.LoginActivity;
import com.fsl.fslclubs.login.User;
import com.fsl.fslclubs.main.MainActivity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by B47714 on 10/20/2015.
 */
public class EventsActivity extends Activity {
    private WebView webview;
    private Button btnSignup;
    private LinearLayout layoutBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);
        getActionBar().setDisplayHomeAsUpEnabled(true);     // set action bar navigation
        webview = (WebView)findViewById(R.id.webV_activity_activities);
        btnSignup = (Button)findViewById(R.id.btn_activity_activities_signup);
        layoutBottom = (LinearLayout) findViewById(R.id.activity_activities_button);

        // get Intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ClubEvent event = bundle.getParcelable("event");
        final String activityId = event.getId();
        String name = event.getName();
        String icon = event.getIcon();
        String website = event.getWebsite();
        String address = event.getAddress();
        String time = event.getTime();
        String expireTime = event.getExpireTime();

        // set webview
        setWebView(webview, website);

        // set sign up button
        setSignUpButton(activityId, name, icon, website, address, time, expireTime);
    }

    private boolean setWebView(WebView webview, String website) {
        WebSettings settings = webview.getSettings();

        // font size to the lagest
        settings.setTextSize(WebSettings.TextSize.LARGEST);
        // enable JS
        settings.setJavaScriptEnabled(true);
        // auto fit for the screen
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        // load image, for 4.0 above, this set true will stop image load
        settings.setBlockNetworkImage(false);
        // zoom
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);
        settings.setDisplayZoomControls(false);
        // scrollbar
        webview.setVerticalScrollBarEnabled(false);

        webview.loadUrl(HttpUtil.ACTIVITY_URL + website);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        return true;
    }

    /**
     * check expire time first, then check whether log in , then sign up activity
     * @param activityId: activity id in String
     * @param expireTime: activity expire time in String
     */
    private boolean setSignUpButton(final String activityId, final String name, final String icon, final String website,
                                    final String address, final String time, final String expireTime) {
        final User loggedInUser = LoginActivity.getLoggedInUser();
        if (expireTime == null || expireTime.equals("") || expireTime.equals(" ")) {
          // not a sign up activity
            layoutBottom.setVisibility(View.INVISIBLE);
            btnSignup.setClickable(false);
            btnSignup.setTextSize(0);
        } else if (getExpireTime(expireTime) < System.currentTimeMillis()) {
            // activity expired, can't be clickable
            btnSignup.setText(getString(R.string.activity_expired));
            btnSignup.setClickable(false);
        } else if (loggedInUser != null && loggedInUser.getActivity().contains(activityId)) {
            // user already sign up this activity, can't be clickable
            btnSignup.setText(getString(R.string.already_signed_activity));
            btnSignup.setClickable(false);
        } else {
            // can sign up,
            btnSignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // check whether logged in, only logged into user can join club
                    if (loggedInUser == null) {
                        Toast.makeText(EventsActivity.this, getString(R.string.signup_after_login), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EventsActivity.this, LoginActivity.class);
                        startActivity(intent);
                        return;
                    }

                    // add user info into server database
                    String userId = loggedInUser.getId();
                    if (!signupActivity(activityId, userId, name, icon, website,
                            address, time, expireTime))
                        return;

                    // back to fragment
                    Bundle bundle = new Bundle();
                    bundle.putInt("radioButtonCheckId", R.id.main_rbtn_activities);
                    Intent intent = new Intent(EventsActivity.this, MainActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }
        return true;
    }

    private boolean signupActivity(String activityId, String userId, String name, String icon, String website,
                                   String address, String time, String expireTime) {
        Map<String, String> params = new HashMap<>();
        params.put("activityId", activityId);
        params.put("userId", userId);

        final String url = HttpUtil.BASE_URL + "signup_activities";
        String queryResult = null;
        try {
            queryResult = HttpUtil.queryStringForPost(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check query result
        if(queryResult.equals(HttpUtil.SIGNUP_ACTIVITY_SUCCESS)) {
            Toast.makeText(EventsActivity.this, getString(R.string.signup_activity_success), Toast.LENGTH_SHORT).show();
            // update LoggedInUser
            User user = LoginActivity.getLoggedInUser();
            LoginActivity.setLoggedInUser(new User(
                    user.getId(),
                    user.getPhone(),
                    user.getPassword(),
                    user.getName(),
                    user.getClub(),
                    user.getSex(),
                    user.getEmail(),
                    user.getCoreid(),
                    user.getSignature(),
                    user.getLegalid(),
                    (user.getActivity() == null) ? activityId : (user.getActivity() + "," + activityId)
            ));
            // update MyEventProvider
            saveSignedUpActivity(this, activityId, name, icon, website, address, time, expireTime);
            return true;
        } else {
            Toast.makeText(EventsActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     *  trans the time from string to millis
     *  @time: time string in the format of "year-month-day[-hour]", hour is 20 by default
     */
    private long getExpireTime(String time) {
        String[] timeStr = time.split("-");
        int year = Integer.valueOf(timeStr[0]);
        int month = Integer.valueOf(timeStr[1]);
        int dayOfMonth = Integer.valueOf(timeStr[2]);
        int hourOfDay = (timeStr.length >= 4) ? (Integer.valueOf(timeStr[3])) : 20;     // 20:00 is the default activity expire time
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, dayOfMonth, hourOfDay, 0);        // month is 0~11 in calendar

        return calendar.getTimeInMillis();
    }

    private boolean saveSignedUpActivity(Context context, String id, String name, String icon, String website,
                                         String address, String time, String expireTime) {
        ContentValues values = new ContentValues();
        values.clear();

        values.put(MyEventProvider.TBL_ACTIVITY_ID, id);
        values.put(MyEventProvider.TBL_NAME, name);
        values.put(MyEventProvider.TBL_ICON, icon);
        values.put(MyEventProvider.TBL_WEBSITE, website);
        values.put(MyEventProvider.TBL_ADDRESS, address);
        values.put(MyEventProvider.TBL_TIME, time);
        values.put(MyEventProvider.TBL_EXPIRE_TIME, expireTime);
        values.put(MyEventProvider.TBL_ALARM_DATE, "");
        values.put(MyEventProvider.TBL_ALARM_TIME, "");
        values.put(MyEventProvider.TBL_ALARM_IS_ON, 0);

        Uri uri = MyEventProvider.USER_TBL_URI;
        context.getContentResolver().insert(uri, values);

        return true;
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
