package com.fsl.fslclubs.myEvents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fsl.fslclubs.myEvents.AlarmActivity;

/**
 * Created by B47714 on 10/3/2015.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, AlarmActivity.class);
        context.startActivity(intent);
    }
}
