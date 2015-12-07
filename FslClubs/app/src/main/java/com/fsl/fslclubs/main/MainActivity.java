package com.fsl.fslclubs.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.events.EventsFragment;
import com.fsl.fslclubs.clubs.ClubsFragment;
import com.fsl.fslclubs.login.LoginActivity;
import com.fsl.fslclubs.login.User;
import com.fsl.fslclubs.myEvents.MyEventsFragment;
import com.fsl.fslclubs.me.MeFragment;
import com.fsl.fslclubs.util.SlidingLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by B47714 on 10/14/2015.
 */
public class MainActivity extends Activity {
    private SlidingLayout slidingLayout;
    private LinearLayout rightView;
    private RadioGroup radioGroup;
    private ViewPager viewPager;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slidingLayout = (SlidingLayout) findViewById(R.id.sliding_layout);
        rightView = (LinearLayout) findViewById(R.id.right_main);
        rightView.setClickable(true);
        slidingLayout.setScrollEvent(rightView);
        radioGroup = (RadioGroup)findViewById(R.id.main_radio_group);
//        viewPager = (ViewPager) findViewById(R.id.activity_main_viewPager);
        fragmentManager = getFragmentManager();

        initLeftView();

//        initViewPager();
//
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                switch (checkedId) {
//                    case R.id.main_rbtn_activities:
//                        viewPager.setCurrentItem(0, false);
//                        break;
//                    case R.id.main_rbtn_friends:
//                        viewPager.setCurrentItem(0, false);
//                        break;
//                    case R.id.main_rbtn_clubs:
//                        viewPager.setCurrentItem(0, false);
//                        break;
//                    case R.id.main_rbtn_me:
//                        viewPager.setCurrentItem(0, false);
//                        break;
//                }
//            }
//        });
        // set radio gruop checked change listener
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Fragment fragment = null;
                ActionBar actionBar = getActionBar();
                switch (checkedId) {
                    case R.id.main_rbtn_activities:
                        Log.v("activity_main", "fragment 1");
//                        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//                        ActionBar.Tab tab = actionBar
//                                .newTab()
//                                .setText(R.string.activities)
//                                .setTabListener(new TabListener<EventsFragment>(
//                                        MainActivity.this,
//                                        getResources().getString(R.string.activities),
//                                        EventsFragment.class
//                                ));
//                        actionBar.addTab(tab);
//                        tab = actionBar
//                                .newTab()
//                                .setText(R.string.my_activity)
//                                .setTabListener(new TabListener<MyEventsFragment>(
//                                        MainActivity.this,
//                                        getResources().getString(R.string.my_activity),
//                                        MyEventsFragment.class
//                                ));
//                        actionBar.addTab(tab);
                        fragment = new EventsFragment();
                        break;
                    case R.id.main_rbtn_friends:
                        Log.v("activity_main", "fragment 2");
                        actionBar.removeAllTabs();
                        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                        fragment = new MyEventsFragment();
                        break;
                    case R.id.main_rbtn_clubs:
                        Log.v("activity_main", "fragment 3");
                        actionBar.removeAllTabs();
                        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                        fragment = new ClubsFragment();
                        break;
                    case R.id.main_rbtn_me:
                        Log.v("activity_main", "fragment 4");
                        actionBar.removeAllTabs();
                        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                        fragment = new MeFragment();
                        break;
                    default:
                        Log.v("activity_main", "wrong fragment");
                        break;
                }

                // replace fragment
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (fragment != null)
                    transaction.replace(R.id.main_content,fragment);
                transaction.commit();
            }
        });

        // set default radio button: activities
        RadioButton radioButton = (RadioButton)findViewById(R.id.main_rbtn_activities);
        radioButton.setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            int radioButtonCheckId = bundle.getInt("radioButtonCheckId");
            if (radioButtonCheckId != 0) {
                RadioButton radioButton = (RadioButton)findViewById(radioButtonCheckId);
                radioButton.setChecked(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // search action
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchView == null)
            return true;
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        ComponentName cn = new ComponentName("com.fsl.fslclubs", "com.fsl.fslclubs.main.SearchResultActivity");
        SearchableInfo info = searchManager.getSearchableInfo(cn);
        searchView.setSearchableInfo(info);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);


    }

    public SlidingLayout getSlidingLayout() {
        return slidingLayout;
    }

    private void initLeftView() {
        Fragment fragment = null;
        User user = LoginActivity.getLoggedInUser();
        if (user != null) {
            fragment = new UserLoggedInFragment();
        } else {
            fragment = new UserNotLoggedInFragment();
        }

        // replace fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragment != null)
            transaction.replace(R.id.main_person_info,fragment);
        transaction.commit();
    }

//    private void initViewPager() {
//        List<Fragment> fragmentList = new ArrayList<>();
//        fragmentList.add(new MeFragment());
//        fragmentList.add(new MyEventsFragment());
//        fragmentList.add(new ClubsFragment());
//        fragmentList.add(new MeFragment());
//
//        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(fragmentManager, fragmentList);
//        viewPager.setAdapter(adapter);
//        viewPager.setCurrentItem(0);
//        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                switch (position) {
//                    case 0:
//                        radioGroup.check(R.id.main_rbtn_activities);
//                        break;
//                    case 1:
//                        radioGroup.check(R.id.main_rbtn_friends);
//                        break;
//                    case 2:
//                        radioGroup.check(R.id.main_rbtn_clubs);
//                        break;
//                    case 3:
//                        radioGroup.check(R.id.main_rbtn_me);
//                        break;
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
//
//    }


}
