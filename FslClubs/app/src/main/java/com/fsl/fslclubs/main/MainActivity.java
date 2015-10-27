package com.fsl.fslclubs.main;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.activities.ActivitiesFragment;
import com.fsl.fslclubs.clubs.ClubsFragment;
import com.fsl.fslclubs.friends.FriendsFragment;
import com.fsl.fslclubs.me.MeFragment;

/**
 * Created by B47714 on 10/14/2015.
 */
public class MainActivity extends Activity {
    private RadioGroup radioGroup;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        radioGroup = (RadioGroup)findViewById(R.id.main_radio_group);
        fragmentManager = getFragmentManager();

        // set radio gruop checked change listener
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Fragment fragment = null;
                switch(checkedId) {
                    case R.id.main_rbtn_activities:
                        Log.v("activity_main", "fragment 1");
                        fragment = new ActivitiesFragment();
                        break;
                    case R.id.main_rbtn_friends:
                        Log.v("activity_main", "fragment 2");
                        fragment = new FriendsFragment();
                        break;
                    case R.id.main_rbtn_clubs:
                        Log.v("activity_main", "fragment 3");
                        fragment = new ClubsFragment();
                        break;
                    case R.id.main_rbtn_me:
                        Log.v("activity_main", "fragment 4");
                        fragment = new MeFragment();
                        break;
                    default:
                        Log.v("activity_main", "wrong fragment");
                        break;
                }

                // replace fragment
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_content, fragment);
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
}
