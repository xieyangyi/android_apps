package com.fsl.fslclubs.clubs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.util.HttpUtil;
import com.fsl.fslclubs.login.LoginActivity;
import com.fsl.fslclubs.login.User;
import com.fsl.fslclubs.main.MainActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by B47714 on 10/15/2015.
 */
public class ClubsActivity extends Activity {
    private WebView webView;
    private Button btnClubJoin;
    private Button btnClubMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubs);
        getActionBar().setDisplayHomeAsUpEnabled(true);     // set action bar navigation
        webView = (WebView)findViewById(R.id.webV_activity_clubs);
        btnClubJoin = (Button)findViewById(R.id.btn_activity_clubs_join);
        btnClubMember = (Button)findViewById(R.id.btn_activity_clubs_member);

        // get clubs id by intent data
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        final int clubId = bundle.getInt("clubID");
        final int radioButtonCheckId = bundle.getInt("radioButtonCheckId");
        Club clubs[] = new Club[] {
                new Club(Club.BASKETBALL_ID, null, null, 0, "basketball.html"),
                new Club(Club.FOOTBALL_ID, null, null, 0, "football.html"),
                new Club(Club.SNOOKER_ID, null, null, 0, "snooker.html"),
                new Club(Club.YOGA_ID, null, null, 0, "yoga.html"),
                new Club(Club.PINGPANG_ID, null, null, 0, "pingpang.html"),
                new Club(Club.TENNIS_ID, null, null, 0, "tennis.html"),
                new Club(Club.RIDING_ID, null, null, 0, "riding.html"),
                new Club(Club.BADMINTON_ID, null, null, 0, "badminton.html"),
        };

        // set club description imageview
        for(int i = 0; i < clubs.length; i++) {
            if(clubId == clubs[i].getId()) {
                setWebView(webView, clubs[i].getWebViewAddr());
            }
        }

        // set join club button, if already join in, don't set click listener
        final User loggedInUser = LoginActivity.getLoggedInUser();
        if (loggedInUser != null && loggedInUser.getClub().contains(String.valueOf(clubId))) {
            btnClubJoin.setText(getString(R.string.already_joined_club));
            btnClubJoin.setClickable(false);
        } else {
            btnClubJoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // check whether logged in, only logged into user can join club
                    if (loggedInUser == null) {
                        Toast.makeText(ClubsActivity.this, getString(R.string.join_after_login), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ClubsActivity.this, LoginActivity.class);
                        startActivity(intent);
                        return;
                    }

                    // add user info into server database
                    String username = loggedInUser.getName();
                    String userId = loggedInUser.getId();
                    if (!joinClub(String.valueOf(clubId), userId, username))
                        return;

                    // back to fragment
                    Bundle bundle = new Bundle();
                    bundle.putInt("radioButtonCheckId", radioButtonCheckId);
                    Intent intent = new Intent(ClubsActivity.this, MainActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }

        btnClubMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClubMembers(String.valueOf(clubId));
            }
        });
    }

    private boolean joinClub(String clubId, String userId, String username) {
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", HttpUtil.REQUEST_JOIN_CLUB);
        params.put("clubId", clubId);
        params.put("userId", userId);
        params.put("username", username);

        final String url = HttpUtil.BASE_URL + "login";
        String queryResult = null;
        try {
            queryResult = HttpUtil.queryStringForPost(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check query result
        if(queryResult.equals(HttpUtil.JOIN_CLUB_SUCCESS)) {
            Toast.makeText(ClubsActivity.this, getString(R.string.join_club_success), Toast.LENGTH_SHORT).show();
            // update LoggedInUser
            User user = LoginActivity.getLoggedInUser();
            LoginActivity.setLoggedInUser(new User(
                    user.getId(),
                    user.getPhone(),
                    user.getPassword(),
                    user.getName(),
                    (user.getClub() == null) ? clubId : (user.getClub() + "," + clubId),
                    user.getSex(),
                    user.getEmail(),
                    user.getCoreid(),
                    user.getSignature(),
                    user.getLegalid(),
                    user.getActivity()
            ));
            return true;
        } else {
            Toast.makeText(ClubsActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean showClubMembers(String clubId) {
        Map<String, String> params = new HashMap<>();
        params.put("clubId", clubId);

        final String url = HttpUtil.BASE_URL + "check_club_members";
        String queryResult = null;
        try {
            queryResult = HttpUtil.queryStringForPost(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check query result
        if(queryResult != null && !queryResult.equals(HttpUtil.CHECK_CLUB_MEMBER_FAILED)) {
            String member = queryResult;
            if (member.equals("")) {
                member = getString(R.string.no_member_exist);
            }
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.club_member))
                    .setMessage(member)
                    .show();
            return true;
        } else {
            Toast.makeText(ClubsActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            return false;
        }
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

        // load the html in the apk
        webview.loadUrl("file:///android_asset/" + website);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

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
