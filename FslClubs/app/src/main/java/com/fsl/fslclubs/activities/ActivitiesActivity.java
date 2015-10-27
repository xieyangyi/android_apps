package com.fsl.fslclubs.activities;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.friends.MyActivityProvider;
import com.fsl.fslclubs.login.HttpUtil;
import com.fsl.fslclubs.login.LoginActivity;
import com.fsl.fslclubs.login.User;
import com.fsl.fslclubs.main.MainActivity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;

/**
 * Created by B47714 on 10/20/2015.
 */
public class ActivitiesActivity extends Activity {
    private WebView webview;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);
        webview = (WebView)findViewById(R.id.webV_activity_activities);
        btnSignup = (Button)findViewById(R.id.btn_activity_activities_signup);

        // get Intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        final String activityId = bundle.getString("activityId");
        String name = bundle.getString("name");
        String icon = bundle.getString("icon");
        String website = bundle.getString("website");
        String address = bundle.getString("address");
        String time = bundle.getString("time");
        String expireTime = bundle.getString("expireTime");

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
            btnSignup.setVisibility(View.INVISIBLE);
            btnSignup.setClickable(false);
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
                        Toast.makeText(ActivitiesActivity.this, getString(R.string.signup_after_login), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ActivitiesActivity.this, LoginActivity.class);
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
                    Intent intent = new Intent(ActivitiesActivity.this, MainActivity.class);
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
            Toast.makeText(ActivitiesActivity.this, getString(R.string.signup_activity_success), Toast.LENGTH_SHORT).show();
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
            // update MyActivityProvider
            saveSignedUpActivity(this, activityId, name, icon, website, address, time, expireTime);
            return true;
        } else {
            Toast.makeText(ActivitiesActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
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

        values.put(MyActivityProvider.TBL_ACTIVITY_ID, id);
        values.put(MyActivityProvider.TBL_NAME, name);
        values.put(MyActivityProvider.TBL_ICON, icon);
        values.put(MyActivityProvider.TBL_WEBSITE, website);
        values.put(MyActivityProvider.TBL_ADDRESS, address);
        values.put(MyActivityProvider.TBL_TIME, time);
        values.put(MyActivityProvider.TBL_EXPIRE_TIME, expireTime);
        values.put(MyActivityProvider.TBL_ALARM_DATE, "");
        values.put(MyActivityProvider.TBL_ALARM_TIME, "");
        values.put(MyActivityProvider.TBL_ALARM_IS_ON, 0);

        Uri uri = MyActivityProvider.USER_TBL_URI;
        context.getContentResolver().insert(uri, values);

        return true;
    }
}
